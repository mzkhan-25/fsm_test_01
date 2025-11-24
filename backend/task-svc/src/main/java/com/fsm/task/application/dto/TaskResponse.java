package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for service task responses.
 * Contains all task fields for API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object representing a service task")
public class TaskResponse {
    
    @Schema(description = "Task ID", example = "1")
    private Long id;
    
    @Schema(description = "Task title", example = "Repair HVAC System")
    private String title;
    
    @Schema(description = "Task description", example = "Customer reports heating system not working properly")
    private String description;
    
    @Schema(description = "Client address", example = "123 Main St, Springfield, IL 62701")
    private String clientAddress;
    
    @Schema(description = "Task priority level", example = "HIGH")
    private Priority priority;
    
    @Schema(description = "Estimated duration in minutes", example = "120")
    private Integer estimatedDuration;
    
    @Schema(description = "Task status", example = "UNASSIGNED")
    private TaskStatus status;
    
    @Schema(description = "User who created the task", example = "dispatcher@fsm.com")
    private String createdBy;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    /**
     * Converts a ServiceTask entity to TaskResponse DTO
     * 
     * @param task the service task entity
     * @return TaskResponse DTO
     */
    public static TaskResponse fromEntity(ServiceTask task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .clientAddress(task.getClientAddress())
                .priority(task.getPriority())
                .estimatedDuration(task.getEstimatedDuration())
                .status(task.getStatus())
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
