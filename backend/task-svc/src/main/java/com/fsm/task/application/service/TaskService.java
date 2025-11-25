package com.fsm.task.application.service;

import com.fsm.task.application.dto.AssignTaskRequest;
import com.fsm.task.application.dto.AssignTaskResponse;
import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.ReassignTaskRequest;
import com.fsm.task.application.dto.ReassignTaskResponse;
import com.fsm.task.application.dto.TaskListRequest;
import com.fsm.task.application.dto.TaskListResponse;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.application.exception.InvalidAssignmentException;
import com.fsm.task.application.exception.TaskNotFoundException;
import com.fsm.task.domain.model.Assignment;
import com.fsm.task.domain.model.Assignment.AssignmentStatus;
import com.fsm.task.domain.model.AssignmentHistory;
import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import com.fsm.task.domain.repository.AssignmentHistoryRepository;
import com.fsm.task.domain.repository.AssignmentRepository;
import com.fsm.task.domain.repository.TaskRepository;
import com.fsm.task.domain.repository.TaskSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for task management operations.
 * Handles business logic for creating and managing service tasks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final TechnicianValidationService technicianValidationService;
    
    /**
     * Creates a new service task.
     * Sets initial status to UNASSIGNED and records the creator.
     * 
     * @param request the create task request containing task details
     * @param createdBy the username/email of the authenticated user creating the task
     * @return TaskResponse with the created task details
     */
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, String createdBy) {
        log.info("Creating new task with title: '{}' by user: {}", request.getTitle(), createdBy);
        
        ServiceTask task = ServiceTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .clientAddress(request.getClientAddress())
                .priority(request.getPriority())
                .estimatedDuration(request.getEstimatedDuration())
                .createdBy(createdBy)
                .build();
        
        ServiceTask savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());
        
        return TaskResponse.fromEntity(savedTask);
    }
    
    /**
     * Retrieves a list of tasks with filtering, sorting, and pagination.
     * 
     * @param request the task list request containing filter and pagination parameters
     * @return TaskListResponse with tasks, pagination info, and status counts
     */
    @Transactional(readOnly = true)
    public TaskListResponse getTasks(TaskListRequest request) {
        log.info("Fetching tasks with filters - status: {}, priority: {}, search: {}, sortBy: {}, sortOrder: {}, page: {}, pageSize: {}",
                request.getStatus(), request.getPriority(), request.getSearch(),
                request.getSortBy(), request.getSortOrder(), request.getPage(), request.getPageSize());
        
        // Build sorting - default sorting: priority desc, createdAt desc
        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());
        
        // Build pagination
        Pageable pageable = PageRequest.of(request.getPage(), request.getPageSize(), sort);
        
        // Build specification for filtering
        Specification<ServiceTask> spec = TaskSpecification.withFilters(
                request.getStatus(),
                request.getPriority(),
                request.getSearch()
        );
        
        // Execute query with filtering and pagination
        Page<ServiceTask> taskPage = taskRepository.findAll(spec, pageable);
        
        // Convert to DTOs
        List<TaskResponse> taskResponses = taskPage.getContent().stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
        
        // Get status counts for all tasks (not filtered)
        Map<String, Long> statusCounts = getStatusCounts();
        
        log.info("Found {} tasks (page {} of {})", taskResponses.size(), request.getPage() + 1, taskPage.getTotalPages());
        
        return TaskListResponse.builder()
                .tasks(taskResponses)
                .page(taskPage.getNumber())
                .pageSize(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .first(taskPage.isFirst())
                .last(taskPage.isLast())
                .statusCounts(statusCounts)
                .build();
    }
    
    /**
     * Assigns a task to a technician.
     * Handles both new assignments and reassignments.
     * Creates an assignment record and history, and updates the task atomically.
     * 
     * @param taskId the ID of the task to assign
     * @param request the assignment request containing technician ID
     * @param assignedBy the username of the user making the assignment
     * @return AssignTaskResponse with assignment details and workload info
     */
    @Transactional
    public AssignTaskResponse assignTask(Long taskId, AssignTaskRequest request, String assignedBy) {
        log.info("Assigning task {} to technician {} by user: {}", taskId, request.getTechnicianId(), assignedBy);
        
        // Find the task
        ServiceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        
        // Validate task can be assigned
        if (!task.canBeAssigned()) {
            throw new InvalidAssignmentException(
                    String.format("Task %d cannot be assigned. Current status: %s. Only UNASSIGNED or ASSIGNED tasks can be assigned.", 
                            taskId, task.getStatus()));
        }
        
        Long technicianId = request.getTechnicianId();
        
        // Validate technician exists and is active via identity-svc
        technicianValidationService.validateTechnician(technicianId);
        
        Long previousTechnicianId = null;
        boolean isReassignment = task.isAssigned();
        
        // Handle reassignment - mark previous assignment as REASSIGNED
        if (isReassignment) {
            Optional<Assignment> activeAssignment = assignmentRepository.findActiveAssignmentForTask(taskId);
            if (activeAssignment.isPresent()) {
                Assignment previousAssignment = activeAssignment.get();
                previousTechnicianId = previousAssignment.getTechnicianId();
                previousAssignment.markAsReassigned("Reassigned to technician " + technicianId);
                assignmentRepository.save(previousAssignment);
                log.info("Marked previous assignment {} as REASSIGNED", previousAssignment.getId());
            }
        }
        
        // Create new assignment
        Assignment assignment = Assignment.builder()
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Created new assignment with ID: {}", savedAssignment.getId());
        
        // Create assignment history record
        AssignmentHistory history;
        if (isReassignment) {
            history = AssignmentHistory.forReassignment(savedAssignment, previousTechnicianId, assignedBy, 
                    "Reassigned from technician " + previousTechnicianId + " to " + technicianId);
        } else {
            history = AssignmentHistory.forCreation(savedAssignment, assignedBy);
        }
        assignmentHistoryRepository.save(history);
        log.info("Created assignment history record");
        
        // Update task's assigned technician
        if (isReassignment) {
            task.reassignToTechnician(technicianId);
        } else {
            task.assignToTechnician(technicianId);
        }
        taskRepository.save(task);
        log.info("Updated task {} with assigned technician {}", taskId, technicianId);
        
        // Calculate technician workload
        int workload = assignmentRepository.getTechnicianWorkload(technicianId);
        log.info("Technician {} current workload: {} active assignments", technicianId, workload);
        
        return AssignTaskResponse.fromAssignment(savedAssignment, task, workload, assignedBy);
    }
    
    /**
     * Reassigns a task to a different technician.
     * Handles reassignment with reason tracking and validates domain invariants.
     * 
     * Domain Invariants:
     * - Cannot reassign COMPLETED tasks
     * - IN_PROGRESS tasks require a reason for reassignment
     * - Assignment history preserves audit trail
     * - Both old and new technician must exist and be active
     * 
     * @param taskId the ID of the task to reassign
     * @param request the reassignment request containing new technician ID and optional reason
     * @param reassignedBy the username of the user making the reassignment
     * @return ReassignTaskResponse with reassignment details and assignment history
     */
    @Transactional
    public ReassignTaskResponse reassignTask(Long taskId, ReassignTaskRequest request, String reassignedBy) {
        log.info("Reassigning task {} to technician {} by user: {}", taskId, request.getNewTechnicianId(), reassignedBy);
        
        // Find the task
        ServiceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        
        // Validate task can be reassigned (must be ASSIGNED or IN_PROGRESS, not COMPLETED)
        if (!task.canBeReassigned()) {
            throw new InvalidAssignmentException(
                    String.format("Task %d cannot be reassigned. Current status: %s. Only ASSIGNED or IN_PROGRESS tasks can be reassigned.", 
                            taskId, task.getStatus()));
        }
        
        // Require reason for IN_PROGRESS tasks
        if (task.isInProgress() && (request.getReason() == null || request.getReason().trim().isEmpty())) {
            throw new InvalidAssignmentException(
                    String.format("Task %d is IN_PROGRESS. A reason is required for reassigning IN_PROGRESS tasks.", 
                            taskId));
        }
        
        Long newTechnicianId = request.getNewTechnicianId();
        String reason = request.getReason();
        
        // Validate new technician exists and is active via identity-svc
        technicianValidationService.validateTechnician(newTechnicianId);
        
        Long previousTechnicianId = task.getAssignedTechnicianId();
        
        // Ensure task has a previous technician (must be assigned to someone)
        if (previousTechnicianId == null) {
            throw new InvalidAssignmentException(
                    String.format("Task %d has no assigned technician. Use assign endpoint instead.", taskId));
        }
        
        // Mark previous assignment as REASSIGNED
        Optional<Assignment> activeAssignment = assignmentRepository.findActiveAssignmentForTask(taskId);
        if (activeAssignment.isPresent()) {
            Assignment previousAssignment = activeAssignment.get();
            String reassignmentReason = reason != null ? reason : "Reassigned to technician " + newTechnicianId;
            previousAssignment.markAsReassigned(reassignmentReason);
            assignmentRepository.save(previousAssignment);
            log.info("Marked previous assignment {} as REASSIGNED", previousAssignment.getId());
        }
        
        // Create new assignment
        Assignment assignment = Assignment.builder()
                .taskId(taskId)
                .technicianId(newTechnicianId)
                .assignedAt(LocalDateTime.now())
                .assignedBy(reassignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Created new assignment with ID: {}", savedAssignment.getId());
        
        // Create assignment history record for reassignment
        String historyReason = reason != null 
                ? reason 
                : "Reassigned from technician " + previousTechnicianId + " to " + newTechnicianId;
        AssignmentHistory history = AssignmentHistory.forReassignment(
                savedAssignment, previousTechnicianId, reassignedBy, historyReason);
        assignmentHistoryRepository.save(history);
        log.info("Created assignment history record for reassignment");
        
        // Update task's assigned technician
        task.reassignToTechnician(newTechnicianId);
        taskRepository.save(task);
        log.info("Updated task {} with new assigned technician {}", taskId, newTechnicianId);
        
        // Calculate new technician workload
        int workload = assignmentRepository.getTechnicianWorkload(newTechnicianId);
        log.info("New technician {} current workload: {} active assignments", newTechnicianId, workload);
        
        // Retrieve assignment history for this task
        List<AssignmentHistory> taskHistory = assignmentHistoryRepository.findByTaskIdOrderByActionAtDesc(taskId);
        
        return ReassignTaskResponse.fromReassignment(
                savedAssignment, task, previousTechnicianId, reason, workload, reassignedBy, taskHistory);
    }
    
    /**
     * Builds the Sort object based on sortBy and sortOrder parameters.
     * Default sorting is by priority (desc) and then by createdAt (desc).
     * 
     * @param sortBy the field to sort by (priority, createdAt, status)
     * @param sortOrder the sort order (asc, desc)
     * @return Sort object for the query
     */
    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        String sortField;
        switch (sortBy != null ? sortBy.toLowerCase() : "priority") {
            case "createdat":
                sortField = "createdAt";
                break;
            case "status":
                sortField = "status";
                break;
            case "priority":
            default:
                sortField = "priority";
                break;
        }
        
        // For priority sorting, add secondary sort by createdAt desc
        if ("priority".equals(sortField)) {
            return Sort.by(direction, sortField).and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        
        return Sort.by(direction, sortField);
    }
    
    /**
     * Gets the count of tasks for each status.
     * 
     * @return Map with status names as keys and counts as values
     */
    private Map<String, Long> getStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            counts.put(status.name(), taskRepository.countByStatus(status));
        }
        return counts;
    }
}
