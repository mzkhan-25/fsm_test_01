package com.fsm.identity.application.dto;

import com.fsm.identity.domain.model.User.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User response DTO.
 * Contains user information without sensitive data like passwords.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
