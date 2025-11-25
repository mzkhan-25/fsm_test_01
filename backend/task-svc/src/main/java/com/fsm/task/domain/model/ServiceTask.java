package com.fsm.task.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ServiceTask entity representing a field service task in the FSM system.
 * Domain Invariants:
 * - Task must have a title (minimum 3 characters)
 * - Task must have a valid client address
 * - Task must have a priority (HIGH, MEDIUM, LOW)
 * - Task status must be one of valid enum values
 * - Estimated duration must be positive if provided
 */
@Entity
@Table(name = "service_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, message = "Title must be at least 3 characters")
    @Column(nullable = false)
    private String title;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotBlank(message = "Client address is required")
    @Column(nullable = false, name = "client_address")
    private String clientAddress;
    
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Positive(message = "Estimated duration must be positive")
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.UNASSIGNED;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "assigned_technician_id")
    private Long assignedTechnicianId;
    
    /**
     * Priority enum representing task priority levels
     */
    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }
    
    /**
     * TaskStatus enum representing task lifecycle states
     */
    public enum TaskStatus {
        UNASSIGNED,
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Assigns the task
     */
    public void assign() {
        if (this.status == TaskStatus.UNASSIGNED) {
            this.status = TaskStatus.ASSIGNED;
        }
    }
    
    /**
     * Assigns the task to a specific technician
     * @param technicianId The ID of the technician to assign to
     */
    public void assignToTechnician(Long technicianId) {
        if (this.status == TaskStatus.UNASSIGNED) {
            this.status = TaskStatus.ASSIGNED;
            this.assignedTechnicianId = technicianId;
        }
    }
    
    /**
     * Reassigns the task to a different technician.
     * Only tasks with status ASSIGNED or IN_PROGRESS can be reassigned.
     * When an IN_PROGRESS task is reassigned, it returns to ASSIGNED status
     * since the new technician hasn't started work on it yet.
     * @param technicianId The ID of the new technician
     */
    public void reassignToTechnician(Long technicianId) {
        if (this.status == TaskStatus.ASSIGNED || this.status == TaskStatus.IN_PROGRESS) {
            this.assignedTechnicianId = technicianId;
            // When reassigning, the task should be in ASSIGNED status for the new technician
            this.status = TaskStatus.ASSIGNED;
        }
    }
    
    /**
     * Checks if the task can be assigned or reassigned
     * @return true if the task can be (re)assigned
     */
    public boolean canBeAssigned() {
        return this.status == TaskStatus.UNASSIGNED || this.status == TaskStatus.ASSIGNED;
    }
    
    /**
     * Checks if the task can be reassigned to a different technician.
     * Only tasks with status ASSIGNED or IN_PROGRESS can be reassigned.
     * @return true if the task can be reassigned
     */
    public boolean canBeReassigned() {
        return this.status == TaskStatus.ASSIGNED || this.status == TaskStatus.IN_PROGRESS;
    }
    
    /**
     * Checks if the task is currently assigned
     * @return true if the task is assigned
     */
    public boolean isAssigned() {
        return this.status == TaskStatus.ASSIGNED;
    }
    
    /**
     * Checks if the task is in progress
     * @return true if the task is in progress
     */
    public boolean isInProgress() {
        return this.status == TaskStatus.IN_PROGRESS;
    }
    
    /**
     * Starts the task (moves to IN_PROGRESS)
     */
    public void start() {
        if (this.status == TaskStatus.ASSIGNED) {
            this.status = TaskStatus.IN_PROGRESS;
        }
    }
    
    /**
     * Completes the task
     */
    public void complete() {
        if (this.status == TaskStatus.IN_PROGRESS) {
            this.status = TaskStatus.COMPLETED;
        }
    }
    
    /**
     * Checks if task is completed
     */
    public boolean isCompleted() {
        return this.status == TaskStatus.COMPLETED;
    }
    
    /**
     * Checks if task is unassigned
     */
    public boolean isUnassigned() {
        return this.status == TaskStatus.UNASSIGNED;
    }
}
