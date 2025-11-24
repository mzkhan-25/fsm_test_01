package com.fsm.identity.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.identity.application.dto.UserRequest;
import com.fsm.identity.application.dto.UserResponse;
import com.fsm.identity.application.dto.UserUpdateRequest;
import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.RoleEntity;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.repository.RoleRepository;
import com.fsm.identity.domain.repository.UserRepository;
import com.fsm.identity.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for user management endpoints
 * Tests the complete CRUD flow for user management
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("User Management Integration Tests")
class UserManagementIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private String adminToken;
    private String supervisorToken;
    private String technicianToken;
    private User testAdmin;
    private User testSupervisor;
    private User testTechnician;
    
    @BeforeEach
    void setUp() {
        // Clean up existing test users
        userRepository.findByEmail("admin@test.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("supervisor@test.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("technician@test.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("newuser@test.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("updated@test.com").ifPresent(userRepository::delete);
        
        // Create test users with different roles
        RoleEntity adminRole = roleRepository.findByName(Role.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        RoleEntity supervisorRole = roleRepository.findByName(Role.SUPERVISOR)
                .orElseThrow(() -> new RuntimeException("SUPERVISOR role not found"));
        RoleEntity technicianRole = roleRepository.findByName(Role.TECHNICIAN)
                .orElseThrow(() -> new RuntimeException("TECHNICIAN role not found"));
        
        testAdmin = User.builder()
                .name("Test Admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(adminRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        testAdmin = userRepository.save(testAdmin);
        
        testSupervisor = User.builder()
                .name("Test Supervisor")
                .email("supervisor@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(supervisorRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        testSupervisor = userRepository.save(testSupervisor);
        
        testTechnician = User.builder()
                .name("Test Technician")
                .email("technician@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        testTechnician = userRepository.save(testTechnician);
        
        // Generate JWT tokens for test users
        adminToken = jwtUtil.generateToken(
                testAdmin.getId(),
                testAdmin.getEmail(),
                testAdmin.getName(),
                testAdmin.getRole().getName(),
                false
        );
        
        supervisorToken = jwtUtil.generateToken(
                testSupervisor.getId(),
                testSupervisor.getEmail(),
                testSupervisor.getName(),
                testSupervisor.getRole().getName(),
                false
        );
        
        technicianToken = jwtUtil.generateToken(
                testTechnician.getId(),
                testTechnician.getEmail(),
                testTechnician.getName(),
                testTechnician.getRole().getName(),
                false
        );
    }
    
    @Test
    @DisplayName("Should create user successfully as ADMIN")
    void shouldCreateUserAsAdmin() throws Exception {
        // Given
        UserRequest request = UserRequest.builder()
                .name("New User")
                .email("newuser@test.com")
                .phone("+12025551234")
                .password("password123")
                .role(Role.TECHNICIAN)
                .build();
        
        // When/Then
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.role").value("TECHNICIAN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    
    @Test
    @DisplayName("Should not create user as SUPERVISOR")
    void shouldNotCreateUserAsSupervisor() throws Exception {
        // Given
        UserRequest request = UserRequest.builder()
                .name("New User")
                .email("newuser@test.com")
                .password("password123")
                .role(Role.TECHNICIAN)
                .build();
        
        // When/Then
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should return 400 when creating user with existing email")
    void shouldReturn400WhenCreatingUserWithExistingEmail() throws Exception {
        // Given
        UserRequest request = UserRequest.builder()
                .name("Duplicate Email User")
                .email("technician@test.com")  // Already exists
                .password("password123")
                .role(Role.TECHNICIAN)
                .build();
        
        // When/Then
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }
    
    @Test
    @DisplayName("Should get all users as ADMIN")
    void shouldGetAllUsersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].email", hasItem("admin@test.com")))
                .andExpect(jsonPath("$[*].email", hasItem("supervisor@test.com")))
                .andExpect(jsonPath("$[*].email", hasItem("technician@test.com")));
    }
    
    @Test
    @DisplayName("Should get all users as SUPERVISOR")
    void shouldGetAllUsersAsSupervisor() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }
    
    @Test
    @DisplayName("Should not get all users as TECHNICIAN")
    void shouldNotGetAllUsersAsTechnician() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should get user by ID as any authenticated user")
    void shouldGetUserByIdAsAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/users/" + testTechnician.getId())
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTechnician.getId()))
                .andExpect(jsonPath("$.name").value("Test Technician"))
                .andExpect(jsonPath("$.email").value("technician@test.com"));
    }
    
    @Test
    @DisplayName("Should return 404 when getting non-existent user")
    void shouldReturn404WhenGettingNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }
    
    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("Updated Technician")
                .phone("+12025559999")
                .build();
        
        // When/Then
        mockMvc.perform(put("/api/users/" + testTechnician.getId())
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTechnician.getId()))
                .andExpect(jsonPath("$.name").value("Updated Technician"))
                .andExpect(jsonPath("$.phone").value("+12025559999"));
    }
    
    @Test
    @DisplayName("Should update user email successfully")
    void shouldUpdateUserEmailSuccessfully() throws Exception {
        // Given
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("updated@test.com")
                .build();
        
        // When/Then
        mockMvc.perform(put("/api/users/" + testTechnician.getId())
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }
    
    @Test
    @DisplayName("Should return 400 when updating email to existing one")
    void shouldReturn400WhenUpdatingEmailToExistingOne() throws Exception {
        // Given
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("admin@test.com")  // Already exists
                .build();
        
        // When/Then
        mockMvc.perform(put("/api/users/" + testTechnician.getId())
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }
    
    @Test
    @DisplayName("Should deactivate user successfully as ADMIN")
    void shouldDeactivateUserAsAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + testTechnician.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        
        // Verify user is deactivated
        User deactivatedUser = userRepository.findById(testTechnician.getId())
                .orElseThrow();
        assert deactivatedUser.getStatus() == User.UserStatus.INACTIVE;
    }
    
    @Test
    @DisplayName("Should not deactivate user as TECHNICIAN")
    void shouldNotDeactivateUserAsTechnician() throws Exception {
        mockMvc.perform(delete("/api/users/" + testSupervisor.getId())
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should return 404 when deactivating non-existent user")
    void shouldReturn404WhenDeactivatingNonExistentUser() throws Exception {
        mockMvc.perform(delete("/api/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }
    
}
