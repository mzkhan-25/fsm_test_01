package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Data JPA repository for ServiceTask entity.
 * Provides database persistence operations for service tasks.
 * Inherits CRUD operations from JpaRepository: create, findById, findAll, update, delete.
 */
@Repository
public interface TaskRepository extends JpaRepository<ServiceTask, Long> {
    
    /**
     * Find tasks by status (uses idx_service_tasks_status index)
     */
    List<ServiceTask> findByStatus(TaskStatus status);
    
    /**
     * Find tasks by priority (uses idx_service_tasks_priority index)
     */
    List<ServiceTask> findByPriority(Priority priority);
    
    /**
     * Find tasks by created by (uses idx_service_tasks_created_by index)
     */
    List<ServiceTask> findByCreatedBy(String createdBy);
    
    /**
     * Find tasks by status and priority (uses idx_service_tasks_status_priority composite index)
     */
    List<ServiceTask> findByStatusAndPriority(TaskStatus status, Priority priority);
    
    /**
     * Find tasks created after a specific date (uses idx_service_tasks_created_at index)
     */
    List<ServiceTask> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find tasks created before a specific date (uses idx_service_tasks_created_at index)
     */
    List<ServiceTask> findByCreatedAtBefore(LocalDateTime date);
    
    /**
     * Find tasks created between two dates (uses idx_service_tasks_created_at index)
     */
    List<ServiceTask> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find tasks by status ordered by priority (HIGH tasks first)
     */
    @Query("SELECT t FROM ServiceTask t WHERE t.status = :status ORDER BY " +
           "CASE t.priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 END")
    List<ServiceTask> findByStatusOrderByPriorityDesc(@Param("status") TaskStatus status);
    
    /**
     * Find tasks by status ordered by created date (most recent first)
     */
    List<ServiceTask> findByStatusOrderByCreatedAtDesc(TaskStatus status);
    
    /**
     * Count tasks by status
     */
    long countByStatus(TaskStatus status);
    
    /**
     * Count tasks by priority
     */
    long countByPriority(Priority priority);
    
    /**
     * Find all unassigned high priority tasks (for dispatcher dashboard)
     */
    @Query("SELECT t FROM ServiceTask t WHERE t.status = 'UNASSIGNED' AND t.priority = 'HIGH' ORDER BY t.createdAt ASC")
    List<ServiceTask> findUrgentUnassignedTasks();
    
    /**
     * Returns hardcoded sample tasks for initial development
     * This method provides 6 sample tasks with various statuses and priorities
     */
    default List<ServiceTask> getHardcodedTasks() {
        LocalDateTime now = LocalDateTime.now();
        
        return Arrays.asList(
            ServiceTask.builder()
                .id(1L)
                .title("Repair HVAC System")
                .description("Customer reports heating system not working properly. Need to inspect and repair HVAC unit.")
                .clientAddress("123 Main St, Springfield, IL 62701")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(now.minusDays(1))
                .build(),
            
            ServiceTask.builder()
                .id(2L)
                .title("Install New Water Heater")
                .description("Replace old water heater with new 50-gallon electric model.")
                .clientAddress("456 Oak Ave, Springfield, IL 62702")
                .priority(Priority.MEDIUM)
                .estimatedDuration(180)
                .status(TaskStatus.ASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(now.minusDays(2))
                .build(),
            
            ServiceTask.builder()
                .id(3L)
                .title("Electrical Panel Inspection")
                .description("Annual inspection of electrical panel and circuit breakers.")
                .clientAddress("789 Elm St, Springfield, IL 62703")
                .priority(Priority.LOW)
                .estimatedDuration(60)
                .status(TaskStatus.IN_PROGRESS)
                .createdBy("admin@fsm.com")
                .createdAt(now.minusDays(3))
                .build(),
            
            ServiceTask.builder()
                .id(4L)
                .title("Plumbing Leak Emergency")
                .description("Customer reports major leak in kitchen. Immediate attention required.")
                .clientAddress("321 Pine Rd, Springfield, IL 62704")
                .priority(Priority.HIGH)
                .estimatedDuration(90)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(now.minusHours(2))
                .build(),
            
            ServiceTask.builder()
                .id(5L)
                .title("Appliance Installation")
                .description("Install new dishwasher and connect to water supply.")
                .clientAddress("654 Maple Dr, Springfield, IL 62705")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.COMPLETED)
                .createdBy("admin@fsm.com")
                .createdAt(now.minusDays(5))
                .build(),
            
            ServiceTask.builder()
                .id(6L)
                .title("Routine Maintenance Check")
                .description("Quarterly maintenance check for commercial building HVAC systems.")
                .clientAddress("987 Business Blvd, Springfield, IL 62706")
                .priority(Priority.LOW)
                .estimatedDuration(240)
                .status(TaskStatus.ASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(now.minusDays(4))
                .build()
        );
    }
}
