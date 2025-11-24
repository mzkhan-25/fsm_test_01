package com.fsm.task.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Assignment entity representing a task-technician assignment relationship.
 * Tracks the history of assignments including reassignments.
 * 
 * Domain Invariants:
 * - Task can only be assigned to one technician at a time (managed at service level)
 * - Assignment history is preserved even after reassignment
 * - Only tasks with status UNASSIGNED or ASSIGNED can be (re)assigned
 */
@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Task ID is required")
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    @NotNull(message = "Technician ID is required")
    @Column(name = "technician_id", nullable = false)
    private Long technicianId;
    
    @NotNull(message = "Assigned at timestamp is required")
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
    
    @NotNull(message = "Assigned by is required")
    @Column(name = "assigned_by", nullable = false)
    private String assignedBy;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ACTIVE;
    
    @Column(length = 500)
    private String reason;
    
    /**
     * AssignmentStatus enum representing assignment lifecycle states
     */
    public enum AssignmentStatus {
        /** Currently active assignment */
        ACTIVE,
        /** Assignment was superseded by a reassignment */
        REASSIGNED,
        /** Assignment was completed */
        COMPLETED,
        /** Assignment was cancelled */
        CANCELLED
    }
    
    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Marks this assignment as reassigned when a task is reassigned to a different technician
     * @param reason The reason for reassignment
     */
    public void markAsReassigned(String reason) {
        if (this.status == AssignmentStatus.ACTIVE) {
            this.status = AssignmentStatus.REASSIGNED;
            this.reason = reason;
        }
    }
    
    /**
     * Marks this assignment as completed
     */
    public void complete() {
        if (this.status == AssignmentStatus.ACTIVE) {
            this.status = AssignmentStatus.COMPLETED;
        }
    }
    
    /**
     * Marks this assignment as cancelled
     * @param reason The reason for cancellation
     */
    public void cancel(String reason) {
        if (this.status == AssignmentStatus.ACTIVE) {
            this.status = AssignmentStatus.CANCELLED;
            this.reason = reason;
        }
    }
    
    /**
     * Checks if the assignment is currently active
     * @return true if the assignment is active
     */
    public boolean isActive() {
        return this.status == AssignmentStatus.ACTIVE;
    }
    
    /**
     * Checks if the assignment is completed
     * @return true if the assignment is completed
     */
    public boolean isCompleted() {
        return this.status == AssignmentStatus.COMPLETED;
    }
}
