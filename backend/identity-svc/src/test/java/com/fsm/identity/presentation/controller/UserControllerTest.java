package com.fsm.identity.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.identity.application.dto.UserRequest;
import com.fsm.identity.application.dto.UserResponse;
import com.fsm.identity.application.dto.UserUpdateRequest;
import com.fsm.identity.application.service.UserService;
import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 */
@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {UserController.class, UserControllerTest.TestSecurityConfig.class})
@DisplayName("UserController Tests")
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    private UserRequest userRequest;
    private UserResponse userResponse;
    private UserUpdateRequest updateRequest;
    
    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            // CSRF disabled for test - mimics production config for stateless API
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
    
    @BeforeEach
    void setUp() {
        userRequest = UserRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .phone("+12025551234")
                .password("password123")
                .role(Role.TECHNICIAN)
                .build();
        
        userResponse = UserResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .phone("+12025551234")
                .role(Role.TECHNICIAN.name())
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        updateRequest = UserUpdateRequest.builder()
                .name("Updated User")
                .build();
    }
    
    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() throws Exception {
        // Given
        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);
        
        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("TECHNICIAN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        
        verify(userService).createUser(any(UserRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 when creating user with invalid email")
    void shouldReturn400WhenCreatingUserWithInvalidEmail() throws Exception {
        // Given
        userRequest.setEmail("invalid-email");
        
        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
        
        verify(userService, never()).createUser(any(UserRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 when creating user with missing name")
    void shouldReturn400WhenCreatingUserWithMissingName() throws Exception {
        // Given
        userRequest.setName("");
        
        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
        
        verify(userService, never()).createUser(any(UserRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 when creating user with missing password")
    void shouldReturn400WhenCreatingUserWithMissingPassword() throws Exception {
        // Given
        userRequest.setPassword("");
        
        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
        
        verify(userService, never()).createUser(any(UserRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 when creating user with missing role")
    void shouldReturn400WhenCreatingUserWithMissingRole() throws Exception {
        // Given
        userRequest.setRole(null);
        
        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
        
        verify(userService, never()).createUser(any(UserRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 when email already exists")
    void shouldReturn400WhenEmailAlreadyExists() throws Exception {
        // Given
        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));
        
        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
        
        verify(userService).createUser(any(UserRequest.class));
    }
    
    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsersSuccessfully() throws Exception {
        // Given
        UserResponse user2 = UserResponse.builder()
                .id(2L)
                .name("User 2")
                .email("user2@example.com")
                .role(Role.ADMIN.name())
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        List<UserResponse> users = Arrays.asList(userResponse, user2);
        when(userService.getAllUsers()).thenReturn(users);
        
        // When/Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test User"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("User 2"));
        
        verify(userService).getAllUsers();
    }
    
    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(userResponse);
        
        // When/Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
        
        verify(userService).getUserById(1L);
    }
    
    @Test
    @DisplayName("Should return 404 when user not found by ID")
    void shouldReturn404WhenUserNotFoundById() throws Exception {
        // Given
        when(userService.getUserById(999L))
                .thenThrow(new IllegalArgumentException("User not found with id: 999"));
        
        // When/Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
        
        verify(userService).getUserById(999L);
    }
    
    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .name("Updated User")
                .email("test@example.com")
                .phone("+12025551234")
                .role(Role.TECHNICIAN.name())
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(updatedResponse);
        
        // When/Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated User"));
        
        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }
    
    @Test
    @DisplayName("Should return 404 when updating non-existent user")
    void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
        // Given
        when(userService.updateUser(eq(999L), any(UserUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("User not found with id: 999"));
        
        // When/Then
        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
        
        verify(userService).updateUser(eq(999L), any(UserUpdateRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 when updating with invalid email")
    void shouldReturn400WhenUpdatingWithInvalidEmail() throws Exception {
        // Given
        updateRequest.setEmail("invalid-email");
        
        // When/Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
        
        verify(userService, never()).updateUser(any(), any(UserUpdateRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 when updating with invalid phone")
    void shouldReturn400WhenUpdatingWithInvalidPhone() throws Exception {
        // Given
        updateRequest.setPhone("invalid-phone");
        
        // When/Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
        
        verify(userService, never()).updateUser(any(), any(UserUpdateRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 when updating email to existing one")
    void shouldReturn400WhenUpdatingEmailToExistingOne() throws Exception {
        // Given
        updateRequest.setEmail("existing@example.com");
        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));
        
        // When/Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
        
        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }
    
    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUserSuccessfully() throws Exception {
        // Given
        doNothing().when(userService).deactivateUser(1L);
        
        // When/Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
        
        verify(userService).deactivateUser(1L);
    }
    
    @Test
    @DisplayName("Should return 404 when deactivating non-existent user")
    void shouldReturn404WhenDeactivatingNonExistentUser() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("User not found with id: 999"))
                .when(userService).deactivateUser(999L);
        
        // When/Then
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
        
        verify(userService).deactivateUser(999L);
    }
}
