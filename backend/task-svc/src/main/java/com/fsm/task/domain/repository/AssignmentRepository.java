package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.Assignment;
import com.fsm.task.domain.model.Assignment.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Assignment entity.
 * Provides database persistence operations for assignment tracking.
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    /**
     * Find all assignments for a specific task
     * @param taskId The task ID
     * @return List of assignments for the task
     */
    List<Assignment> findByTaskId(Long taskId);
    
    /**
     * Find all assignments for a specific technician
     * @param technicianId The technician ID
     * @return List of assignments for the technician
     */
    List<Assignment> findByTechnicianId(Long technicianId);
    
    /**
     * Find all active assignments for a specific technician
     * @param technicianId The technician ID
     * @param status The assignment status to filter by
     * @return List of assignments with the specified status for the technician
     */
    List<Assignment> findByTechnicianIdAndStatus(Long technicianId, AssignmentStatus status);
    
    /**
     * Find the current active assignment for a task
     * @param taskId The task ID
     * @param status The status to filter by
     * @return Optional containing the active assignment if found
     */
    Optional<Assignment> findByTaskIdAndStatus(Long taskId, AssignmentStatus status);
    
    /**
     * Find all assignments by status
     * @param status The assignment status
     * @return List of assignments with the specified status
     */
    List<Assignment> findByStatus(AssignmentStatus status);
    
    /**
     * Count active assignments for a technician (technician workload)
     * @param technicianId The technician ID
     * @param status The status to count
     * @return Number of assignments with the specified status
     */
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.technicianId = :technicianId AND a.status = :status")
    int countByTechnicianIdAndStatus(@Param("technicianId") Long technicianId, @Param("status") AssignmentStatus status);
    
    /**
     * Get technician workload (count of active assignments)
     * @param technicianId The technician ID
     * @return Number of active assignments for the technician
     */
    default int getTechnicianWorkload(Long technicianId) {
        return countByTechnicianIdAndStatus(technicianId, AssignmentStatus.ACTIVE);
    }
    
    /**
     * Returns hardcoded sample assignments for initial development
     * This method provides sample assignments with various statuses
     */
    default List<Assignment> getHardcodedAssignments() {
        LocalDateTime now = LocalDateTime.now();
        
        return Arrays.asList(
            Assignment.builder()
                .id(1L)
                .taskId(2L)
                .technicianId(101L)
                .assignedAt(now.minusDays(2))
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build(),
            
            Assignment.builder()
                .id(2L)
                .taskId(3L)
                .technicianId(102L)
                .assignedAt(now.minusDays(3))
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build(),
            
            Assignment.builder()
                .id(3L)
                .taskId(5L)
                .technicianId(101L)
                .assignedAt(now.minusDays(5))
                .assignedBy("admin@fsm.com")
                .status(AssignmentStatus.COMPLETED)
                .build(),
            
            Assignment.builder()
                .id(4L)
                .taskId(6L)
                .technicianId(103L)
                .assignedAt(now.minusDays(4))
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build(),
            
            Assignment.builder()
                .id(5L)
                .taskId(2L)
                .technicianId(104L)
                .assignedAt(now.minusDays(3))
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.REASSIGNED)
                .reason("Technician on leave, reassigned to available technician")
                .build(),
            
            Assignment.builder()
                .id(6L)
                .taskId(1L)
                .technicianId(102L)
                .assignedAt(now.minusHours(1))
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.CANCELLED)
                .reason("Task cancelled by customer")
                .build()
        );
    }
}
