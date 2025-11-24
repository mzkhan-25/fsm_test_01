package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new service task.
 * Contains all required fields with validation constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for creating a new service task")
public class CreateTaskRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @Schema(description = "Task title", example = "Repair HVAC System", minLength = 3, maxLength = 200)
    private String title;
    
    @Schema(description = "Task description", example = "Customer reports heating system not working properly")
    private String description;
    
    @NotBlank(message = "Client address is required")
    @Schema(description = "Client address where the task will be performed", example = "123 Main St, Springfield, IL 62701")
    private String clientAddress;
    
    @NotNull(message = "Priority is required")
    @Schema(description = "Task priority level", example = "HIGH")
    private Priority priority;
    
    @Positive(message = "Estimated duration must be positive")
    @Schema(description = "Estimated duration in minutes", example = "120")
    private Integer estimatedDuration;
}
