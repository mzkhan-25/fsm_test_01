package com.fsm.identity.application.service;

import com.fsm.identity.application.dto.UserRequest;
import com.fsm.identity.application.dto.UserResponse;
import com.fsm.identity.application.dto.UserUpdateRequest;
import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.RoleEntity;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.repository.RoleRepository;
import com.fsm.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private RoleEntity adminRole;
    private RoleEntity technicianRole;
    private User testUser;
    private UserRequest userRequest;
    
    @BeforeEach
    void setUp() {
        adminRole = RoleEntity.builder()
                .id(1L)
                .name(Role.ADMIN)
                .description("Administrator")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        technicianRole = RoleEntity.builder()
                .id(2L)
                .name(Role.TECHNICIAN)
                .description("Technician")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .phone("+12025551234")
                .password("hashed_password")
                .role(adminRole)
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        userRequest = UserRequest.builder()
                .name("New User")
                .email("newuser@example.com")
                .phone("+12025555678")
                .password("password123")
                .role(Role.TECHNICIAN)
                .build();
    }
    
    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName(Role.TECHNICIAN)).thenReturn(Optional.of(technicianRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        
        User savedUser = User.builder()
                .id(2L)
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .phone(userRequest.getPhone())
                .password("hashed_password")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserResponse response = userService.createUser(userRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo(userRequest.getName());
        assertThat(response.getEmail()).isEqualTo(userRequest.getEmail());
        assertThat(response.getRole()).isEqualTo(Role.TECHNICIAN.name());
        assertThat(response.getStatus()).isEqualTo(User.UserStatus.ACTIVE);
        
        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(roleRepository).findByName(Role.TECHNICIAN);
        verify(passwordEncoder).encode(userRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should throw exception when creating user with existing email")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
        
        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when role not found")
    void shouldThrowExceptionWhenRoleNotFound() {
        // Given
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName(Role.TECHNICIAN)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
        
        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(roleRepository).findByName(Role.TECHNICIAN);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsersSuccessfully() {
        // Given
        User user2 = User.builder()
                .id(2L)
                .name("User 2")
                .email("user2@example.com")
                .password("hashed_password")
                .role(technicianRole)
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);
        
        // When
        List<UserResponse> responses = userService.getAllUsers();
        
        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
        
        verify(userRepository).findAll();
    }
    
    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When
        UserResponse response = userService.getUserById(1L);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo(testUser.getName());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        
        verify(userRepository).findById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with id: 999");
        
        verify(userRepository).findById(999L);
    }
    
    @Test
    @DisplayName("Should update user name successfully")
    void shouldUpdateUserNameSuccessfully() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .name("Updated Name")
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserResponse response = userService.updateUser(1L, updateRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(testUser.getName()).isEqualTo("Updated Name");
        
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }
    
    @Test
    @DisplayName("Should update user email successfully")
    void shouldUpdateUserEmailSuccessfully() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .email("newemail@example.com")
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserResponse response = userService.updateUser(1L, updateRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(testUser.getEmail()).isEqualTo("newemail@example.com");
        
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("newemail@example.com");
        verify(userRepository).save(testUser);
    }
    
    @Test
    @DisplayName("Should throw exception when updating email to existing one")
    void shouldThrowExceptionWhenUpdatingToExistingEmail() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .email("existing@example.com")
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        // When/Then
        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
        
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should not validate email uniqueness when email unchanged")
    void shouldNotValidateEmailWhenUnchanged() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .email(testUser.getEmail())  // Same email
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserResponse response = userService.updateUser(1L, updateRequest);
        
        // Then
        assertThat(response).isNotNull();
        
        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(testUser);
    }
    
    @Test
    @DisplayName("Should update user password successfully")
    void shouldUpdateUserPasswordSuccessfully() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .password("newpassword123")
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new_hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserResponse response = userService.updateUser(1L, updateRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(testUser.getPassword()).isEqualTo("new_hashed_password");
        
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).save(testUser);
    }
    
    @Test
    @DisplayName("Should update user role successfully")
    void shouldUpdateUserRoleSuccessfully() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .role(Role.TECHNICIAN)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(Role.TECHNICIAN)).thenReturn(Optional.of(technicianRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        UserResponse response = userService.updateUser(1L, updateRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(testUser.getRole()).isEqualTo(technicianRole);
        
        verify(userRepository).findById(1L);
        verify(roleRepository).findByName(Role.TECHNICIAN);
        verify(userRepository).save(testUser);
    }
    
    @Test
    @DisplayName("Should throw exception when updating role to non-existent role")
    void shouldThrowExceptionWhenUpdatingToNonExistentRole() {
        // Given
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .role(Role.DISPATCHER)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(Role.DISPATCHER)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role not found");
        
        verify(userRepository).findById(1L);
        verify(roleRepository).findByName(Role.DISPATCHER);
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUserSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        userService.deactivateUser(1L);
        
        // Then
        assertThat(testUser.getStatus()).isEqualTo(User.UserStatus.INACTIVE);
        
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }
    
    @Test
    @DisplayName("Should throw exception when deactivating non-existent user")
    void shouldThrowExceptionWhenDeactivatingNonExistentUser() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userService.deactivateUser(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with id: 999");
        
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }
}
