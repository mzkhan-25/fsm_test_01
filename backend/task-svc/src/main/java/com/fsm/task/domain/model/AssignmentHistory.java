package com.fsm.task.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AssignmentHistory entity for tracking all assignment changes (audit trail).
 * 
 * Domain Invariants:
 * - History records cannot be deleted (audit trail)
 * - Every assignment change must be recorded
 * - Previous technician is recorded for reassignments
 */
@Entity
@Table(name = "assignment_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Assignment ID is required")
    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;
    
    @NotNull(message = "Task ID is required")
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    @NotNull(message = "Technician ID is required")
    @Column(name = "technician_id", nullable = false)
    private Long technicianId;
    
    @Column(name = "previous_technician_id")
    private Long previousTechnicianId;
    
    @NotNull(message = "Action is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HistoryAction action;
    
    @NotBlank(message = "Action by is required")
    @Column(name = "action_by", nullable = false)
    private String actionBy;
    
    @NotNull(message = "Action at timestamp is required")
    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;
    
    @Column(length = 500)
    private String reason;
    
    /**
     * HistoryAction enum representing types of assignment actions
     */
    public enum HistoryAction {
        /** Assignment was created */
        CREATED,
        /** Assignment was reassigned to a different technician */
        REASSIGNED,
        /** Assignment was completed */
        COMPLETED,
        /** Assignment was cancelled */
        CANCELLED
    }
    
    @PrePersist
    protected void onCreate() {
        if (actionAt == null) {
            actionAt = LocalDateTime.now();
        }
    }
    
    /**
     * Factory method to create a history record for a new assignment
     */
    public static AssignmentHistory forCreation(Assignment assignment, String actionBy) {
        return AssignmentHistory.builder()
                .assignmentId(assignment.getId())
                .taskId(assignment.getTaskId())
                .technicianId(assignment.getTechnicianId())
                .action(HistoryAction.CREATED)
                .actionBy(actionBy)
                .actionAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Factory method to create a history record for reassignment
     */
    public static AssignmentHistory forReassignment(Assignment assignment, Long previousTechnicianId, String actionBy, String reason) {
        return AssignmentHistory.builder()
                .assignmentId(assignment.getId())
                .taskId(assignment.getTaskId())
                .technicianId(assignment.getTechnicianId())
                .previousTechnicianId(previousTechnicianId)
                .action(HistoryAction.REASSIGNED)
                .actionBy(actionBy)
                .actionAt(LocalDateTime.now())
                .reason(reason)
                .build();
    }
    
    /**
     * Factory method to create a history record for completion
     */
    public static AssignmentHistory forCompletion(Assignment assignment, String actionBy) {
        return AssignmentHistory.builder()
                .assignmentId(assignment.getId())
                .taskId(assignment.getTaskId())
                .technicianId(assignment.getTechnicianId())
                .action(HistoryAction.COMPLETED)
                .actionBy(actionBy)
                .actionAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Factory method to create a history record for cancellation
     */
    public static AssignmentHistory forCancellation(Assignment assignment, String actionBy, String reason) {
        return AssignmentHistory.builder()
                .assignmentId(assignment.getId())
                .taskId(assignment.getTaskId())
                .technicianId(assignment.getTechnicianId())
                .action(HistoryAction.CANCELLED)
                .actionBy(actionBy)
                .actionAt(LocalDateTime.now())
                .reason(reason)
                .build();
    }
}
