package com.fsm.task.application.service;

import com.fsm.task.application.dto.AssignTaskRequest;
import com.fsm.task.application.dto.AssignTaskResponse;
import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.domain.model.Assignment;
import com.fsm.task.domain.model.Assignment.AssignmentStatus;
import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.repository.AssignmentRepository;
import com.fsm.task.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

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
     * Assigns a task to a technician.
     * Creates an assignment record and updates task status.
     * Returns a warning if technician workload exceeds threshold.
     * 
     * Domain Invariants:
     * - Only UNASSIGNED or ASSIGNED tasks can be (re)assigned
     * - Task must exist
     * - Technician must exist and be active (validated at controller level or via external service)
     * - Assignment creates entry in assignment history
     * 
     * @param taskId the ID of the task to assign
     * @param request the assignment request containing technician ID
     * @param assignedBy the username/email of the authenticated user making the assignment
     * @return AssignTaskResponse with assignment details and workload warning if applicable
     * @throws IllegalArgumentException if task not found or cannot be assigned
     */
    @Transactional
    public AssignTaskResponse assignTask(Long taskId, AssignTaskRequest request, String assignedBy) {
        log.info("Assigning task {} to technician {} by user: {}", taskId, request.getTechnicianId(), assignedBy);
        
        // Find the task
        ServiceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found with ID: {}", taskId);
                    return new IllegalArgumentException("Task not found with ID: " + taskId);
                });
        
        // Validate task can be assigned
        if (!task.canBeAssigned()) {
            log.warn("Task {} cannot be assigned. Current status: {}", taskId, task.getStatus());
            throw new IllegalArgumentException("Task cannot be assigned. Current status: " + task.getStatus());
        }
        
        Long technicianId = request.getTechnicianId();
        
        // If task is already assigned, handle reassignment
        if (task.isAssigned()) {
            // Mark current assignment as reassigned
            Optional<Assignment> currentAssignment = assignmentRepository
                    .findByTaskIdAndStatus(taskId, AssignmentStatus.ACTIVE);
            currentAssignment.ifPresent(assignment -> {
                assignment.markAsReassigned("Reassigned to technician " + technicianId);
                assignmentRepository.save(assignment);
                log.info("Previous assignment {} marked as reassigned", assignment.getId());
            });
            
            // Reassign the task
            task.reassignToTechnician(technicianId);
        } else {
            // Assign the task
            task.assignToTechnician(technicianId);
        }
        
        // Save updated task
        ServiceTask savedTask = taskRepository.save(task);
        
        // Create new assignment record
        Assignment assignment = Assignment.builder()
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment created with ID: {} for task: {}", savedAssignment.getId(), taskId);
        
        // Get technician workload (after this assignment)
        int workload = assignmentRepository.getTechnicianWorkload(technicianId);
        log.info("Technician {} current workload: {}", technicianId, workload);
        
        return AssignTaskResponse.fromAssignment(savedAssignment, savedTask, workload, assignedBy);
    }
}
