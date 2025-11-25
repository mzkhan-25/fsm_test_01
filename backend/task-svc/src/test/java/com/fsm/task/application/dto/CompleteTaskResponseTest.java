package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CompleteTaskResponse DTO
 */
class CompleteTaskResponseTest {
    
    @Test
    void testFromEntityWithDuration() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(120);
        LocalDateTime completedAt = LocalDateTime.now();
        LocalDateTime assignedAt = LocalDateTime.now().minusHours(3);
        
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.COMPLETED)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .workSummary("Work completed successfully")
                .build();
        
        CompleteTaskResponse response = CompleteTaskResponse.fromEntity(task, assignedAt);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals("123 Test St", response.getClientAddress());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(120, response.getEstimatedDuration());
        assertEquals(TaskStatus.COMPLETED, response.getStatus());
        assertEquals(assignedAt, response.getAssignedAt());
        assertEquals(startedAt, response.getStartedAt());
        assertEquals(completedAt, response.getCompletedAt());
        assertEquals("Work completed successfully", response.getWorkSummary());
        assertNotNull(response.getActualDurationMinutes());
        assertTrue(response.getActualDurationMinutes() > 0);
    }
    
    @Test
    void testFromEntityWithoutStartedAt() {
        LocalDateTime assignedAt = LocalDateTime.now().minusHours(3);
        
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .workSummary("Work completed")
                .build();
        
        CompleteTaskResponse response = CompleteTaskResponse.fromEntity(task, assignedAt);
        
        assertNotNull(response);
        assertNull(response.getActualDurationMinutes());
    }
    
    @Test
    void testFromEntityWithoutCompletedAt() {
        LocalDateTime assignedAt = LocalDateTime.now().minusHours(3);
        
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now().minusHours(1))
                .build();
        
        CompleteTaskResponse response = CompleteTaskResponse.fromEntity(task, assignedAt);
        
        assertNotNull(response);
        assertNull(response.getActualDurationMinutes());
    }
    
    @Test
    void testDurationCalculation() {
        LocalDateTime startedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime completedAt = LocalDateTime.of(2024, 1, 1, 12, 30);
        LocalDateTime assignedAt = LocalDateTime.of(2024, 1, 1, 9, 0);
        
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.COMPLETED)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .workSummary("Work completed")
                .build();
        
        CompleteTaskResponse response = CompleteTaskResponse.fromEntity(task, assignedAt);
        
        assertNotNull(response.getActualDurationMinutes());
        assertEquals(150, response.getActualDurationMinutes()); // 2.5 hours = 150 minutes
    }
    
    @Test
    void testGettersAndSetters() {
        CompleteTaskResponse response = new CompleteTaskResponse();
        
        response.setId(1L);
        response.setTitle("Test Task");
        response.setDescription("Test Description");
        response.setClientAddress("123 Test St");
        response.setPriority(Priority.HIGH);
        response.setEstimatedDuration(120);
        response.setStatus(TaskStatus.COMPLETED);
        
        LocalDateTime assignedAt = LocalDateTime.now().minusHours(3);
        LocalDateTime startedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime completedAt = LocalDateTime.now();
        
        response.setAssignedAt(assignedAt);
        response.setStartedAt(startedAt);
        response.setCompletedAt(completedAt);
        response.setWorkSummary("Work completed");
        response.setActualDurationMinutes(120L);
        
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals("123 Test St", response.getClientAddress());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(120, response.getEstimatedDuration());
        assertEquals(TaskStatus.COMPLETED, response.getStatus());
        assertEquals(assignedAt, response.getAssignedAt());
        assertEquals(startedAt, response.getStartedAt());
        assertEquals(completedAt, response.getCompletedAt());
        assertEquals("Work completed", response.getWorkSummary());
        assertEquals(120L, response.getActualDurationMinutes());
    }
    
    @Test
    void testBuilder() {
        LocalDateTime assignedAt = LocalDateTime.now().minusHours(3);
        LocalDateTime startedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime completedAt = LocalDateTime.now();
        
        CompleteTaskResponse response = CompleteTaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.COMPLETED)
                .assignedAt(assignedAt)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .workSummary("Work completed")
                .actualDurationMinutes(120L)
                .build();
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(TaskStatus.COMPLETED, response.getStatus());
        assertEquals("Work completed", response.getWorkSummary());
        assertEquals(120L, response.getActualDurationMinutes());
    }
    
    @Test
    void testNoArgsConstructor() {
        CompleteTaskResponse response = new CompleteTaskResponse();
        assertNotNull(response);
    }
    
    @Test
    void testAllArgsConstructor() {
        LocalDateTime assignedAt = LocalDateTime.now().minusHours(3);
        LocalDateTime startedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime completedAt = LocalDateTime.now();
        
        CompleteTaskResponse response = new CompleteTaskResponse(
                1L, "Test Task", "Test Description", "123 Test St",
                Priority.HIGH, 120, TaskStatus.COMPLETED,
                assignedAt, startedAt, completedAt, "Work completed", 120L
        );
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals(TaskStatus.COMPLETED, response.getStatus());
        assertEquals("Work completed", response.getWorkSummary());
        assertEquals(120L, response.getActualDurationMinutes());
    }
}
