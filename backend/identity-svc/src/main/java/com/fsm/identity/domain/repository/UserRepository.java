package com.fsm.identity.domain.repository;

import com.fsm.identity.domain.model.Role;
import com.fsm.identity.domain.model.User;
import com.fsm.identity.domain.model.User.UserStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * UserRepository with hardcoded user data.
 * This provides basic CRUD operations and returns sample users.
 */
@Repository
public class UserRepository {
    
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailToId = new HashMap<>();  // Track email uniqueness
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    public UserRepository() {
        initializeHardcodedUsers();
    }
    
    /**
     * Initialize repository with hardcoded sample users
     */
    private void initializeHardcodedUsers() {
        // Admin user
        User admin = User.builder()
                .id(idGenerator.getAndIncrement())
                .name("John Administrator")
                .email("admin@fsm.com")
                .phone("+12025551001")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        users.put(admin.getId(), admin);
        emailToId.put(admin.getEmail(), admin.getId());
        
        // Dispatcher user
        User dispatcher = User.builder()
                .id(idGenerator.getAndIncrement())
                .name("Sarah Dispatcher")
                .email("sarah.dispatcher@fsm.com")
                .phone("+12025551002")
                .role(Role.DISPATCHER)
                .status(UserStatus.ACTIVE)
                .build();
        users.put(dispatcher.getId(), dispatcher);
        emailToId.put(dispatcher.getEmail(), dispatcher.getId());
        
        // Supervisor user
        User supervisor = User.builder()
                .id(idGenerator.getAndIncrement())
                .name("Mike Supervisor")
                .email("mike.supervisor@fsm.com")
                .phone("+12025551003")
                .role(Role.SUPERVISOR)
                .status(UserStatus.ACTIVE)
                .build();
        users.put(supervisor.getId(), supervisor);
        emailToId.put(supervisor.getEmail(), supervisor.getId());
        
        // Technician user
        User technician = User.builder()
                .id(idGenerator.getAndIncrement())
                .name("Tom Technician")
                .email("tom.technician@fsm.com")
                .phone("+12025551004")
                .role(Role.TECHNICIAN)
                .status(UserStatus.ACTIVE)
                .build();
        users.put(technician.getId(), technician);
        emailToId.put(technician.getEmail(), technician.getId());
    }
    
    /**
     * Find all users
     */
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
    
    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        Long userId = emailToId.get(email);
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(users.get(userId));
    }
    
    /**
     * Find users by role
     */
    public List<User> findByRole(Role role) {
        return users.values().stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }
    
    /**
     * Find users by status
     */
    public List<User> findByStatus(UserStatus status) {
        return users.values().stream()
                .filter(user -> user.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    /**
     * Save or update user
     */
    public User save(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            // New user - check email uniqueness
            if (emailToId.containsKey(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }
            user.setId(idGenerator.getAndIncrement());
            users.put(user.getId(), user);
            emailToId.put(user.getEmail(), user.getId());
        } else {
            // Existing user - find original email by looking up in emailToId
            String originalEmail = null;
            for (Map.Entry<String, Long> entry : emailToId.entrySet()) {
                if (entry.getValue().equals(user.getId())) {
                    originalEmail = entry.getKey();
                    break;
                }
            }
            
            // If email changed, check uniqueness
            if (originalEmail != null && !originalEmail.equals(user.getEmail())) {
                if (emailToId.containsKey(user.getEmail())) {
                    throw new IllegalArgumentException("Email already exists: " + user.getEmail());
                }
                // Remove old email mapping and add new one
                emailToId.remove(originalEmail);
                emailToId.put(user.getEmail(), user.getId());
            }
            
            users.put(user.getId(), user);
        }
        
        return user;
    }
    
    /**
     * Delete user by ID
     */
    public void deleteById(Long id) {
        User user = users.remove(id);
        if (user != null) {
            emailToId.remove(user.getEmail());
        }
    }
    
    /**
     * Check if user exists by ID
     */
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
    
    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return email != null && emailToId.containsKey(email);
    }
    
    /**
     * Count all users
     */
    public long count() {
        return users.size();
    }
}
