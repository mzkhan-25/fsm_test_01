package com.fsm.task.application.service;

import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.TaskListRequest;
import com.fsm.task.application.dto.TaskListResponse;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
