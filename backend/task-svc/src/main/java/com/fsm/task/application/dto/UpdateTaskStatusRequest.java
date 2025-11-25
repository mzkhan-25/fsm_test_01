package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating task status.
 * Used by technicians to update the status of their assigned tasks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for updating task status")
public class UpdateTaskStatusRequest {
    
    @NotNull(message = "Status is required")
    @Schema(description = "New task status", example = "IN_PROGRESS", required = true)
    private TaskStatus status;
}
