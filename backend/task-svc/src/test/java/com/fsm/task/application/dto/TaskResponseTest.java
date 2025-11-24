package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskResponse DTO
 */
class TaskResponseTest {
    
    @Test
    void testFromEntity() {
        LocalDateTime now = LocalDateTime.now();
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("test@example.com")
                .createdAt(now)
                .build();
        
        TaskResponse response = TaskResponse.fromEntity(task);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals("123 Main St", response.getClientAddress());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(120, response.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, response.getStatus());
        assertEquals("test@example.com", response.getCreatedBy());
        assertEquals(now, response.getCreatedAt());
    }
    
    @Test
    void testFromEntityWithNullOptionalFields() {
        ServiceTask task = ServiceTask.builder()
                .id(2L)
                .title("Minimal Task")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.ASSIGNED)
                .build();
        
        TaskResponse response = TaskResponse.fromEntity(task);
        
        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Minimal Task", response.getTitle());
        assertNull(response.getDescription());
        assertEquals("456 Test Ave", response.getClientAddress());
        assertEquals(Priority.LOW, response.getPriority());
        assertNull(response.getEstimatedDuration());
        assertEquals(TaskStatus.ASSIGNED, response.getStatus());
        assertNull(response.getCreatedBy());
        assertNull(response.getCreatedAt());
    }
    
    @Test
    void testBuilderPattern() {
        LocalDateTime now = LocalDateTime.now();
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Builder Test")
                .description("Testing builder")
                .clientAddress("789 Builder Rd")
                .priority(Priority.MEDIUM)
                .estimatedDuration(90)
                .status(TaskStatus.IN_PROGRESS)
                .createdBy("builder@test.com")
                .createdAt(now)
                .build();
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Builder Test", response.getTitle());
        assertEquals("Testing builder", response.getDescription());
        assertEquals("789 Builder Rd", response.getClientAddress());
        assertEquals(Priority.MEDIUM, response.getPriority());
        assertEquals(90, response.getEstimatedDuration());
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
        assertEquals("builder@test.com", response.getCreatedBy());
        assertEquals(now, response.getCreatedAt());
    }
    
    @Test
    void testNoArgsConstructor() {
        TaskResponse response = new TaskResponse();
        assertNotNull(response);
    }
    
    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        TaskResponse response = new TaskResponse(
                1L, "Title", "Description", "Address",
                Priority.HIGH, 60, TaskStatus.COMPLETED,
                "user@test.com", now);
        
        assertEquals(1L, response.getId());
        assertEquals("Title", response.getTitle());
        assertEquals("Description", response.getDescription());
        assertEquals("Address", response.getClientAddress());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(60, response.getEstimatedDuration());
        assertEquals(TaskStatus.COMPLETED, response.getStatus());
        assertEquals("user@test.com", response.getCreatedBy());
        assertEquals(now, response.getCreatedAt());
    }
    
    @Test
    void testSettersAndGetters() {
        TaskResponse response = new TaskResponse();
        LocalDateTime now = LocalDateTime.now();
        
        response.setId(1L);
        response.setTitle("Test Title");
        response.setDescription("Test Description");
        response.setClientAddress("Test Address");
        response.setPriority(Priority.HIGH);
        response.setEstimatedDuration(120);
        response.setStatus(TaskStatus.UNASSIGNED);
        response.setCreatedBy("test@example.com");
        response.setCreatedAt(now);
        
        assertEquals(1L, response.getId());
        assertEquals("Test Title", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals("Test Address", response.getClientAddress());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(120, response.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, response.getStatus());
        assertEquals("test@example.com", response.getCreatedBy());
        assertEquals(now, response.getCreatedAt());
    }
    
    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        TaskResponse response1 = TaskResponse.builder()
                .id(1L)
                .title("Test")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .createdAt(now)
                .build();
        
        TaskResponse response2 = TaskResponse.builder()
                .id(1L)
                .title("Test")
                .clientAddress("Address")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .createdAt(now)
                .build();
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
    
    @Test
    void testNotEquals() {
        TaskResponse response1 = TaskResponse.builder()
                .id(1L)
                .title("Test1")
                .build();
        
        TaskResponse response2 = TaskResponse.builder()
                .id(2L)
                .title("Test2")
                .build();
        
        assertNotEquals(response1, response2);
    }
    
    @Test
    void testToString() {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Task"));
    }
    
    @Test
    void testFromEntityWithAllStatuses() {
        for (TaskStatus status : TaskStatus.values()) {
            ServiceTask task = ServiceTask.builder()
                    .id(1L)
                    .title("Test")
                    .clientAddress("Address")
                    .priority(Priority.HIGH)
                    .status(status)
                    .build();
            
            TaskResponse response = TaskResponse.fromEntity(task);
            assertEquals(status, response.getStatus());
        }
    }
    
    @Test
    void testFromEntityWithAllPriorities() {
        for (Priority priority : Priority.values()) {
            ServiceTask task = ServiceTask.builder()
                    .id(1L)
                    .title("Test")
                    .clientAddress("Address")
                    .priority(priority)
                    .status(TaskStatus.UNASSIGNED)
                    .build();
            
            TaskResponse response = TaskResponse.fromEntity(task);
            assertEquals(priority, response.getPriority());
        }
    }
}
