package com.fsm.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for task reassignment request.
 * Contains the new technician ID and optional reason for reassignment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for reassigning a task to a different technician")
public class ReassignTaskRequest {
    
    @NotNull(message = "New technician ID is required")
    @Schema(description = "ID of the new technician to reassign the task to", example = "102")
    private Long newTechnicianId;
    
    @Schema(description = "Reason for reassignment (required for IN_PROGRESS tasks)", example = "Original technician is on leave")
    private String reason;
}
