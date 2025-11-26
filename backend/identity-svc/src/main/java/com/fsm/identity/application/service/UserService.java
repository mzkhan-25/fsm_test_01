package com.fsm.identity.application.service;

import com.fsm.identity.application.dto.UserRequest;
import com.fsm.identity.application.dto.UserResponse;
import com.fsm.identity.application.dto.UserUpdateRequest;
import com.fsm.identity.domain.model.RoleEntity;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.repository.RoleRepository;
import com.fsm.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User management service handling CRUD operations for users.
 * Domain invariants:
 * - Email must be unique across all users
 * - Password must be hashed before storage
 * - Deactivation preserves historical data
 * - Cannot delete user with active task assignments (to be validated when task service exists)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Create a new user
     * 
     * @param userRequest User creation request
     * @return Created user response
     * @throws IllegalArgumentException if email already exists or role not found
     */
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating user with email: {}", userRequest.getEmail());
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("Email already exists: {}", userRequest.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Find role entity
        RoleEntity roleEntity = roleRepository.findByName(userRequest.getRole())
                .orElseThrow(() -> {
                    log.error("Role not found: {}", userRequest.getRole());
                    return new IllegalArgumentException("Role not found: " + userRequest.getRole());
                });
        
        // Hash password before storage
        String hashedPassword = passwordEncoder.encode(userRequest.getPassword());
        
        // Create user entity
        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .phone(userRequest.getPhone())
                .password(hashedPassword)
                .role(roleEntity)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        
        return mapToResponse(savedUser);
    }
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return User response
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new IllegalArgumentException("User not found with id: " + id);
                });
        return mapToResponse(user);
    }
    
    /**
     * Update user
     * 
     * @param id User ID
     * @param updateRequest User update request
     * @return Updated user response
     * @throws IllegalArgumentException if user not found or email already exists
     */
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest updateRequest) {
        log.info("Updating user with id: {}", id);
        
        // Find existing user
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new IllegalArgumentException("User not found with id: " + id);
                });
        
        // Update name if provided
        if (updateRequest.getName() != null && !updateRequest.getName().isBlank()) {
            user.setName(updateRequest.getName());
        }
        
        // Update email if provided and validate uniqueness
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isBlank()) {
            if (!user.getEmail().equals(updateRequest.getEmail())) {
                if (userRepository.existsByEmail(updateRequest.getEmail())) {
                    log.warn("Email already exists: {}", updateRequest.getEmail());
                    throw new IllegalArgumentException("Email already exists");
                }
                user.setEmail(updateRequest.getEmail());
            }
        }
        
        // Update phone if provided
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        
        // Update password if provided (hash it)
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isBlank()) {
            String hashedPassword = passwordEncoder.encode(updateRequest.getPassword());
            user.setPassword(hashedPassword);
        }
        
        // Update role if provided
        if (updateRequest.getRole() != null) {
            RoleEntity roleEntity = roleRepository.findByName(updateRequest.getRole())
                    .orElseThrow(() -> {
                        log.error("Role not found: {}", updateRequest.getRole());
                        return new IllegalArgumentException("Role not found: " + updateRequest.getRole());
                    });
            user.setRole(roleEntity);
        }
        
        // Save updated user
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());
        
        return mapToResponse(updatedUser);
    }
    
    /**
     * Deactivate user (soft delete)
     * Sets user status to INACTIVE, preserving historical data.
     * 
     * @param id User ID
     * @throws IllegalArgumentException if user not found
     * @throws IllegalStateException if user has active task assignments (when task service exists)
     */
    @Transactional
    public void deactivateUser(Long id) {
        log.info("Deactivating user with id: {}", id);
        
        // Find existing user
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new IllegalArgumentException("User not found with id: " + id);
                });
        
        // TODO: When task service is implemented, check for active task assignments
        // For now, we just log a note
        log.debug("Note: Check for active task assignments not yet implemented");
        
        // Deactivate user
        user.deactivate();
        userRepository.save(user);
        log.info("User deactivated successfully with id: {}", id);
    }
    
    /**
     * Register device token for push notifications
     * 
     * @param userId User ID
     * @param deviceToken Device token from FCM
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public void registerDeviceToken(Long userId, String deviceToken) {
        log.info("Registering device token for user id: {}", userId);
        
        // Find existing user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new IllegalArgumentException("User not found with id: " + userId);
                });
        
        // Register device token
        user.registerDeviceToken(deviceToken);
        userRepository.save(user);
        log.info("Device token registered successfully for user id: {}", userId);
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().getName().name())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
