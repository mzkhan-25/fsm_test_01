package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.AssignmentHistory;
import com.fsm.task.domain.model.AssignmentHistory.HistoryAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for AssignmentHistory entity.
 * Provides read-only operations for assignment history (audit trail).
 * 
 * Note: Delete operations are intentionally not exposed to maintain audit trail integrity.
 */
@Repository
public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {
    
    /**
     * Find all history records for a specific assignment
     * @param assignmentId The assignment ID
     * @return List of history records ordered by action timestamp
     */
    List<AssignmentHistory> findByAssignmentIdOrderByActionAtDesc(Long assignmentId);
    
    /**
     * Find all history records for a specific task
     * @param taskId The task ID
     * @return List of history records ordered by action timestamp
     */
    List<AssignmentHistory> findByTaskIdOrderByActionAtDesc(Long taskId);
    
    /**
     * Find all history records for a specific technician
     * @param technicianId The technician ID
     * @return List of history records ordered by action timestamp
     */
    List<AssignmentHistory> findByTechnicianIdOrderByActionAtDesc(Long technicianId);
    
    /**
     * Find all history records by action type
     * @param action The action type
     * @return List of history records with the specified action
     */
    List<AssignmentHistory> findByAction(HistoryAction action);
    
    /**
     * Find history records for a task within a date range
     * @param taskId The task ID
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of history records within the date range
     */
    List<AssignmentHistory> findByTaskIdAndActionAtBetween(Long taskId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find history records for a technician within a date range
     * @param technicianId The technician ID
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of history records within the date range
     */
    List<AssignmentHistory> findByTechnicianIdAndActionAtBetween(Long technicianId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count the number of reassignments for a task
     * @param taskId The task ID
     * @param action The action type (REASSIGNED)
     * @return Count of reassignments
     */
    @Query("SELECT COUNT(h) FROM AssignmentHistory h WHERE h.taskId = :taskId AND h.action = :action")
    int countByTaskIdAndAction(@Param("taskId") Long taskId, @Param("action") HistoryAction action);
    
    /**
     * Get the most recent history record for a task
     * @param taskId The task ID
     * @return The most recent history record
     */
    @Query("SELECT h FROM AssignmentHistory h WHERE h.taskId = :taskId ORDER BY h.actionAt DESC LIMIT 1")
    AssignmentHistory findLatestByTaskId(@Param("taskId") Long taskId);
    
    /**
     * Get all history records performed by a specific user
     * @param actionBy The user who performed the actions
     * @return List of history records
     */
    List<AssignmentHistory> findByActionByOrderByActionAtDesc(String actionBy);
    
    /**
     * Get count of actions by a specific user
     * @param actionBy The user who performed the actions
     * @return Count of actions
     */
    long countByActionBy(String actionBy);
}
