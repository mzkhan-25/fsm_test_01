package com.fsm.identity.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User entity representing a user in the FSM system.
 * Domain Invariants:
 * - User email must be unique
 * - User must have exactly one role
 * - User status can only be active or inactive
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email", name = "uk_user_email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", 
             message = "Phone number must be in E.164 format (e.g., +12025551234)")
    @Column(name = "phone")
    private String phone;
    
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;
    
    @NotNull(message = "Role is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role"))
    private RoleEntity role;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "device_token")
    private String deviceToken;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * User status enum representing active or inactive state
     */
    public enum UserStatus {
        ACTIVE,
        INACTIVE
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Activates the user
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
    
    /**
     * Deactivates the user
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
    
    /**
     * Checks if user is active
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
    
    /**
     * Registers a device token for push notifications
     * @param token the device token
     */
    public void registerDeviceToken(String token) {
        this.deviceToken = token;
    }
}
