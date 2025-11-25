package com.fsm.task.application.dto;

import com.fsm.task.domain.model.Assignment;
import com.fsm.task.domain.model.AssignmentHistory;
import com.fsm.task.domain.model.ServiceTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for task reassignment response.
 * Contains reassignment details and assignment history.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object for task reassignment operation")
public class ReassignTaskResponse {
    
    @Schema(description = "Assignment ID", example = "1")
    private Long assignmentId;
    
    @Schema(description = "Task ID", example = "1")
    private Long taskId;
    
    @Schema(description = "Previous technician ID", example = "101")
    private Long previousTechnicianId;
    
    @Schema(description = "New technician ID", example = "102")
    private Long newTechnicianId;
    
    @Schema(description = "Reassignment timestamp")
    private LocalDateTime reassignedAt;
    
    @Schema(description = "User who made the reassignment", example = "dispatcher@fsm.com")
    private String reassignedBy;
    
    @Schema(description = "Reason for reassignment", example = "Original technician is on leave")
    private String reason;
    
    @Schema(description = "Updated task status", example = "ASSIGNED")
    private ServiceTask.TaskStatus taskStatus;
    
    @Schema(description = "Current workload of the new technician (number of active tasks)", example = "5")
    private Integer newTechnicianWorkload;
    
    @Schema(description = "Warning message if new technician workload exceeds threshold")
    private String workloadWarning;
    
    @Schema(description = "Assignment history for this task")
    private List<AssignmentHistoryDto> assignmentHistory;
    
    /**
     * Default workload threshold for warning
     */
    public static final int WORKLOAD_THRESHOLD = 10;
    
    /**
     * DTO for assignment history entries
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Assignment history entry")
    public static class AssignmentHistoryDto {
        @Schema(description = "History entry ID", example = "1")
        private Long id;
        
        @Schema(description = "Technician ID", example = "101")
        private Long technicianId;
        
        @Schema(description = "Previous technician ID (for reassignments)", example = "100")
        private Long previousTechnicianId;
        
        @Schema(description = "Action type", example = "REASSIGNED")
        private String action;
        
        @Schema(description = "User who performed the action", example = "dispatcher@fsm.com")
        private String actionBy;
        
        @Schema(description = "Action timestamp")
        private LocalDateTime actionAt;
        
        @Schema(description = "Reason for the action")
        private String reason;
        
        /**
         * Converts an AssignmentHistory entity to DTO
         */
        public static AssignmentHistoryDto fromEntity(AssignmentHistory history) {
            return AssignmentHistoryDto.builder()
                    .id(history.getId())
                    .technicianId(history.getTechnicianId())
                    .previousTechnicianId(history.getPreviousTechnicianId())
                    .action(history.getAction().name())
                    .actionBy(history.getActionBy())
                    .actionAt(history.getActionAt())
                    .reason(history.getReason())
                    .build();
        }
    }
    
    /**
     * Creates a ReassignTaskResponse from assignment and task entities
     * 
     * @param assignment the new assignment entity
     * @param task the service task entity
     * @param previousTechnicianId the previous technician ID
     * @param reason the reason for reassignment
     * @param workload the current workload of the new technician
     * @param reassignedBy the username who made the reassignment
     * @param history the assignment history for this task
     * @return ReassignTaskResponse DTO
     */
    public static ReassignTaskResponse fromReassignment(
            Assignment assignment, 
            ServiceTask task, 
            Long previousTechnicianId,
            String reason,
            int workload, 
            String reassignedBy,
            List<AssignmentHistory> history) {
        
        String warning = null;
        if (workload > WORKLOAD_THRESHOLD) {
            warning = String.format("Warning: New technician has %d active tasks, which exceeds the recommended threshold of %d", 
                    workload, WORKLOAD_THRESHOLD);
        }
        
        List<AssignmentHistoryDto> historyDtos = history.stream()
                .map(AssignmentHistoryDto::fromEntity)
                .toList();
        
        return ReassignTaskResponse.builder()
                .assignmentId(assignment.getId())
                .taskId(task.getId())
                .previousTechnicianId(previousTechnicianId)
                .newTechnicianId(assignment.getTechnicianId())
                .reassignedAt(assignment.getAssignedAt())
                .reassignedBy(reassignedBy)
                .reason(reason)
                .taskStatus(task.getStatus())
                .newTechnicianWorkload(workload)
                .workloadWarning(warning)
                .assignmentHistory(historyDtos)
                .build();
    }
}
