package com.fsm.task.application.dto;

import com.fsm.task.domain.model.Assignment;
import com.fsm.task.domain.model.ServiceTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for task assignment response.
 * Contains assignment details and workload warning if applicable.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object for task assignment operation")
public class AssignTaskResponse {
    
    @Schema(description = "Assignment ID", example = "1")
    private Long assignmentId;
    
    @Schema(description = "Task ID", example = "1")
    private Long taskId;
    
    @Schema(description = "Technician ID", example = "101")
    private Long technicianId;
    
    @Schema(description = "Assignment timestamp")
    private LocalDateTime assignedAt;
    
    @Schema(description = "User who made the assignment", example = "dispatcher@fsm.com")
    private String assignedBy;
    
    @Schema(description = "Updated task status", example = "ASSIGNED")
    private ServiceTask.TaskStatus taskStatus;
    
    @Schema(description = "Current workload of the technician (number of active tasks)", example = "5")
    private Integer technicianWorkload;
    
    @Schema(description = "Warning message if technician workload exceeds threshold", example = "Warning: Technician has 12 active tasks, which exceeds the recommended threshold of 10")
    private String workloadWarning;
    
    /**
     * Default workload threshold for warning
     */
    public static final int WORKLOAD_THRESHOLD = 10;
    
    /**
     * Creates an AssignTaskResponse from assignment and task entities
     * 
     * @param assignment the assignment entity
     * @param task the service task entity
     * @param workload the current technician workload
     * @param assignedBy the username who made the assignment
     * @return AssignTaskResponse DTO
     */
    public static AssignTaskResponse fromAssignment(Assignment assignment, ServiceTask task, int workload, String assignedBy) {
        String warning = null;
        if (workload > WORKLOAD_THRESHOLD) {
            warning = String.format("Warning: Technician has %d active tasks, which exceeds the recommended threshold of %d", 
                    workload, WORKLOAD_THRESHOLD);
        }
        
        return AssignTaskResponse.builder()
                .assignmentId(assignment.getId())
                .taskId(task.getId())
                .technicianId(assignment.getTechnicianId())
                .assignedAt(assignment.getAssignedAt())
                .assignedBy(assignedBy)
                .taskStatus(task.getStatus())
                .technicianWorkload(workload)
                .workloadWarning(warning)
                .build();
    }
}
