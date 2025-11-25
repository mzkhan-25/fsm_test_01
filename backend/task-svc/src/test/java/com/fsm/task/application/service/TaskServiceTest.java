package com.fsm.task.application.service;

import com.fsm.task.application.dto.AssignTaskRequest;
import com.fsm.task.application.dto.AssignTaskResponse;
import com.fsm.task.application.dto.CreateTaskRequest;
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

import java.time.LocalDateTime;
import java.util.Optional;

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
    
    // ==================== Task Assignment Tests ====================
    
    @Test
    void testAssignTaskSuccess() {
        // Setup - unassigned task
        ServiceTask unassignedTask = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        Assignment savedAssignment = Assignment.builder()
                .id(1L)
                .taskId(1L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(assignmentRepository.getTechnicianWorkload(101L)).thenReturn(1);
        
        AssignTaskRequest request = AssignTaskRequest.builder().technicianId(101L).build();
        
        AssignTaskResponse response = taskService.assignTask(1L, request, "dispatcher@fsm.com");
        
        assertNotNull(response);
        assertEquals(1L, response.getAssignmentId());
        assertEquals(1L, response.getTaskId());
        assertEquals(101L, response.getTechnicianId());
        assertEquals(TaskStatus.ASSIGNED, response.getTaskStatus());
        assertEquals(1, response.getTechnicianWorkload());
        assertNull(response.getWorkloadWarning());
        
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(ServiceTask.class));
        verify(assignmentRepository).save(any(Assignment.class));
    }
    
    @Test
    void testAssignTaskWithWorkloadWarning() {
        // Setup - unassigned task
        ServiceTask unassignedTask = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        Assignment savedAssignment = Assignment.builder()
                .id(1L)
                .taskId(1L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(assignmentRepository.getTechnicianWorkload(101L)).thenReturn(12); // Exceeds threshold of 10
        
        AssignTaskRequest request = AssignTaskRequest.builder().technicianId(101L).build();
        
        AssignTaskResponse response = taskService.assignTask(1L, request, "dispatcher@fsm.com");
        
        assertNotNull(response);
        assertEquals(12, response.getTechnicianWorkload());
        assertNotNull(response.getWorkloadWarning());
        assertTrue(response.getWorkloadWarning().contains("12"));
        assertTrue(response.getWorkloadWarning().contains("10"));
    }
    
    @Test
    void testAssignTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        AssignTaskRequest request = AssignTaskRequest.builder().technicianId(101L).build();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> taskService.assignTask(999L, request, "dispatcher@fsm.com"));
        
        assertEquals("Task not found with ID: 999", exception.getMessage());
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    void testAssignTaskCannotAssignInProgressTask() {
        // Setup - task in progress
        ServiceTask inProgressTask = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(inProgressTask));
        
        AssignTaskRequest request = AssignTaskRequest.builder().technicianId(101L).build();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> taskService.assignTask(1L, request, "dispatcher@fsm.com"));
        
        assertTrue(exception.getMessage().contains("cannot be assigned"));
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    void testAssignTaskCannotAssignCompletedTask() {
        // Setup - completed task
        ServiceTask completedTask = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.COMPLETED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(completedTask));
        
        AssignTaskRequest request = AssignTaskRequest.builder().technicianId(101L).build();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> taskService.assignTask(1L, request, "dispatcher@fsm.com"));
        
        assertTrue(exception.getMessage().contains("cannot be assigned"));
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    void testReassignTask() {
        // Setup - already assigned task
        ServiceTask assignedTask = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.ASSIGNED)
                .assignedTechnicianId(100L)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        Assignment previousAssignment = Assignment.builder()
                .id(1L)
                .taskId(1L)
                .technicianId(100L)
                .assignedAt(LocalDateTime.now().minusDays(1))
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment newAssignment = Assignment.builder()
                .id(2L)
                .taskId(1L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(assignedTask));
        when(assignmentRepository.findByTaskIdAndStatus(1L, AssignmentStatus.ACTIVE))
                .thenReturn(Optional.of(previousAssignment));
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment arg = invocation.getArgument(0);
            if (arg.getId() == null) {
                return newAssignment; // New assignment
            }
            return arg; // Previous assignment being marked as reassigned
        });
        when(assignmentRepository.getTechnicianWorkload(102L)).thenReturn(3);
        
        AssignTaskRequest request = AssignTaskRequest.builder().technicianId(102L).build();
        
        AssignTaskResponse response = taskService.assignTask(1L, request, "dispatcher@fsm.com");
        
        assertNotNull(response);
        assertEquals(102L, response.getTechnicianId());
        assertEquals(TaskStatus.ASSIGNED, response.getTaskStatus());
        
        // Verify previous assignment was marked as reassigned
        verify(assignmentRepository, times(2)).save(any(Assignment.class));
    }
    
    @Test
    void testAssignTaskCreatesAssignmentRecord() {
        ServiceTask unassignedTask = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment assignment = invocation.getArgument(0);
            assignment.setId(1L);
            return assignment;
        });
        when(assignmentRepository.getTechnicianWorkload(101L)).thenReturn(1);
        
        AssignTaskRequest request = AssignTaskRequest.builder().technicianId(101L).build();
        
        taskService.assignTask(1L, request, "dispatcher@fsm.com");
        
        ArgumentCaptor<Assignment> assignmentCaptor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository).save(assignmentCaptor.capture());
        
        Assignment capturedAssignment = assignmentCaptor.getValue();
        assertEquals(1L, capturedAssignment.getTaskId());
        assertEquals(101L, capturedAssignment.getTechnicianId());
        assertEquals("dispatcher@fsm.com", capturedAssignment.getAssignedBy());
        assertEquals(AssignmentStatus.ACTIVE, capturedAssignment.getStatus());
    }
    
    @Test
    void testAssignTaskUpdatesTaskStatus() {
        ServiceTask unassignedTask = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment assignment = invocation.getArgument(0);
            assignment.setId(1L);
            return assignment;
        });
        when(assignmentRepository.getTechnicianWorkload(101L)).thenReturn(1);
        
        AssignTaskRequest request = AssignTaskRequest.builder().technicianId(101L).build();
        
        taskService.assignTask(1L, request, "dispatcher@fsm.com");
        
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        
        ServiceTask capturedTask = taskCaptor.getValue();
        assertEquals(TaskStatus.ASSIGNED, capturedTask.getStatus());
        assertEquals(101L, capturedTask.getAssignedTechnicianId());
    }
    
    @Test
    void testAssignTaskWorkloadAtThreshold() {
        ServiceTask unassignedTask = ServiceTask.builder()
                .id(1L)
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        Assignment savedAssignment = Assignment.builder()
                .id(1L)
                .taskId(1L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(assignmentRepository.getTechnicianWorkload(101L)).thenReturn(10); // Exactly at threshold
        
        AssignTaskRequest request = AssignTaskRequest.builder().technicianId(101L).build();
        
        AssignTaskResponse response = taskService.assignTask(1L, request, "dispatcher@fsm.com");
        
        assertNotNull(response);
        assertEquals(10, response.getTechnicianWorkload());
        assertNull(response.getWorkloadWarning()); // No warning at exactly threshold
    }
}
