package com.fsm.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for task assignment request.
 * Contains the technician ID to assign the task to.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for assigning a task to a technician")
public class AssignTaskRequest {
    
    @NotNull(message = "Technician ID is required")
    @Schema(description = "ID of the technician to assign the task to", example = "101")
    private Long technicianId;
}
