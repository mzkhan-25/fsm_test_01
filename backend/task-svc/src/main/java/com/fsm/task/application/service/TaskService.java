package com.fsm.task.application.service;

import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for task management operations.
 * Handles business logic for creating and managing service tasks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    
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
}
