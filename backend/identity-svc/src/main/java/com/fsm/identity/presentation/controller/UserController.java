package com.fsm.identity.presentation.controller;

import com.fsm.identity.application.dto.UserRequest;
import com.fsm.identity.application.dto.UserResponse;
import com.fsm.identity.application.dto.UserUpdateRequest;
import com.fsm.identity.application.service.UserService;
import com.fsm.identity.domain.model.Role;
import com.fsm.identity.infrastructure.security.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management controller handling CRUD operations for users.
 * Implements role-based access control for all endpoints.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User CRUD operations API")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Create a new user (ADMIN only)
     * 
     * @param userRequest User creation request
     * @return Created user response
     */
    @PostMapping
    @RequireRole(Role.ADMIN)
    @Operation(summary = "Create new user", description = "Create a new user in the system (ADMIN only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or email already exists",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions",
                    content = @Content)
    })
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest userRequest) {
        try {
            UserResponse response = userService.createUser(userRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Get all users (ADMIN, SUPERVISOR)
     * 
     * @return List of all users
     */
    @GetMapping
    @RequireRole({Role.ADMIN, Role.SUPERVISOR})
    @Operation(summary = "Get all users", description = "Retrieve all users in the system (ADMIN, SUPERVISOR)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions",
                    content = @Content)
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return User response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by ID (all authenticated users)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponse response = userService.getUserById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Update user
     * 
     * @param id User ID
     * @param updateRequest User update request
     * @return Updated user response
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user details (all authenticated users can update)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or email already exists",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    public ResponseEntity<?> updateUser(@PathVariable Long id, 
                                        @Valid @RequestBody UserUpdateRequest updateRequest) {
        try {
            UserResponse response = userService.updateUser(id, updateRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update user: {}", e.getMessage());
            HttpStatus status = e.getMessage().contains("not found") ? 
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Deactivate user (soft delete - ADMIN only)
     * Sets user status to INACTIVE, preserving historical data.
     * 
     * @param id User ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @RequireRole(Role.ADMIN)
    @Operation(summary = "Deactivate user", description = "Deactivate user (set status to inactive) - ADMIN only")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deactivated successfully",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to deactivate user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Register device token for push notifications
     * 
     * @param id User ID
     * @param request Device token request
     * @return No content
     */
    @PostMapping("/{id}/device-token")
    @Operation(summary = "Register device token", description = "Register a device token for push notifications")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Device token registered successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    public ResponseEntity<?> registerDeviceToken(@PathVariable Long id, 
                                                  @RequestBody DeviceTokenRequest request) {
        try {
            userService.registerDeviceToken(id, request.deviceToken());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to register device token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * Device token request DTO
     */
    private record DeviceTokenRequest(String deviceToken) {
    }
    
    /**
     * Error response DTO for consistent error handling
     */
    private record ErrorResponse(String message) {
    }
}
