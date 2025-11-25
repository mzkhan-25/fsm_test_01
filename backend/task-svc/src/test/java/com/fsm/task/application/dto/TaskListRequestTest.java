package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskListRequest DTO
 */
class TaskListRequestTest {
    
    @Test
    void testBuilderWithAllFields() {
        TaskListRequest request = TaskListRequest.builder()
                .status(TaskStatus.UNASSIGNED)
                .priority(Priority.HIGH)
                .search("HVAC")
                .sortBy("priority")
                .sortOrder("desc")
                .page(1)
                .pageSize(20)
                .build();
        
        assertEquals(TaskStatus.UNASSIGNED, request.getStatus());
        assertEquals(Priority.HIGH, request.getPriority());
        assertEquals("HVAC", request.getSearch());
        assertEquals("priority", request.getSortBy());
        assertEquals("desc", request.getSortOrder());
        assertEquals(1, request.getPage());
        assertEquals(20, request.getPageSize());
    }
    
    @Test
    void testDefaultValues() {
        TaskListRequest request = TaskListRequest.builder().build();
        
        assertNull(request.getStatus());
        assertNull(request.getPriority());
        assertNull(request.getSearch());
        assertEquals("priority", request.getSortBy());
        assertEquals("desc", request.getSortOrder());
        assertEquals(0, request.getPage());
        assertEquals(50, request.getPageSize());
    }
    
    @Test
    void testNoArgsConstructor() {
        TaskListRequest request = new TaskListRequest();
        
        assertNull(request.getStatus());
        assertNull(request.getPriority());
        assertNull(request.getSearch());
        // Default values from @Builder.Default are not applied in no-args constructor
    }
    
    @Test
    void testAllArgsConstructor() {
        TaskListRequest request = new TaskListRequest(
                TaskStatus.ASSIGNED,
                Priority.MEDIUM,
                "search term",
                "createdAt",
                "asc",
                2,
                25
        );
        
        assertEquals(TaskStatus.ASSIGNED, request.getStatus());
        assertEquals(Priority.MEDIUM, request.getPriority());
        assertEquals("search term", request.getSearch());
        assertEquals("createdAt", request.getSortBy());
        assertEquals("asc", request.getSortOrder());
        assertEquals(2, request.getPage());
        assertEquals(25, request.getPageSize());
    }
    
    @Test
    void testSettersAndGetters() {
        TaskListRequest request = new TaskListRequest();
        
        request.setStatus(TaskStatus.COMPLETED);
        request.setPriority(Priority.LOW);
        request.setSearch("test");
        request.setSortBy("status");
        request.setSortOrder("asc");
        request.setPage(5);
        request.setPageSize(100);
        
        assertEquals(TaskStatus.COMPLETED, request.getStatus());
        assertEquals(Priority.LOW, request.getPriority());
        assertEquals("test", request.getSearch());
        assertEquals("status", request.getSortBy());
        assertEquals("asc", request.getSortOrder());
        assertEquals(5, request.getPage());
        assertEquals(100, request.getPageSize());
    }
    
    @Test
    void testEqualsAndHashCode() {
        TaskListRequest request1 = TaskListRequest.builder()
                .status(TaskStatus.UNASSIGNED)
                .priority(Priority.HIGH)
                .page(0)
                .pageSize(50)
                .build();
        
        TaskListRequest request2 = TaskListRequest.builder()
                .status(TaskStatus.UNASSIGNED)
                .priority(Priority.HIGH)
                .page(0)
                .pageSize(50)
                .build();
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    void testToString() {
        TaskListRequest request = TaskListRequest.builder()
                .status(TaskStatus.UNASSIGNED)
                .priority(Priority.HIGH)
                .build();
        
        String toString = request.toString();
        
        assertTrue(toString.contains("status=UNASSIGNED"));
        assertTrue(toString.contains("priority=HIGH"));
    }
    
    @Test
    void testBuilderWithNullValues() {
        TaskListRequest request = TaskListRequest.builder()
                .status(null)
                .priority(null)
                .search(null)
                .build();
        
        assertNull(request.getStatus());
        assertNull(request.getPriority());
        assertNull(request.getSearch());
    }
    
    @Test
    void testDifferentStatusValues() {
        for (TaskStatus status : TaskStatus.values()) {
            TaskListRequest request = TaskListRequest.builder()
                    .status(status)
                    .build();
            
            assertEquals(status, request.getStatus());
        }
    }
    
    @Test
    void testDifferentPriorityValues() {
        for (Priority priority : Priority.values()) {
            TaskListRequest request = TaskListRequest.builder()
                    .priority(priority)
                    .build();
            
            assertEquals(priority, request.getPriority());
        }
    }
}
