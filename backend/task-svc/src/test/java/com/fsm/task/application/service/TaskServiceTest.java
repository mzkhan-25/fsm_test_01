package com.fsm.task.application.service;

import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import com.fsm.task.domain.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
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
}
