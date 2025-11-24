package com.fsm.identity.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.identity.application.dto.LoginRequest;
import com.fsm.identity.application.dto.LoginResponse;
import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.RoleEntity;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.repository.RoleRepository;
import com.fsm.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for login endpoint
 * Tests the complete authentication flow from request to response
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class LoginIntegrationTest {
    
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
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Clean up existing test user
        userRepository.findByEmail("integration@example.com").ifPresent(userRepository::delete);
        
        // Create test user with hashed password
        RoleEntity adminRole = roleRepository.findByName(Role.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        
        testUser = User.builder()
                .name("Integration Test User")
                .email("integration@example.com")
                .password(passwordEncoder.encode("testPassword123"))
                .role(adminRole)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        userRepository.save(testUser);
    }
    
    @Test
    void testCompleteLoginFlowSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("integration@example.com")
                .password("testPassword123")
                .mobile(false)
                .build();
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value("Integration Test User"))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();
        
        // Verify token was generated
        String responseBody = result.getResponse().getContentAsString();
        LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);
        
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isEmpty());
        assertTrue(response.getToken().contains("."), "JWT token should have dots separating header, payload, and signature");
    }
    
    @Test
    void testLoginWithWrongPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("integration@example.com")
                .password("wrongPassword")
                .build();
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
    
    @Test
    void testLoginWithNonExistentUser() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
    
    @Test
    void testLoginWithInactiveUser() throws Exception {
        // Deactivate the user
        testUser.deactivate();
        userRepository.save(testUser);
        
        LoginRequest request = LoginRequest.builder()
                .email("integration@example.com")
                .password("testPassword123")
                .build();
        
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User account is inactive"));
    }
    
    @Test
    void testLoginWithMobileFlag() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("integration@example.com")
                .password("testPassword123")
                .mobile(true)
                .build();
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        
        // Verify token was generated
        String responseBody = result.getResponse().getContentAsString();
        LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);
        
        assertNotNull(response.getToken());
    }
    
    @Test
    void testPasswordNotReturnedInResponse() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("integration@example.com")
                .password("testPassword123")
                .build();
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        
        // Verify password is not in response
        assertFalse(responseBody.contains("password"));
        assertFalse(responseBody.contains("$2a$10"));
    }
}
