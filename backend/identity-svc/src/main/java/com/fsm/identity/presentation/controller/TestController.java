package com.fsm.identity.presentation.controller;

import com.fsm.identity.domain.model.Role;
import com.fsm.identity.infrastructure.security.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for demonstrating role-based access control.
 * Provides endpoints with different role requirements.
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
@Tag(name = "Test", description = "Test endpoints for RBAC demonstration")
public class TestController {

    /**
     * Public endpoint accessible to all authenticated users
     */
    @GetMapping("/public")
    @Operation(summary = "Public endpoint", description = "Accessible to all authenticated users")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint accessible to all authenticated users");
    }

    /**
     * Admin-only endpoint
     */
    @GetMapping("/admin")
    @RequireRole(Role.ADMIN)
    @Operation(summary = "Admin endpoint", description = "Accessible only to ADMIN role")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("This is an admin-only endpoint");
    }

    /**
     * Dispatcher-only endpoint
     */
    @GetMapping("/dispatcher")
    @RequireRole(Role.DISPATCHER)
    @Operation(summary = "Dispatcher endpoint", description = "Accessible only to DISPATCHER role")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> dispatcherEndpoint() {
        return ResponseEntity.ok("This is a dispatcher-only endpoint");
    }

    /**
     * Technician-only endpoint
     */
    @GetMapping("/technician")
    @RequireRole(Role.TECHNICIAN)
    @Operation(summary = "Technician endpoint", description = "Accessible only to TECHNICIAN role")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> technicianEndpoint() {
        return ResponseEntity.ok("This is a technician-only endpoint");
    }

    /**
     * Endpoint accessible to both DISPATCHER and SUPERVISOR
     */
    @GetMapping("/dispatcher-supervisor")
    @RequireRole({Role.DISPATCHER, Role.SUPERVISOR})
    @Operation(summary = "Dispatcher/Supervisor endpoint", 
               description = "Accessible to DISPATCHER and SUPERVISOR roles")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> dispatcherSupervisorEndpoint() {
        return ResponseEntity.ok("This endpoint is accessible to dispatchers and supervisors");
    }
}
