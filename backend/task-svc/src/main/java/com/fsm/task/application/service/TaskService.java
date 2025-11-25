package com.fsm.task.application.service;

import com.fsm.task.application.dto.AssignTaskRequest;
import com.fsm.task.application.dto.AssignTaskResponse;
import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.TaskListRequest;
import com.fsm.task.application.dto.TaskListResponse;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.application.dto.TechnicianTaskListResponse;
import com.fsm.task.application.dto.TechnicianTaskResponse;
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

import java.time.LocalDate;
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
    
    /**
     * Retrieves tasks assigned to a specific technician.
     * Filters by status if provided, and excludes completed tasks from previous days.
     * Tasks are sorted by priority (HIGH first) and then by assigned time.
     * 
     * Domain Invariants:
     * - Only return tasks assigned to the authenticated technician
     * - Completed tasks from previous days are not shown
     * - Tasks ordered by priority for easy identification
     * 
     * @param technicianId the ID of the authenticated technician
     * @param status optional status filter (all, assigned, in_progress, completed)
     * @return TechnicianTaskListResponse with the technician's tasks
     */
    @Transactional(readOnly = true)
    public TechnicianTaskListResponse getTechnicianTasks(Long technicianId, String status) {
        log.info("Fetching tasks for technician {} with status filter: {}", technicianId, status);
        
        List<ServiceTask> tasks;
        TaskStatus statusFilter = parseStatusFilter(status);
        
        if (statusFilter != null) {
            tasks = taskRepository.findByTechnicianIdAndStatusOrderedByPriority(technicianId, statusFilter);
        } else {
            tasks = taskRepository.findByTechnicianIdOrderedByPriority(technicianId);
        }
        
        // Filter out completed tasks from previous days
        LocalDate today = LocalDate.now();
        tasks = tasks.stream()
                .filter(task -> {
                    if (task.getStatus() == TaskStatus.COMPLETED) {
                        // Get assigned date from assignment if available, else use createdAt
                        Optional<Assignment> assignment = assignmentRepository.findByTaskIdAndStatus(
                                task.getId(), AssignmentStatus.COMPLETED);
                        LocalDate completionDate = assignment
                                .map(a -> a.getAssignedAt().toLocalDate())
                                .orElse(task.getCreatedAt().toLocalDate());
                        return !completionDate.isBefore(today);
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // Convert to DTOs with assignment info
        List<TechnicianTaskResponse> taskResponses = tasks.stream()
                .map(task -> {
                    // Get the assigned at timestamp from the assignment
                    Optional<Assignment> activeAssignment = assignmentRepository.findActiveAssignmentForTask(task.getId());
                    LocalDateTime assignedAt = activeAssignment
                            .map(Assignment::getAssignedAt)
                            .orElse(task.getCreatedAt());
                    return TechnicianTaskResponse.fromEntity(task, assignedAt);
                })
                .collect(Collectors.toList());
        
        log.info("Found {} tasks for technician {}", taskResponses.size(), technicianId);
        
        return TechnicianTaskListResponse.builder()
                .tasks(taskResponses)
                .totalTasks(taskResponses.size())
                .build();
    }
    
    /**
     * Parses the status filter string to a TaskStatus enum.
     * 
     * @param status the status filter string (all, assigned, in_progress, completed)
     * @return TaskStatus enum or null if "all" or invalid
     */
    private TaskStatus parseStatusFilter(String status) {
        if (status == null || status.isEmpty() || "all".equalsIgnoreCase(status)) {
            return null;
        }
        
        try {
            return TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status filter: {}. Returning all tasks.", status);
            return null;
        }
    }
}
