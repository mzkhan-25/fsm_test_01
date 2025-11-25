package com.fsm.task.application.service;

import com.fsm.task.application.dto.AssignTaskRequest;
import com.fsm.task.application.dto.AssignTaskResponse;
import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.TaskListRequest;
import com.fsm.task.application.dto.TaskListResponse;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.domain.model.Assignment;
import com.fsm.task.domain.model.Assignment.AssignmentStatus;
import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import com.fsm.task.domain.repository.AssignmentRepository;
import com.fsm.task.domain.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private AssignmentRepository assignmentRepository;
    
    @InjectMocks
    private TaskService taskService;
    
    private CreateTaskRequest validRequest;
    private ServiceTask savedTask;
    
    @BeforeEach
    void setUp() {
        validRequest = CreateTaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .build();
        
        savedTask = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void testCreateTaskSuccess() {
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(savedTask);
        
        TaskResponse response = taskService.createTask(validRequest, "dispatcher@fsm.com");
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Test Description", response.getDescription());
        assertEquals("123 Main St", response.getClientAddress());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(120, response.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, response.getStatus());
        assertEquals("dispatcher@fsm.com", response.getCreatedBy());
        
        verify(taskRepository, times(1)).save(any(ServiceTask.class));
    }
    
    @Test
    void testCreateTaskSetsCreatedBy() {
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> {
            ServiceTask task = invocation.getArgument(0);
            task.setId(1L);
            task.setCreatedAt(LocalDateTime.now());
            return task;
        });
        
        TaskResponse response = taskService.createTask(validRequest, "admin@fsm.com");
        
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        
        ServiceTask capturedTask = taskCaptor.getValue();
        assertEquals("admin@fsm.com", capturedTask.getCreatedBy());
    }
    
    @Test
    void testCreateTaskSetsUnassignedStatus() {
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> {
            ServiceTask task = invocation.getArgument(0);
            task.setId(1L);
            task.setCreatedAt(LocalDateTime.now());
            return task;
        });
        
        taskService.createTask(validRequest, "dispatcher@fsm.com");
        
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        
        ServiceTask capturedTask = taskCaptor.getValue();
        assertEquals(TaskStatus.UNASSIGNED, capturedTask.getStatus());
    }
    
    @Test
    void testCreateTaskWithMinimalRequest() {
        CreateTaskRequest minimalRequest = CreateTaskRequest.builder()
                .title("Minimal Task")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .build();
        
        ServiceTask minimalSavedTask = ServiceTask.builder()
                .id(2L)
                .title("Minimal Task")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("tech@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(minimalSavedTask);
        
        TaskResponse response = taskService.createTask(minimalRequest, "tech@fsm.com");
        
        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Minimal Task", response.getTitle());
        assertNull(response.getDescription());
        assertEquals("456 Test Ave", response.getClientAddress());
        assertEquals(Priority.LOW, response.getPriority());
        assertNull(response.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, response.getStatus());
    }
    
    @Test
    void testCreateTaskWithAllPriorities() {
        for (Priority priority : Priority.values()) {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Priority Test")
                    .clientAddress("Test Address")
                    .priority(priority)
                    .build();
            
            ServiceTask task = ServiceTask.builder()
                    .id(1L)
                    .title("Priority Test")
                    .clientAddress("Test Address")
                    .priority(priority)
                    .status(TaskStatus.UNASSIGNED)
                    .createdBy("test@fsm.com")
                    .createdAt(LocalDateTime.now())
                    .build();
            
            when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
            
            TaskResponse response = taskService.createTask(request, "test@fsm.com");
            
            assertEquals(priority, response.getPriority());
        }
    }
    
    @Test
    void testCreateTaskCopiesAllFieldsFromRequest() {
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> {
            ServiceTask task = invocation.getArgument(0);
            task.setId(1L);
            task.setCreatedAt(LocalDateTime.now());
            return task;
        });
        
        taskService.createTask(validRequest, "dispatcher@fsm.com");
        
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        
        ServiceTask capturedTask = taskCaptor.getValue();
        assertEquals("Test Task", capturedTask.getTitle());
        assertEquals("Test Description", capturedTask.getDescription());
        assertEquals("123 Main St", capturedTask.getClientAddress());
        assertEquals(Priority.HIGH, capturedTask.getPriority());
        assertEquals(120, capturedTask.getEstimatedDuration());
    }
    
    @Test
    void testCreateTaskWithNullDescription() {
        CreateTaskRequest requestWithoutDesc = CreateTaskRequest.builder()
                .title("No Description Task")
                .clientAddress("789 Test Blvd")
                .priority(Priority.MEDIUM)
                .description(null)
                .build();
        
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> {
            ServiceTask task = invocation.getArgument(0);
            task.setId(1L);
            task.setCreatedAt(LocalDateTime.now());
            return task;
        });
        
        taskService.createTask(requestWithoutDesc, "user@fsm.com");
        
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        
        ServiceTask capturedTask = taskCaptor.getValue();
        assertNull(capturedTask.getDescription());
    }
    
    @Test
    void testCreateTaskWithNullEstimatedDuration() {
        CreateTaskRequest requestWithoutDuration = CreateTaskRequest.builder()
                .title("No Duration Task")
                .clientAddress("101 Test Ln")
                .priority(Priority.LOW)
                .estimatedDuration(null)
                .build();
        
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> {
            ServiceTask task = invocation.getArgument(0);
            task.setId(1L);
            task.setCreatedAt(LocalDateTime.now());
            return task;
        });
        
        taskService.createTask(requestWithoutDuration, "user@fsm.com");
        
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        
        ServiceTask capturedTask = taskCaptor.getValue();
        assertNull(capturedTask.getEstimatedDuration());
    }
    
    @Test
    void testRepositorySaveIsCalled() {
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(savedTask);
        
        taskService.createTask(validRequest, "dispatcher@fsm.com");
        
        verify(taskRepository, times(1)).save(any(ServiceTask.class));
    }
    
    @Test
    void testCreateTaskReturnsCreatedTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        savedTask.setCreatedAt(now);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(savedTask);
        
        TaskResponse response = taskService.createTask(validRequest, "dispatcher@fsm.com");
        
        assertEquals(now, response.getCreatedAt());
    }
    
    // ============== Tests for getTasks ==============
    
    @Test
    void testGetTasksReturnsEmptyListWhenNoTasks() {
        TaskListRequest request = TaskListRequest.builder().build();
        Page<ServiceTask> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 50), 0);
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        assertTrue(response.getTasks().isEmpty());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }
    
    @Test
    void testGetTasksReturnsTasksWithPagination() {
        TaskListRequest request = TaskListRequest.builder()
                .page(0)
                .pageSize(10)
                .build();
        
        List<ServiceTask> tasks = Arrays.asList(
                createTask(1L, "Task 1", Priority.HIGH, TaskStatus.UNASSIGNED),
                createTask(2L, "Task 2", Priority.MEDIUM, TaskStatus.ASSIGNED)
        );
        Page<ServiceTask> taskPage = new PageImpl<>(tasks, Pageable.ofSize(10), 2);
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(TaskStatus.UNASSIGNED)).thenReturn(1L);
        when(taskRepository.countByStatus(TaskStatus.ASSIGNED)).thenReturn(1L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(0L);
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        assertEquals(2, response.getTasks().size());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getPageSize());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }
    
    @Test
    void testGetTasksWithStatusFilter() {
        TaskListRequest request = TaskListRequest.builder()
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        List<ServiceTask> tasks = Collections.singletonList(
                createTask(1L, "Unassigned Task", Priority.HIGH, TaskStatus.UNASSIGNED)
        );
        Page<ServiceTask> taskPage = new PageImpl<>(tasks);
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        when(taskRepository.countByStatus(TaskStatus.UNASSIGNED)).thenReturn(1L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        assertEquals(TaskStatus.UNASSIGNED, response.getTasks().get(0).getStatus());
    }
    
    @Test
    void testGetTasksWithPriorityFilter() {
        TaskListRequest request = TaskListRequest.builder()
                .priority(Priority.HIGH)
                .build();
        
        List<ServiceTask> tasks = Collections.singletonList(
                createTask(1L, "High Priority Task", Priority.HIGH, TaskStatus.UNASSIGNED)
        );
        Page<ServiceTask> taskPage = new PageImpl<>(tasks);
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        assertEquals(Priority.HIGH, response.getTasks().get(0).getPriority());
    }
    
    @Test
    void testGetTasksWithSearchTerm() {
        TaskListRequest request = TaskListRequest.builder()
                .search("HVAC")
                .build();
        
        List<ServiceTask> tasks = Collections.singletonList(
                createTask(1L, "HVAC Repair", Priority.HIGH, TaskStatus.UNASSIGNED)
        );
        Page<ServiceTask> taskPage = new PageImpl<>(tasks);
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        assertTrue(response.getTasks().get(0).getTitle().contains("HVAC"));
    }
    
    @Test
    void testGetTasksWithSortByCreatedAt() {
        TaskListRequest request = TaskListRequest.builder()
                .sortBy("createdAt")
                .sortOrder("desc")
                .build();
        
        Page<ServiceTask> taskPage = new PageImpl<>(Collections.emptyList());
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        verify(taskRepository).findAll(any(Specification.class), any(Pageable.class));
    }
    
    @Test
    void testGetTasksWithSortByStatus() {
        TaskListRequest request = TaskListRequest.builder()
                .sortBy("status")
                .sortOrder("asc")
                .build();
        
        Page<ServiceTask> taskPage = new PageImpl<>(Collections.emptyList());
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        verify(taskRepository).findAll(any(Specification.class), any(Pageable.class));
    }
    
    @Test
    void testGetTasksReturnsStatusCounts() {
        TaskListRequest request = TaskListRequest.builder().build();
        Page<ServiceTask> taskPage = new PageImpl<>(Collections.emptyList());
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(TaskStatus.UNASSIGNED)).thenReturn(5L);
        when(taskRepository.countByStatus(TaskStatus.ASSIGNED)).thenReturn(3L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(2L);
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(10L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response.getStatusCounts());
        assertEquals(5L, response.getStatusCounts().get("UNASSIGNED"));
        assertEquals(3L, response.getStatusCounts().get("ASSIGNED"));
        assertEquals(2L, response.getStatusCounts().get("IN_PROGRESS"));
        assertEquals(10L, response.getStatusCounts().get("COMPLETED"));
    }
    
    @Test
    void testGetTasksDefaultSorting() {
        TaskListRequest request = TaskListRequest.builder().build();
        Page<ServiceTask> taskPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 50), 0);
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        // Default values should be used
        assertEquals(0, response.getPage());
        assertEquals(50, response.getPageSize());
    }
    
    @Test
    void testGetTasksWithCombinedFilters() {
        TaskListRequest request = TaskListRequest.builder()
                .status(TaskStatus.UNASSIGNED)
                .priority(Priority.HIGH)
                .search("urgent")
                .page(0)
                .pageSize(20)
                .build();
        
        List<ServiceTask> tasks = Collections.singletonList(
                createTask(1L, "Urgent HVAC", Priority.HIGH, TaskStatus.UNASSIGNED)
        );
        Page<ServiceTask> taskPage = new PageImpl<>(tasks, Pageable.ofSize(20), 1);
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        assertEquals(1, response.getTotalElements());
    }
    
    @Test
    void testGetTasksMultiplePagesFirstPage() {
        TaskListRequest request = TaskListRequest.builder()
                .page(0)
                .pageSize(2)
                .build();
        
        List<ServiceTask> tasks = Arrays.asList(
                createTask(1L, "Task 1", Priority.HIGH, TaskStatus.UNASSIGNED),
                createTask(2L, "Task 2", Priority.MEDIUM, TaskStatus.ASSIGNED)
        );
        Page<ServiceTask> taskPage = new PageImpl<>(tasks, Pageable.ofSize(2).withPage(0), 5);
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        assertEquals(2, response.getTasks().size());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
    }
    
    @Test
    void testGetTasksMultiplePagesLastPage() {
        TaskListRequest request = TaskListRequest.builder()
                .page(2)
                .pageSize(2)
                .build();
        
        List<ServiceTask> tasks = Collections.singletonList(
                createTask(5L, "Task 5", Priority.LOW, TaskStatus.COMPLETED)
        );
        Page<ServiceTask> taskPage = new PageImpl<>(tasks, Pageable.ofSize(2).withPage(2), 5);
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isFirst());
        assertTrue(response.isLast());
    }
    
    @Test
    void testGetTasksWithNullSortBy() {
        TaskListRequest request = TaskListRequest.builder()
                .sortBy(null)
                .build();
        
        Page<ServiceTask> taskPage = new PageImpl<>(Collections.emptyList());
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        verify(taskRepository).findAll(any(Specification.class), any(Pageable.class));
    }
    
    @Test
    void testGetTasksWithAscendingSort() {
        TaskListRequest request = TaskListRequest.builder()
                .sortBy("priority")
                .sortOrder("asc")
                .build();
        
        Page<ServiceTask> taskPage = new PageImpl<>(Collections.emptyList());
        
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);
        
        TaskListResponse response = taskService.getTasks(request);
        
        assertNotNull(response);
        verify(taskRepository).findAll(any(Specification.class), any(Pageable.class));
    }
    
    // Helper method to create test tasks
    private ServiceTask createTask(Long id, String title, Priority priority, TaskStatus status) {
        return ServiceTask.builder()
                .id(id)
                .title(title)
                .description("Test Description")
                .clientAddress("123 Test St")
                .priority(priority)
                .status(status)
                .createdBy("test@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
