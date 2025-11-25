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
 * DTO for technician's task response.
 * Contains task details relevant to technicians viewing their assigned tasks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object representing a technician's assigned task")
public class TechnicianTaskResponse {
    
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
    
    @Schema(description = "Task status", example = "ASSIGNED")
    private TaskStatus status;
    
    @Schema(description = "Timestamp when the task was assigned")
    private LocalDateTime assignedAt;
    
    /**
     * Converts a ServiceTask entity to TechnicianTaskResponse DTO
     * 
     * @param task the service task entity
     * @param assignedAt the timestamp when the task was assigned
     * @return TechnicianTaskResponse DTO
     */
    public static TechnicianTaskResponse fromEntity(ServiceTask task, LocalDateTime assignedAt) {
        return TechnicianTaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .clientAddress(task.getClientAddress())
                .priority(task.getPriority())
                .estimatedDuration(task.getEstimatedDuration())
                .status(task.getStatus())
                .assignedAt(assignedAt)
                .build();
    }
    
    /**
     * Converts a ServiceTask entity to TechnicianTaskResponse DTO
     * Uses createdAt as default for assignedAt if not provided separately
     * 
     * @param task the service task entity
     * @return TechnicianTaskResponse DTO
     */
    public static TechnicianTaskResponse fromEntity(ServiceTask task) {
        return fromEntity(task, task.getCreatedAt());
    }
}
