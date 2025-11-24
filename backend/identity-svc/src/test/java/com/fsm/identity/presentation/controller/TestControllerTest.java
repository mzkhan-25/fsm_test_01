package com.fsm.identity.presentation.controller;

import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.RoleEntity;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.repository.RoleRepository;
import com.fsm.identity.domain.repository.UserRepository;
import com.fsm.identity.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for TestController RBAC functionality
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String dispatcherToken;
    private String supervisorToken;
    private String technicianToken;

    @BeforeEach
    void setUp() {
        // Create test users with different roles
        RoleEntity adminRole = roleRepository.findByName(Role.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
        RoleEntity dispatcherRole = roleRepository.findByName(Role.DISPATCHER)
                .orElseThrow(() -> new RuntimeException("Dispatcher role not found"));
        RoleEntity supervisorRole = roleRepository.findByName(Role.SUPERVISOR)
                .orElseThrow(() -> new RuntimeException("Supervisor role not found"));
        RoleEntity technicianRole = roleRepository.findByName(Role.TECHNICIAN)
                .orElseThrow(() -> new RuntimeException("Technician role not found"));

        // Generate tokens
        adminToken = jwtUtil.generateToken(1L, "admin@test.com", "Admin User", Role.ADMIN, false);
        dispatcherToken = jwtUtil.generateToken(2L, "dispatcher@test.com", "Dispatcher User", Role.DISPATCHER, false);
        supervisorToken = jwtUtil.generateToken(3L, "supervisor@test.com", "Supervisor User", Role.SUPERVISOR, false);
        technicianToken = jwtUtil.generateToken(4L, "technician@test.com", "Technician User", Role.TECHNICIAN, false);
    }

    @Test
    void testPublicEndpointAccessibleToAllAuthenticatedUsers() throws Exception {
        // All roles should be able to access public endpoint
        mockMvc.perform(get("/api/test/public")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a public endpoint accessible to all authenticated users"));

        mockMvc.perform(get("/api/test/public")
                        .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test/public")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test/public")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk());
    }

    @Test
    void testPublicEndpointRequiresAuthentication() throws Exception {
        // Without token, Spring Security denies access (403 because it's configured with authenticated())
        // The public endpoint doesn't have @RequireRole, so RoleAuthorizationFilter doesn't run
        // and Spring Security's default filter chain handles it with 403 for anonymous users
        mockMvc.perform(get("/api/test/public"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminEndpointAccessibleToAdmin() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("This is an admin-only endpoint"));
    }

    @Test
    void testAdminEndpointForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDispatcherEndpointAccessibleToDispatcher() throws Exception {
        mockMvc.perform(get("/api/test/dispatcher")
                        .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a dispatcher-only endpoint"));
    }

    @Test
    void testDispatcherEndpointAccessibleToAdmin() throws Exception {
        // Admin should have access to everything
        mockMvc.perform(get("/api/test/dispatcher")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testDispatcherEndpointForbiddenForOtherRoles() throws Exception {
        mockMvc.perform(get("/api/test/dispatcher")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/dispatcher")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testTechnicianEndpointAccessibleToTechnician() throws Exception {
        mockMvc.perform(get("/api/test/technician")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a technician-only endpoint"));
    }

    @Test
    void testTechnicianEndpointAccessibleToAdmin() throws Exception {
        mockMvc.perform(get("/api/test/technician")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testTechnicianEndpointForbiddenForOtherRoles() throws Exception {
        mockMvc.perform(get("/api/test/technician")
                        .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/test/technician")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDispatcherSupervisorEndpointAccessibleToBothRoles() throws Exception {
        mockMvc.perform(get("/api/test/dispatcher-supervisor")
                        .header("Authorization", "Bearer " + dispatcherToken))
                .andExpect(status().isOk())
                .andExpect(content().string("This endpoint is accessible to dispatchers and supervisors"));

        mockMvc.perform(get("/api/test/dispatcher-supervisor")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk());
    }

    @Test
    void testDispatcherSupervisorEndpointAccessibleToAdmin() throws Exception {
        mockMvc.perform(get("/api/test/dispatcher-supervisor")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testDispatcherSupervisorEndpointForbiddenForTechnician() throws Exception {
        mockMvc.perform(get("/api/test/dispatcher-supervisor")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAllEndpointsRequireAuthentication() throws Exception {
        // Endpoints with @RequireRole return 401 when not authenticated
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/test/dispatcher"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/test/technician"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/test/dispatcher-supervisor"))
                .andExpect(status().isUnauthorized());
    }
}
