package com.fsm.task.presentation.controller;

import com.fsm.task.application.dto.AssignTaskRequest;
import com.fsm.task.application.dto.AssignTaskResponse;
import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.ReassignTaskRequest;
import com.fsm.task.application.dto.ReassignTaskResponse;
import com.fsm.task.application.dto.TaskListRequest;
import com.fsm.task.application.dto.TaskListResponse;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.application.service.TaskService;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import com.fsm.task.infrastructure.security.RequireRole;
import com.fsm.task.infrastructure.security.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for task management operations.
 * Provides endpoints for creating and managing service tasks.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tasks", description = "Task management API endpoints")
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * Retrieves a list of tasks with filtering, sorting, and pagination.
     * Supports filtering by status, priority, and search term.
     * Default sorting is by priority (desc) and createdAt (desc).
     * Default page size is 50.
     * 
     * @param status filter by task status (optional)
     * @param priority filter by task priority (optional)
     * @param search search term for title, id, and client address (optional)
     * @param sortBy field to sort by: priority, createdAt, status (default: priority)
     * @param sortOrder sort order: asc or desc (default: desc)
     * @param page page number, 0-based (default: 0)
     * @param pageSize number of items per page (default: 50, max: 100)
     * @return ResponseEntity with task list, pagination info, and status counts
     */
    @GetMapping
    @Operation(
            summary = "Get task list",
            description = "Retrieves tasks with optional filtering by status, priority, and search. " +
                    "Supports sorting by priority, createdAt, or status. " +
                    "Returns paginated results with status counts in metadata.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            )
    })
    public ResponseEntity<TaskListResponse> getTasks(
            @Parameter(description = "Filter by task status", example = "UNASSIGNED")
            @RequestParam(required = false) TaskStatus status,
            
            @Parameter(description = "Filter by task priority", example = "HIGH")
            @RequestParam(required = false) Priority priority,
            
            @Parameter(description = "Search term for title, id, and client address (case-insensitive)", example = "HVAC")
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Field to sort by: priority, createdAt, status", example = "priority")
            @RequestParam(required = false, defaultValue = "priority") String sortBy,
            
            @Parameter(description = "Sort order: asc or desc", example = "desc")
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            
            @Parameter(description = "Number of items per page (max 100)", example = "50")
            @RequestParam(required = false, defaultValue = "50") Integer pageSize
    ) {
        log.info("Received request to get tasks - status: {}, priority: {}, search: {}", status, priority, search);
        
        // Validate and cap pageSize
        if (pageSize > 100) {
            pageSize = 100;
        }
        if (pageSize < 1) {
            pageSize = 50;
        }
        if (page < 0) {
            page = 0;
        }
        
        TaskListRequest request = TaskListRequest.builder()
                .status(status)
                .priority(priority)
                .search(search)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .pageSize(pageSize)
                .build();
        
        TaskListResponse response = taskService.getTasks(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Creates a new service task.
     * Only ADMIN and DISPATCHER roles are allowed to create tasks.
     * The task is created with UNASSIGNED status.
     * 
     * @param request the create task request with task details
     * @return ResponseEntity with created task and 201 status
     */
    @PostMapping
    @RequireRole({Role.ADMIN, Role.DISPATCHER})
    @Operation(
            summary = "Create a new task",
            description = "Creates a new service task. Only ADMIN and DISPATCHER roles can create tasks.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - validation failed",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content
            )
    })
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        String createdBy = getAuthenticatedUsername();
        log.info("Received request to create task from user: {}", createdBy);
        
        TaskResponse response = taskService.createTask(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Assigns a task to a technician.
     * Only ADMIN and DISPATCHER roles are allowed to assign tasks.
     * Creates an assignment record and updates task status to ASSIGNED.
     * Returns a warning if technician workload exceeds threshold (10 tasks).
     * 
     * @param taskId the ID of the task to assign
     * @param request the assignment request with technician ID
     * @return ResponseEntity with assignment details and 200 status
     */
    @PostMapping("/{taskId}/assign")
    @RequireRole({Role.ADMIN, Role.DISPATCHER})
    @Operation(
            summary = "Assign a task to a technician",
            description = "Assigns or reassigns a task to a technician. Only ADMIN and DISPATCHER roles can assign tasks. " +
                    "Returns a warning if technician workload exceeds 10 active tasks.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task assigned successfully",
                    content = @Content(schema = @Schema(implementation = AssignTaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - validation failed or task cannot be assigned",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content
            )
    })
    public ResponseEntity<AssignTaskResponse> assignTask(
            @Parameter(description = "ID of the task to assign", required = true)
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskRequest request) {
        String assignedBy = getAuthenticatedUsername();
        log.info("Received request to assign task {} to technician {} from user: {}", 
                taskId, request.getTechnicianId(), assignedBy);
        
        AssignTaskResponse response = taskService.assignTask(taskId, request, assignedBy);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reassigns a task to a different technician.
     * Only ADMIN and DISPATCHER roles are allowed to reassign tasks.
     * Creates a new assignment record and updates assignment history.
     * Returns a warning if new technician workload exceeds threshold (10 tasks).
     * 
     * Domain Invariants:
     * - Cannot reassign COMPLETED tasks
     * - IN_PROGRESS tasks require a reason for reassignment
     * - Assignment history preserves audit trail
     * 
     * @param taskId the ID of the task to reassign
     * @param request the reassignment request with new technician ID and optional reason
     * @return ResponseEntity with reassignment details and assignment history
     */
    @PostMapping("/{taskId}/reassign")
    @RequireRole({Role.ADMIN, Role.DISPATCHER})
    @Operation(
            summary = "Reassign a task to a different technician",
            description = "Reassigns a task to a different technician with reason tracking. Only ADMIN and DISPATCHER roles can reassign tasks. " +
                    "IN_PROGRESS tasks require a reason for reassignment. Cannot reassign COMPLETED tasks. " +
                    "Returns assignment history and a warning if new technician workload exceeds 10 active tasks.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task reassigned successfully",
                    content = @Content(schema = @Schema(implementation = ReassignTaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - validation failed, task cannot be reassigned, or reason required for IN_PROGRESS tasks",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content
            )
    })
    public ResponseEntity<ReassignTaskResponse> reassignTask(
            @Parameter(description = "ID of the task to reassign", required = true)
            @PathVariable Long taskId,
            @Valid @RequestBody ReassignTaskRequest request) {
        String reassignedBy = getAuthenticatedUsername();
        log.info("Received request to reassign task {} to technician {} from user: {}, reason: {}", 
                taskId, request.getNewTechnicianId(), reassignedBy, request.getReason());
        
        ReassignTaskResponse response = taskService.reassignTask(taskId, request, reassignedBy);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets the username of the authenticated user from the security context.
     * 
     * @return the authenticated username, or "anonymous" if not authenticated
     */
    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
