package com.fsm.identity.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

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
    
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private Role role;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    /**
     * User status enum representing active or inactive state
     */
    public enum UserStatus {
        ACTIVE,
        INACTIVE
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
}
