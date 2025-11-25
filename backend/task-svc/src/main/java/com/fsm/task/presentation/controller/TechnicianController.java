package com.fsm.task.presentation.controller;

import com.fsm.task.application.dto.TechnicianTaskListResponse;
import com.fsm.task.application.service.TaskService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for technician-specific operations.
 * Provides endpoints for technicians to view and manage their assigned tasks.
 */
@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Technicians", description = "Technician operations API endpoints")
public class TechnicianController {
    
    private final TaskService taskService;
    
    /**
     * Retrieves tasks assigned to the authenticated technician.
     * Supports filtering by status and returns tasks ordered by priority (HIGH first).
     * Completed tasks from previous days are excluded from the results.
     * 
     * Domain Invariants:
     * - Only returns tasks assigned to the authenticated technician
     * - Completed tasks from previous days are not shown
     * - Tasks are ordered by priority for easy identification
     * 
     * @param status filter by task status (all, assigned, in_progress, completed)
     * @return ResponseEntity with the list of technician's tasks
     */
    @GetMapping("/me/tasks")
    @RequireRole({Role.TECHNICIAN})
    @Operation(
            summary = "Get technician's assigned tasks",
            description = "Retrieves tasks assigned to the authenticated technician. " +
                    "Supports filtering by status (all, assigned, in_progress, completed). " +
                    "Tasks are sorted by priority (HIGH first), then by assigned time. " +
                    "Completed tasks from previous days are excluded.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TechnicianTaskListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - only TECHNICIAN role can access this endpoint",
                    content = @Content
            )
    })
    public ResponseEntity<TechnicianTaskListResponse> getTechnicianTasks(
            @Parameter(description = "Filter by task status (all, assigned, in_progress, completed)", example = "all")
            @RequestParam(required = false, defaultValue = "all") String status
    ) {
        Long technicianId = getAuthenticatedTechnicianId();
        log.info("Received request to get tasks for technician {} with status filter: {}", technicianId, status);
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets the technician ID of the authenticated user from the security context.
     * Extracts the technician ID from the authentication principal.
     * 
     * PRODUCTION NOTE: In a real production environment, this would extract the 
     * technician ID from JWT token claims (e.g., a "technicianId" claim). The current
     * implementation uses a convention-based approach for development/testing.
     * 
     * Convention: Username should be in format "technician_{id}" (e.g., "technician_101")
     * or the ID will be derived from the username.
     * 
     * @return the authenticated technician's ID
     * @throws IllegalStateException if the user is not authenticated
     */
    private Long getAuthenticatedTechnicianId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User must be authenticated to access technician tasks");
        }
        
        // In production, this would extract technician ID from JWT claims
        // For now, we use a convention-based approach
        String username = authentication.getName();
        
        // Try to extract numeric ID from username pattern "technician_{id}"
        if (username.startsWith("technician_")) {
            try {
                String idPart = username.substring("technician_".length());
                return Long.parseLong(idPart);
            } catch (NumberFormatException e) {
                log.debug("Could not parse technician ID from username: {}", username);
            }
        }
        
        // For other username formats (e.g., "tech_user_42"), try extracting the last numeric part
        if (username.contains("_")) {
            try {
                String idPart = username.substring(username.lastIndexOf("_") + 1);
                return Long.parseLong(idPart);
            } catch (NumberFormatException e) {
                log.debug("Could not parse numeric ID from username: {}", username);
            }
        }
        
        // Fallback: Use a deterministic ID based on username
        // WARNING: This is for DEVELOPMENT/TESTING ONLY!
        // Hash collisions could allow unauthorized access to another user's tasks.
        // TODO: In production, remove this fallback and require proper JWT token claims
        // with the technician ID. Throw an exception if technician ID cannot be determined.
        long hashBasedId = Math.abs((long) username.hashCode());
        log.warn("Using hash-based technician ID {} for username: {}. This is for development only!", 
                hashBasedId, username);
        return hashBasedId;
    }
}
