package com.fsm.task.application.service;

import com.fsm.task.application.dto.AssignTaskRequest;
import com.fsm.task.application.dto.AssignTaskResponse;
import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.TaskListRequest;
import com.fsm.task.application.dto.TaskListResponse;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.application.dto.TechnicianTaskListResponse;
import com.fsm.task.application.dto.TechnicianTaskResponse;
import com.fsm.task.application.exception.InvalidAssignmentException;
import com.fsm.task.application.exception.TaskNotFoundException;
import com.fsm.task.application.exception.TechnicianNotFoundException;
import com.fsm.task.domain.model.Assignment;
import com.fsm.task.domain.model.Assignment.AssignmentStatus;
import com.fsm.task.domain.model.AssignmentHistory;
import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import com.fsm.task.domain.repository.AssignmentHistoryRepository;
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
    
    @Mock
    private AssignmentHistoryRepository assignmentHistoryRepository;
    
    @Mock
    private TechnicianValidationService technicianValidationService;
    
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
    
    // ============== Tests for assignTask ==============
    
    @Test
    void testAssignTaskSuccess() {
        Long taskId = 1L;
        Long technicianId = 101L;
        String assignedBy = "dispatcher@fsm.com";
        
        ServiceTask task = createTask(taskId, "Test Task", Priority.HIGH, TaskStatus.UNASSIGNED);
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .build();
        
        Assignment savedAssignment = Assignment.builder()
                .id(1L)
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class))).thenReturn(null);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
        when(assignmentRepository.getTechnicianWorkload(technicianId)).thenReturn(1);
        
        AssignTaskResponse response = taskService.assignTask(taskId, request, assignedBy);
        
        assertNotNull(response);
        assertEquals(1L, response.getAssignmentId());
        assertEquals(taskId, response.getTaskId());
        assertEquals(technicianId, response.getTechnicianId());
        assertEquals(assignedBy, response.getAssignedBy());
        assertEquals(TaskStatus.ASSIGNED, response.getTaskStatus());
        assertEquals(1, response.getTechnicianWorkload());
        assertNull(response.getWorkloadWarning());
        
        verify(assignmentRepository).save(any(Assignment.class));
        verify(assignmentHistoryRepository).save(any(AssignmentHistory.class));
        verify(taskRepository).save(any(ServiceTask.class));
    }
    
    @Test
    void testAssignTaskToTaskNotFound() {
        Long taskId = 999L;
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(101L)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        
        assertThrows(TaskNotFoundException.class, 
                () -> taskService.assignTask(taskId, request, "dispatcher@fsm.com"));
        
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    void testAssignTaskToCompletedTaskFails() {
        Long taskId = 1L;
        ServiceTask completedTask = createTask(taskId, "Completed Task", Priority.HIGH, TaskStatus.COMPLETED);
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(101L)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(completedTask));
        
        assertThrows(InvalidAssignmentException.class, 
                () -> taskService.assignTask(taskId, request, "dispatcher@fsm.com"));
        
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    void testAssignTaskToInProgressTaskFails() {
        Long taskId = 1L;
        ServiceTask inProgressTask = createTask(taskId, "In Progress Task", Priority.HIGH, TaskStatus.IN_PROGRESS);
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(101L)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(inProgressTask));
        
        assertThrows(InvalidAssignmentException.class, 
                () -> taskService.assignTask(taskId, request, "dispatcher@fsm.com"));
        
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    void testReassignTaskSuccess() {
        Long taskId = 1L;
        Long previousTechnicianId = 100L;
        Long newTechnicianId = 101L;
        String assignedBy = "dispatcher@fsm.com";
        
        ServiceTask assignedTask = ServiceTask.builder()
                .id(taskId)
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.ASSIGNED)
                .assignedTechnicianId(previousTechnicianId)
                .createdBy("test@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        Assignment previousAssignment = Assignment.builder()
                .id(1L)
                .taskId(taskId)
                .technicianId(previousTechnicianId)
                .assignedAt(LocalDateTime.now().minusDays(1))
                .assignedBy(assignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(newTechnicianId)
                .build();
        
        Assignment newAssignment = Assignment.builder()
                .id(2L)
                .taskId(taskId)
                .technicianId(newTechnicianId)
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(assignedTask));
        when(assignmentRepository.findActiveAssignmentForTask(taskId)).thenReturn(Optional.of(previousAssignment));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment a = invocation.getArgument(0);
            if (a.getId() == null) {
                return newAssignment;
            }
            return a;
        });
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class))).thenReturn(null);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(assignedTask);
        when(assignmentRepository.getTechnicianWorkload(newTechnicianId)).thenReturn(2);
        
        AssignTaskResponse response = taskService.assignTask(taskId, request, assignedBy);
        
        assertNotNull(response);
        assertEquals(2L, response.getAssignmentId());
        assertEquals(newTechnicianId, response.getTechnicianId());
        assertEquals(2, response.getTechnicianWorkload());
        
        // Verify previous assignment was marked as reassigned
        verify(assignmentRepository, times(2)).save(any(Assignment.class));
        verify(assignmentHistoryRepository).save(any(AssignmentHistory.class));
    }
    
    @Test
    void testAssignTaskWithHighWorkloadReturnsWarning() {
        Long taskId = 1L;
        Long technicianId = 101L;
        String assignedBy = "dispatcher@fsm.com";
        
        ServiceTask task = createTask(taskId, "Test Task", Priority.HIGH, TaskStatus.UNASSIGNED);
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .build();
        
        Assignment savedAssignment = Assignment.builder()
                .id(1L)
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class))).thenReturn(null);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
        when(assignmentRepository.getTechnicianWorkload(technicianId)).thenReturn(15); // Exceeds threshold of 10
        
        AssignTaskResponse response = taskService.assignTask(taskId, request, assignedBy);
        
        assertNotNull(response);
        assertEquals(15, response.getTechnicianWorkload());
        assertNotNull(response.getWorkloadWarning());
        assertTrue(response.getWorkloadWarning().contains("exceeds"));
    }
    
    @Test
    void testAssignTaskCreatesHistoryRecord() {
        Long taskId = 1L;
        Long technicianId = 101L;
        String assignedBy = "dispatcher@fsm.com";
        
        ServiceTask task = createTask(taskId, "Test Task", Priority.HIGH, TaskStatus.UNASSIGNED);
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .build();
        
        Assignment savedAssignment = Assignment.builder()
                .id(1L)
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class))).thenReturn(null);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
        when(assignmentRepository.getTechnicianWorkload(technicianId)).thenReturn(1);
        
        taskService.assignTask(taskId, request, assignedBy);
        
        ArgumentCaptor<AssignmentHistory> historyCaptor = ArgumentCaptor.forClass(AssignmentHistory.class);
        verify(assignmentHistoryRepository).save(historyCaptor.capture());
        
        AssignmentHistory savedHistory = historyCaptor.getValue();
        assertEquals(taskId, savedHistory.getTaskId());
        assertEquals(technicianId, savedHistory.getTechnicianId());
        assertEquals(AssignmentHistory.HistoryAction.CREATED, savedHistory.getAction());
        assertEquals(assignedBy, savedHistory.getActionBy());
    }
    
    @Test
    void testAssignTaskUpdatesTaskAssignedTechnician() {
        Long taskId = 1L;
        Long technicianId = 101L;
        String assignedBy = "dispatcher@fsm.com";
        
        ServiceTask task = createTask(taskId, "Test Task", Priority.HIGH, TaskStatus.UNASSIGNED);
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .build();
        
        Assignment savedAssignment = Assignment.builder()
                .id(1L)
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class))).thenReturn(null);
        when(taskRepository.save(any(ServiceTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(assignmentRepository.getTechnicianWorkload(technicianId)).thenReturn(1);
        
        taskService.assignTask(taskId, request, assignedBy);
        
        ArgumentCaptor<ServiceTask> taskCaptor = ArgumentCaptor.forClass(ServiceTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        
        ServiceTask savedTask = taskCaptor.getValue();
        assertEquals(technicianId, savedTask.getAssignedTechnicianId());
        assertEquals(TaskStatus.ASSIGNED, savedTask.getStatus());
    }
    
    @Test
    void testAssignTaskTechnicianNotFoundThrowsException() {
        Long taskId = 1L;
        Long technicianId = 999L;
        String assignedBy = "dispatcher@fsm.com";
        
        ServiceTask task = createTask(taskId, "Test Task", Priority.HIGH, TaskStatus.UNASSIGNED);
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        doThrow(new TechnicianNotFoundException(technicianId))
                .when(technicianValidationService).validateTechnician(technicianId);
        
        TechnicianNotFoundException exception = assertThrows(
                TechnicianNotFoundException.class,
                () -> taskService.assignTask(taskId, request, assignedBy)
        );
        
        assertEquals(technicianId, exception.getTechnicianId());
        assertTrue(exception.getMessage().contains("not found"));
        
        verify(assignmentRepository, never()).save(any());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testAssignTaskTechnicianInactiveThrowsException() {
        Long taskId = 1L;
        Long technicianId = 102L;
        String assignedBy = "dispatcher@fsm.com";
        
        ServiceTask task = createTask(taskId, "Test Task", Priority.HIGH, TaskStatus.UNASSIGNED);
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        doThrow(new TechnicianNotFoundException(technicianId, "is not active"))
                .when(technicianValidationService).validateTechnician(technicianId);
        
        TechnicianNotFoundException exception = assertThrows(
                TechnicianNotFoundException.class,
                () -> taskService.assignTask(taskId, request, assignedBy)
        );
        
        assertEquals(technicianId, exception.getTechnicianId());
        assertTrue(exception.getMessage().contains("not active"));
        
        verify(assignmentRepository, never()).save(any());
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void testAssignTaskCallsTechnicianValidation() {
        Long taskId = 1L;
        Long technicianId = 101L;
        String assignedBy = "dispatcher@fsm.com";
        
        ServiceTask task = createTask(taskId, "Test Task", Priority.HIGH, TaskStatus.UNASSIGNED);
        AssignTaskRequest request = AssignTaskRequest.builder()
                .technicianId(technicianId)
                .build();
        
        Assignment savedAssignment = Assignment.builder()
                .id(1L)
                .taskId(taskId)
                .technicianId(technicianId)
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        // Default mock behavior - do nothing (validation passes)
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(savedAssignment);
        when(assignmentHistoryRepository.save(any(AssignmentHistory.class))).thenReturn(null);
        when(taskRepository.save(any(ServiceTask.class))).thenReturn(task);
        when(assignmentRepository.getTechnicianWorkload(technicianId)).thenReturn(1);
        
        taskService.assignTask(taskId, request, assignedBy);
        
        // Verify that technician validation was called
        verify(technicianValidationService).validateTechnician(technicianId);
    }
    
    // ============== Tests for getTechnicianTasks ==============
    
    @Test
    void testGetTechnicianTasksReturnsEmptyListWhenNoTasks() {
        Long technicianId = 101L;
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId)).thenReturn(Collections.emptyList());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "all");
        
        assertNotNull(response);
        assertTrue(response.getTasks().isEmpty());
        assertEquals(0, response.getTotalTasks());
    }
    
    @Test
    void testGetTechnicianTasksReturnsAssignedTasks() {
        Long technicianId = 101L;
        ServiceTask task1 = createTechnicianTask(1L, "Task 1", Priority.HIGH, TaskStatus.ASSIGNED, technicianId);
        ServiceTask task2 = createTechnicianTask(2L, "Task 2", Priority.MEDIUM, TaskStatus.ASSIGNED, technicianId);
        
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId))
                .thenReturn(Arrays.asList(task1, task2));
        when(assignmentRepository.findActiveAssignmentForTask(any())).thenReturn(Optional.empty());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "all");
        
        assertNotNull(response);
        assertEquals(2, response.getTasks().size());
        assertEquals(2, response.getTotalTasks());
        assertEquals("Task 1", response.getTasks().get(0).getTitle());
        assertEquals("Task 2", response.getTasks().get(1).getTitle());
    }
    
    @Test
    void testGetTechnicianTasksWithStatusFilterAssigned() {
        Long technicianId = 101L;
        ServiceTask task = createTechnicianTask(1L, "Assigned Task", Priority.HIGH, TaskStatus.ASSIGNED, technicianId);
        
        when(taskRepository.findByTechnicianIdAndStatusOrderedByPriority(technicianId, TaskStatus.ASSIGNED))
                .thenReturn(Collections.singletonList(task));
        when(assignmentRepository.findActiveAssignmentForTask(any())).thenReturn(Optional.empty());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "assigned");
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        assertEquals(TaskStatus.ASSIGNED, response.getTasks().get(0).getStatus());
        
        verify(taskRepository).findByTechnicianIdAndStatusOrderedByPriority(technicianId, TaskStatus.ASSIGNED);
    }
    
    @Test
    void testGetTechnicianTasksWithStatusFilterInProgress() {
        Long technicianId = 101L;
        ServiceTask task = createTechnicianTask(1L, "In Progress Task", Priority.MEDIUM, TaskStatus.IN_PROGRESS, technicianId);
        
        when(taskRepository.findByTechnicianIdAndStatusOrderedByPriority(technicianId, TaskStatus.IN_PROGRESS))
                .thenReturn(Collections.singletonList(task));
        when(assignmentRepository.findActiveAssignmentForTask(any())).thenReturn(Optional.empty());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "in_progress");
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        assertEquals(TaskStatus.IN_PROGRESS, response.getTasks().get(0).getStatus());
    }
    
    @Test
    void testGetTechnicianTasksWithStatusFilterCompleted() {
        Long technicianId = 101L;
        // Completed today - should be included
        ServiceTask completedToday = ServiceTask.builder()
                .id(1L)
                .title("Completed Today")
                .clientAddress("123 Test St")
                .priority(Priority.LOW)
                .status(TaskStatus.COMPLETED)
                .assignedTechnicianId(technicianId)
                .createdBy("test@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findByTechnicianIdAndStatusOrderedByPriority(technicianId, TaskStatus.COMPLETED))
                .thenReturn(Collections.singletonList(completedToday));
        when(assignmentRepository.findByTaskIdAndStatus(1L, AssignmentStatus.COMPLETED)).thenReturn(Optional.empty());
        when(assignmentRepository.findActiveAssignmentForTask(any())).thenReturn(Optional.empty());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "completed");
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
    }
    
    @Test
    void testGetTechnicianTasksExcludesCompletedFromPreviousDays() {
        Long technicianId = 101L;
        // Completed yesterday - should be excluded
        ServiceTask completedYesterday = ServiceTask.builder()
                .id(1L)
                .title("Completed Yesterday")
                .clientAddress("123 Test St")
                .priority(Priority.LOW)
                .status(TaskStatus.COMPLETED)
                .assignedTechnicianId(technicianId)
                .createdBy("test@fsm.com")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId))
                .thenReturn(Collections.singletonList(completedYesterday));
        when(assignmentRepository.findByTaskIdAndStatus(1L, AssignmentStatus.COMPLETED)).thenReturn(Optional.empty());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "all");
        
        assertNotNull(response);
        assertEquals(0, response.getTasks().size());
    }
    
    @Test
    void testGetTechnicianTasksIncludesNonCompletedFromPreviousDays() {
        Long technicianId = 101L;
        // Assigned yesterday - should be included
        ServiceTask assignedYesterday = ServiceTask.builder()
                .id(1L)
                .title("Assigned Yesterday")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.ASSIGNED)
                .assignedTechnicianId(technicianId)
                .createdBy("test@fsm.com")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId))
                .thenReturn(Collections.singletonList(assignedYesterday));
        when(assignmentRepository.findActiveAssignmentForTask(1L)).thenReturn(Optional.empty());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "all");
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        assertEquals("Assigned Yesterday", response.getTasks().get(0).getTitle());
    }
    
    @Test
    void testGetTechnicianTasksWithNullStatusFilter() {
        Long technicianId = 101L;
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId)).thenReturn(Collections.emptyList());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, null);
        
        assertNotNull(response);
        verify(taskRepository).findByTechnicianIdOrderedByPriority(technicianId);
        verify(taskRepository, never()).findByTechnicianIdAndStatusOrderedByPriority(any(), any());
    }
    
    @Test
    void testGetTechnicianTasksWithEmptyStatusFilter() {
        Long technicianId = 101L;
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId)).thenReturn(Collections.emptyList());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "");
        
        assertNotNull(response);
        verify(taskRepository).findByTechnicianIdOrderedByPriority(technicianId);
    }
    
    @Test
    void testGetTechnicianTasksWithInvalidStatusFilterReturnsAll() {
        Long technicianId = 101L;
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId)).thenReturn(Collections.emptyList());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "invalid_status");
        
        assertNotNull(response);
        verify(taskRepository).findByTechnicianIdOrderedByPriority(technicianId);
    }
    
    @Test
    void testGetTechnicianTasksReturnsAssignedAtFromAssignment() {
        Long technicianId = 101L;
        LocalDateTime assignedTime = LocalDateTime.now().minusHours(2);
        
        ServiceTask task = createTechnicianTask(1L, "Test Task", Priority.HIGH, TaskStatus.ASSIGNED, technicianId);
        
        Assignment assignment = Assignment.builder()
                .id(1L)
                .taskId(1L)
                .technicianId(technicianId)
                .assignedAt(assignedTime)
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId))
                .thenReturn(Collections.singletonList(task));
        when(assignmentRepository.findActiveAssignmentForTask(1L)).thenReturn(Optional.of(assignment));
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "all");
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        assertEquals(assignedTime, response.getTasks().get(0).getAssignedAt());
    }
    
    @Test
    void testGetTechnicianTasksReturnsTaskDetails() {
        Long technicianId = 101L;
        
        ServiceTask task = ServiceTask.builder()
                .id(1L)
                .title("HVAC Repair")
                .description("Fix heating unit")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.ASSIGNED)
                .assignedTechnicianId(technicianId)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId))
                .thenReturn(Collections.singletonList(task));
        when(assignmentRepository.findActiveAssignmentForTask(1L)).thenReturn(Optional.empty());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "all");
        
        assertNotNull(response);
        assertEquals(1, response.getTasks().size());
        
        TechnicianTaskResponse taskResponse = response.getTasks().get(0);
        assertEquals(1L, taskResponse.getId());
        assertEquals("HVAC Repair", taskResponse.getTitle());
        assertEquals("Fix heating unit", taskResponse.getDescription());
        assertEquals("123 Main St, Springfield", taskResponse.getClientAddress());
        assertEquals(Priority.HIGH, taskResponse.getPriority());
        assertEquals(120, taskResponse.getEstimatedDuration());
        assertEquals(TaskStatus.ASSIGNED, taskResponse.getStatus());
    }
    
    @Test
    void testGetTechnicianTasksOrdersByPriority() {
        Long technicianId = 101L;
        
        // Tasks are returned in priority order by the repository query
        ServiceTask highPriority = createTechnicianTask(1L, "High Priority", Priority.HIGH, TaskStatus.ASSIGNED, technicianId);
        ServiceTask mediumPriority = createTechnicianTask(2L, "Medium Priority", Priority.MEDIUM, TaskStatus.ASSIGNED, technicianId);
        ServiceTask lowPriority = createTechnicianTask(3L, "Low Priority", Priority.LOW, TaskStatus.ASSIGNED, technicianId);
        
        when(taskRepository.findByTechnicianIdOrderedByPriority(technicianId))
                .thenReturn(Arrays.asList(highPriority, mediumPriority, lowPriority));
        when(assignmentRepository.findActiveAssignmentForTask(any())).thenReturn(Optional.empty());
        
        TechnicianTaskListResponse response = taskService.getTechnicianTasks(technicianId, "all");
        
        assertNotNull(response);
        assertEquals(3, response.getTasks().size());
        assertEquals(Priority.HIGH, response.getTasks().get(0).getPriority());
        assertEquals(Priority.MEDIUM, response.getTasks().get(1).getPriority());
        assertEquals(Priority.LOW, response.getTasks().get(2).getPriority());
    }
    
    // Helper method to create technician tasks
    private ServiceTask createTechnicianTask(Long id, String title, Priority priority, TaskStatus status, Long technicianId) {
        return ServiceTask.builder()
                .id(id)
                .title(title)
                .description("Test Description")
                .clientAddress("123 Test St")
                .priority(priority)
                .status(status)
                .assignedTechnicianId(technicianId)
                .createdBy("test@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
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
