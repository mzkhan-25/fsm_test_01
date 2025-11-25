package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskListResponse DTO
 */
class TaskListResponseTest {
    
    @Test
    void testBuilderWithAllFields() {
        List<TaskResponse> tasks = createTaskResponses();
        Map<String, Long> statusCounts = createStatusCounts();
        
        TaskListResponse response = TaskListResponse.builder()
                .tasks(tasks)
                .page(0)
                .pageSize(50)
                .totalElements(2)
                .totalPages(1)
                .first(true)
                .last(true)
                .statusCounts(statusCounts)
                .build();
        
        assertEquals(tasks, response.getTasks());
        assertEquals(0, response.getPage());
        assertEquals(50, response.getPageSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        assertEquals(statusCounts, response.getStatusCounts());
    }
    
    @Test
    void testNoArgsConstructor() {
        TaskListResponse response = new TaskListResponse();
        
        assertNull(response.getTasks());
        assertEquals(0, response.getPage());
        assertEquals(0, response.getPageSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertNull(response.getStatusCounts());
    }
    
    @Test
    void testAllArgsConstructor() {
        List<TaskResponse> tasks = createTaskResponses();
        Map<String, Long> statusCounts = createStatusCounts();
        
        TaskListResponse response = new TaskListResponse(
                tasks, 1, 25, 50, 2, false, false, statusCounts
        );
        
        assertEquals(tasks, response.getTasks());
        assertEquals(1, response.getPage());
        assertEquals(25, response.getPageSize());
        assertEquals(50, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(statusCounts, response.getStatusCounts());
    }
    
    @Test
    void testSettersAndGetters() {
        TaskListResponse response = new TaskListResponse();
        List<TaskResponse> tasks = createTaskResponses();
        Map<String, Long> statusCounts = createStatusCounts();
        
        response.setTasks(tasks);
        response.setPage(2);
        response.setPageSize(100);
        response.setTotalElements(200);
        response.setTotalPages(2);
        response.setFirst(false);
        response.setLast(true);
        response.setStatusCounts(statusCounts);
        
        assertEquals(tasks, response.getTasks());
        assertEquals(2, response.getPage());
        assertEquals(100, response.getPageSize());
        assertEquals(200, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
        assertEquals(statusCounts, response.getStatusCounts());
    }
    
    @Test
    void testEqualsAndHashCode() {
        List<TaskResponse> tasks = createTaskResponses();
        Map<String, Long> statusCounts = createStatusCounts();
        
        TaskListResponse response1 = TaskListResponse.builder()
                .tasks(tasks)
                .page(0)
                .pageSize(50)
                .totalElements(2)
                .totalPages(1)
                .first(true)
                .last(true)
                .statusCounts(statusCounts)
                .build();
        
        TaskListResponse response2 = TaskListResponse.builder()
                .tasks(tasks)
                .page(0)
                .pageSize(50)
                .totalElements(2)
                .totalPages(1)
                .first(true)
                .last(true)
                .statusCounts(statusCounts)
                .build();
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
    
    @Test
    void testToString() {
        Map<String, Long> statusCounts = createStatusCounts();
        
        TaskListResponse response = TaskListResponse.builder()
                .tasks(Collections.emptyList())
                .page(0)
                .pageSize(50)
                .totalElements(100)
                .totalPages(2)
                .statusCounts(statusCounts)
                .build();
        
        String toString = response.toString();
        
        assertTrue(toString.contains("page=0"));
        assertTrue(toString.contains("pageSize=50"));
        assertTrue(toString.contains("totalElements=100"));
    }
    
    @Test
    void testEmptyTaskList() {
        TaskListResponse response = TaskListResponse.builder()
                .tasks(Collections.emptyList())
                .page(0)
                .pageSize(50)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .statusCounts(new HashMap<>())
                .build();
        
        assertTrue(response.getTasks().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
    }
    
    @Test
    void testStatusCountsContainsAllStatuses() {
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("UNASSIGNED", 5L);
        statusCounts.put("ASSIGNED", 3L);
        statusCounts.put("IN_PROGRESS", 2L);
        statusCounts.put("COMPLETED", 10L);
        
        TaskListResponse response = TaskListResponse.builder()
                .tasks(Collections.emptyList())
                .statusCounts(statusCounts)
                .build();
        
        assertEquals(5L, response.getStatusCounts().get("UNASSIGNED"));
        assertEquals(3L, response.getStatusCounts().get("ASSIGNED"));
        assertEquals(2L, response.getStatusCounts().get("IN_PROGRESS"));
        assertEquals(10L, response.getStatusCounts().get("COMPLETED"));
    }
    
    @Test
    void testPaginationFirstPage() {
        TaskListResponse response = TaskListResponse.builder()
                .tasks(Collections.emptyList())
                .page(0)
                .pageSize(50)
                .totalElements(100)
                .totalPages(2)
                .first(true)
                .last(false)
                .build();
        
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
    }
    
    @Test
    void testPaginationLastPage() {
        TaskListResponse response = TaskListResponse.builder()
                .tasks(Collections.emptyList())
                .page(1)
                .pageSize(50)
                .totalElements(100)
                .totalPages(2)
                .first(false)
                .last(true)
                .build();
        
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
    }
    
    @Test
    void testPaginationMiddlePage() {
        TaskListResponse response = TaskListResponse.builder()
                .tasks(Collections.emptyList())
                .page(1)
                .pageSize(50)
                .totalElements(150)
                .totalPages(3)
                .first(false)
                .last(false)
                .build();
        
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
    }
    
    // Helper methods
    private List<TaskResponse> createTaskResponses() {
        TaskResponse task1 = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .createdAt(LocalDateTime.now())
                .build();
        
        TaskResponse task2 = TaskResponse.builder()
                .id(2L)
                .title("Task 2")
                .clientAddress("456 Oak Ave")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.ASSIGNED)
                .createdAt(LocalDateTime.now())
                .build();
        
        return Arrays.asList(task1, task2);
    }
    
    private Map<String, Long> createStatusCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("UNASSIGNED", 1L);
        counts.put("ASSIGNED", 1L);
        counts.put("IN_PROGRESS", 0L);
        counts.put("COMPLETED", 0L);
        return counts;
    }
}
