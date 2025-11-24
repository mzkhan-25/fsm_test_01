package com.fsm.identity.application.dto;

import com.fsm.identity.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User update request DTO.
 * Contains fields that can be updated for an existing user.
 * All fields are optional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    
    private String name;
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", 
             message = "Phone number must be in E.164 format (e.g., +12025551234)")
    private String phone;
    
    private String password;
    
    private Role role;
}
