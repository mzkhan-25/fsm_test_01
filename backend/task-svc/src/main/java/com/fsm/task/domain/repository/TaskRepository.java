package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Data JPA repository for ServiceTask entity.
 * Provides database persistence operations for service tasks.
 * Currently returns hardcoded data for initial development.
 */
@Repository
public interface TaskRepository extends JpaRepository<ServiceTask, Long> {
    
    /**
     * Find tasks by status
     */
    List<ServiceTask> findByStatus(TaskStatus status);
    
    /**
     * Find tasks by priority
     */
    List<ServiceTask> findByPriority(Priority priority);
    
    /**
     * Find tasks by created by
     */
    List<ServiceTask> findByCreatedBy(String createdBy);
    
    /**
     * Returns hardcoded sample tasks for initial development
     * This method provides 5-6 sample tasks with various statuses and priorities
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
