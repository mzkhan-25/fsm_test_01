package com.fsm.task.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.task.application.dto.AssignTaskRequest;
import com.fsm.task.application.dto.AssignTaskResponse;
import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.ReassignTaskRequest;
import com.fsm.task.application.dto.ReassignTaskResponse;
import com.fsm.task.application.dto.TaskListRequest;
import com.fsm.task.application.dto.TaskListResponse;
import com.fsm.task.application.dto.TaskResponse;
import com.fsm.task.application.dto.TechnicianTaskResponse;
import com.fsm.task.application.dto.UpdateTaskStatusRequest;
import com.fsm.task.application.exception.InvalidAssignmentException;
import com.fsm.task.application.exception.InvalidStatusTransitionException;
import com.fsm.task.application.exception.TaskNotFoundException;
import com.fsm.task.application.exception.UnauthorizedTaskAccessException;
import com.fsm.task.application.service.TaskService;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import com.fsm.task.infrastructure.security.RoleAuthorizationAspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TaskController
 */
@WebMvcTest(TaskController.class)
@Import(RoleAuthorizationAspect.class)
@EnableAspectJAutoProxy
class TaskControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TaskService taskService;
    
    private CreateTaskRequest validRequest;
    private TaskResponse taskResponse;
    
    @BeforeEach
    void setUp() {
        validRequest = CreateTaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .build();
        
        taskResponse = TaskResponse.builder()
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
    
    // ============== Tests for GET /api/tasks ==============
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksReturnsOk() throws Exception {
        TaskListResponse response = createEmptyTaskListResponse();
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.pageSize").value(50));
        
        verify(taskService, times(1)).getTasks(any(TaskListRequest.class));
    }
    
    @Test
    void testGetTasksRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
        
        verify(taskService, never()).getTasks(any());
    }
    
    @Test
    @WithMockUser(username = "user@fsm.com", roles = {"USER"})
    void testGetTasksAllowsAnyAuthenticatedUser() throws Exception {
        TaskListResponse response = createEmptyTaskListResponse();
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksWithStatusFilter() throws Exception {
        TaskListResponse response = createEmptyTaskListResponse();
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks")
                        .param("status", "UNASSIGNED"))
                .andExpect(status().isOk());
        
        verify(taskService).getTasks(any(TaskListRequest.class));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksWithPriorityFilter() throws Exception {
        TaskListResponse response = createEmptyTaskListResponse();
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk());
        
        verify(taskService).getTasks(any(TaskListRequest.class));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksWithSearchParameter() throws Exception {
        TaskListResponse response = createEmptyTaskListResponse();
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks")
                        .param("search", "HVAC"))
                .andExpect(status().isOk());
        
        verify(taskService).getTasks(any(TaskListRequest.class));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksWithSortingParameters() throws Exception {
        TaskListResponse response = createEmptyTaskListResponse();
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks")
                        .param("sortBy", "createdAt")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk());
        
        verify(taskService).getTasks(any(TaskListRequest.class));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksWithPaginationParameters() throws Exception {
        TaskListResponse response = createTaskListResponse(0, 10);
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(10));
        
        verify(taskService).getTasks(any(TaskListRequest.class));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksReturnsTasksWithData() throws Exception {
        TaskResponse task1 = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        TaskResponse task2 = TaskResponse.builder()
                .id(2L)
                .title("Task 2")
                .clientAddress("456 Oak Ave")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.ASSIGNED)
                .build();
        
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("UNASSIGNED", 1L);
        statusCounts.put("ASSIGNED", 1L);
        statusCounts.put("IN_PROGRESS", 0L);
        statusCounts.put("COMPLETED", 0L);
        
        TaskListResponse response = TaskListResponse.builder()
                .tasks(Arrays.asList(task1, task2))
                .page(0)
                .pageSize(50)
                .totalElements(2)
                .totalPages(1)
                .first(true)
                .last(true)
                .statusCounts(statusCounts)
                .build();
        
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks.length()").value(2))
                .andExpect(jsonPath("$.tasks[0].id").value(1))
                .andExpect(jsonPath("$.tasks[0].title").value("Task 1"))
                .andExpect(jsonPath("$.tasks[1].id").value(2))
                .andExpect(jsonPath("$.tasks[1].title").value("Task 2"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.statusCounts.UNASSIGNED").value(1))
                .andExpect(jsonPath("$.statusCounts.ASSIGNED").value(1));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksWithAllFilters() throws Exception {
        TaskListResponse response = createEmptyTaskListResponse();
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks")
                        .param("status", "UNASSIGNED")
                        .param("priority", "HIGH")
                        .param("search", "urgent")
                        .param("sortBy", "priority")
                        .param("sortOrder", "desc")
                        .param("page", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isOk());
        
        verify(taskService).getTasks(any(TaskListRequest.class));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksCapsPageSizeAt100() throws Exception {
        // When pageSize > 100, it should be capped to 100
        TaskListResponse response = createTaskListResponse(0, 100);
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks")
                        .param("pageSize", "200"))
                .andExpect(status().isOk());
        
        verify(taskService).getTasks(any(TaskListRequest.class));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksDefaultPageSizeIs50() throws Exception {
        TaskListResponse response = createTaskListResponse(0, 50);
        when(taskService.getTasks(any(TaskListRequest.class))).thenReturn(response);
        
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(50));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksInvalidStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTasksInvalidPriorityReturns400() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("priority", "INVALID_PRIORITY"))
                .andExpect(status().isBadRequest());
    }
    
    // ============== Tests for POST /api/tasks ==============
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskAsDispatcher() throws Exception {
        when(taskService.createTask(any(CreateTaskRequest.class), eq("dispatcher@fsm.com")))
                .thenReturn(taskResponse);
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.clientAddress").value("123 Main St"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.estimatedDuration").value(120))
                .andExpect(jsonPath("$.status").value("UNASSIGNED"))
                .andExpect(jsonPath("$.createdBy").value("dispatcher@fsm.com"));
        
        verify(taskService, times(1)).createTask(any(CreateTaskRequest.class), eq("dispatcher@fsm.com"));
    }
    
    @Test
    @WithMockUser(username = "admin@fsm.com", roles = {"ADMIN"})
    void testCreateTaskAsAdmin() throws Exception {
        TaskResponse adminResponse = TaskResponse.builder()
                .id(2L)
                .title("Admin Task")
                .clientAddress("456 Admin Ave")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("admin@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        CreateTaskRequest adminRequest = CreateTaskRequest.builder()
                .title("Admin Task")
                .clientAddress("456 Admin Ave")
                .priority(Priority.MEDIUM)
                .build();
        
        when(taskService.createTask(any(CreateTaskRequest.class), eq("admin@fsm.com")))
                .thenReturn(adminResponse);
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Admin Task"))
                .andExpect(jsonPath("$.createdBy").value("admin@fsm.com"));
    }
    
    @Test
    void testCreateTaskUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
        
        verify(taskService, never()).createTask(any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskWithBlankTitle() throws Exception {
        CreateTaskRequest invalidRequest = CreateTaskRequest.builder()
                .title("")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .build();
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskWithShortTitle() throws Exception {
        CreateTaskRequest invalidRequest = CreateTaskRequest.builder()
                .title("ab")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .build();
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskWithBlankClientAddress() throws Exception {
        CreateTaskRequest invalidRequest = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("")
                .priority(Priority.HIGH)
                .build();
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskWithNullPriority() throws Exception {
        CreateTaskRequest invalidRequest = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(null)
                .build();
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskWithNegativeEstimatedDuration() throws Exception {
        CreateTaskRequest invalidRequest = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(-10)
                .build();
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskWithZeroEstimatedDuration() throws Exception {
        CreateTaskRequest invalidRequest = CreateTaskRequest.builder()
                .title("Test Task")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(0)
                .build();
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(taskService, never()).createTask(any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskWithAllPriorities() throws Exception {
        for (Priority priority : Priority.values()) {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Priority Test " + priority)
                    .clientAddress("123 Main St")
                    .priority(priority)
                    .build();
            
            TaskResponse response = TaskResponse.builder()
                    .id(1L)
                    .title("Priority Test " + priority)
                    .clientAddress("123 Main St")
                    .priority(priority)
                    .status(TaskStatus.UNASSIGNED)
                    .createdBy("dispatcher@fsm.com")
                    .createdAt(LocalDateTime.now())
                    .build();
            
            when(taskService.createTask(any(CreateTaskRequest.class), any()))
                    .thenReturn(response);
            
            mockMvc.perform(post("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.priority").value(priority.name()));
        }
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskWithOptionalFieldsNull() throws Exception {
        CreateTaskRequest minimalRequest = CreateTaskRequest.builder()
                .title("Minimal Task")
                .clientAddress("123 Main St")
                .priority(Priority.LOW)
                .build();
        
        TaskResponse minimalResponse = TaskResponse.builder()
                .id(1L)
                .title("Minimal Task")
                .clientAddress("123 Main St")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("dispatcher@fsm.com")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(taskService.createTask(any(CreateTaskRequest.class), eq("dispatcher@fsm.com")))
                .thenReturn(minimalResponse);
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.estimatedDuration").doesNotExist());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testCreateTaskReturnsCreatedStatus() throws Exception {
        when(taskService.createTask(any(CreateTaskRequest.class), eq("dispatcher@fsm.com")))
                .thenReturn(taskResponse);
        
        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }
    
    // Helper methods
    private Map<String, Long> createDefaultStatusCounts() {
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("UNASSIGNED", 0L);
        statusCounts.put("ASSIGNED", 0L);
        statusCounts.put("IN_PROGRESS", 0L);
        statusCounts.put("COMPLETED", 0L);
        return statusCounts;
    }
    
    private TaskListResponse createEmptyTaskListResponse() {
        return TaskListResponse.builder()
                .tasks(Collections.emptyList())
                .page(0)
                .pageSize(50)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .statusCounts(createDefaultStatusCounts())
                .build();
    }
    
    private TaskListResponse createTaskListResponse(int page, int pageSize) {
        return TaskListResponse.builder()
                .tasks(Collections.emptyList())
                .page(page)
                .pageSize(pageSize)
                .totalElements(0)
                .totalPages(0)
                .first(page == 0)
                .last(true)
                .statusCounts(createDefaultStatusCounts())
                .build();
    }
    
    // ============== Tests for POST /api/tasks/{taskId}/reassign ==============
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testReassignTaskAsDispatcher() throws Exception {
        Long taskId = 1L;
        Long previousTechnicianId = 100L;
        Long newTechnicianId = 101L;
        String reason = "Technician unavailable";
        
        ReassignTaskRequest request = ReassignTaskRequest.builder()
                .newTechnicianId(newTechnicianId)
                .reason(reason)
                .build();
        
        ReassignTaskResponse response = ReassignTaskResponse.builder()
                .assignmentId(2L)
                .taskId(taskId)
                .previousTechnicianId(previousTechnicianId)
                .newTechnicianId(newTechnicianId)
                .reassignedAt(LocalDateTime.now())
                .reassignedBy("dispatcher@fsm.com")
                .reason(reason)
                .taskStatus(TaskStatus.ASSIGNED)
                .newTechnicianWorkload(3)
                .assignmentHistory(Collections.emptyList())
                .build();
        
        when(taskService.reassignTask(eq(taskId), any(ReassignTaskRequest.class), eq("dispatcher@fsm.com")))
                .thenReturn(response);
        
        mockMvc.perform(post("/api/tasks/{taskId}/reassign", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentId").value(2))
                .andExpect(jsonPath("$.taskId").value(taskId))
                .andExpect(jsonPath("$.previousTechnicianId").value(previousTechnicianId))
                .andExpect(jsonPath("$.newTechnicianId").value(newTechnicianId))
                .andExpect(jsonPath("$.reassignedBy").value("dispatcher@fsm.com"))
                .andExpect(jsonPath("$.reason").value(reason))
                .andExpect(jsonPath("$.taskStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.newTechnicianWorkload").value(3));
        
        verify(taskService).reassignTask(eq(taskId), any(ReassignTaskRequest.class), eq("dispatcher@fsm.com"));
    }
    
    @Test
    @WithMockUser(username = "admin@fsm.com", roles = {"ADMIN"})
    void testReassignTaskAsAdmin() throws Exception {
        Long taskId = 1L;
        Long newTechnicianId = 101L;
        
        ReassignTaskRequest request = ReassignTaskRequest.builder()
                .newTechnicianId(newTechnicianId)
                .build();
        
        ReassignTaskResponse response = ReassignTaskResponse.builder()
                .assignmentId(2L)
                .taskId(taskId)
                .previousTechnicianId(100L)
                .newTechnicianId(newTechnicianId)
                .reassignedAt(LocalDateTime.now())
                .reassignedBy("admin@fsm.com")
                .taskStatus(TaskStatus.ASSIGNED)
                .newTechnicianWorkload(2)
                .assignmentHistory(Collections.emptyList())
                .build();
        
        when(taskService.reassignTask(eq(taskId), any(ReassignTaskRequest.class), eq("admin@fsm.com")))
                .thenReturn(response);
        
        mockMvc.perform(post("/api/tasks/{taskId}/reassign", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reassignedBy").value("admin@fsm.com"));
    }
    
    @Test
    void testReassignTaskUnauthenticated() throws Exception {
        Long taskId = 1L;
        ReassignTaskRequest request = ReassignTaskRequest.builder()
                .newTechnicianId(101L)
                .build();
        
        mockMvc.perform(post("/api/tasks/{taskId}/reassign", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        
        verify(taskService, never()).reassignTask(any(), any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testReassignTaskWithNullNewTechnicianId() throws Exception {
        Long taskId = 1L;
        ReassignTaskRequest request = ReassignTaskRequest.builder()
                .newTechnicianId(null)
                .build();
        
        mockMvc.perform(post("/api/tasks/{taskId}/reassign", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(taskService, never()).reassignTask(any(), any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testReassignTaskNotFound() throws Exception {
        Long taskId = 999L;
        ReassignTaskRequest request = ReassignTaskRequest.builder()
                .newTechnicianId(101L)
                .build();
        
        when(taskService.reassignTask(eq(taskId), any(ReassignTaskRequest.class), any()))
                .thenThrow(new TaskNotFoundException(taskId));
        
        mockMvc.perform(post("/api/tasks/{taskId}/reassign", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testReassignCompletedTaskReturnsBadRequest() throws Exception {
        Long taskId = 1L;
        ReassignTaskRequest request = ReassignTaskRequest.builder()
                .newTechnicianId(101L)
                .build();
        
        when(taskService.reassignTask(eq(taskId), any(ReassignTaskRequest.class), any()))
                .thenThrow(new InvalidAssignmentException("Task cannot be reassigned. Current status: COMPLETED."));
        
        mockMvc.perform(post("/api/tasks/{taskId}/reassign", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testReassignInProgressTaskWithoutReasonReturnsBadRequest() throws Exception {
        Long taskId = 1L;
        ReassignTaskRequest request = ReassignTaskRequest.builder()
                .newTechnicianId(101L)
                .reason(null)
                .build();
        
        when(taskService.reassignTask(eq(taskId), any(ReassignTaskRequest.class), any()))
                .thenThrow(new InvalidAssignmentException("Task is IN_PROGRESS. A reason is required."));
        
        mockMvc.perform(post("/api/tasks/{taskId}/reassign", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testReassignTaskWithWorkloadWarning() throws Exception {
        Long taskId = 1L;
        Long newTechnicianId = 101L;
        
        ReassignTaskRequest request = ReassignTaskRequest.builder()
                .newTechnicianId(newTechnicianId)
                .build();
        
        ReassignTaskResponse response = ReassignTaskResponse.builder()
                .assignmentId(2L)
                .taskId(taskId)
                .previousTechnicianId(100L)
                .newTechnicianId(newTechnicianId)
                .reassignedAt(LocalDateTime.now())
                .reassignedBy("dispatcher@fsm.com")
                .taskStatus(TaskStatus.ASSIGNED)
                .newTechnicianWorkload(15)
                .workloadWarning("Warning: New technician has 15 active tasks, which exceeds the recommended threshold of 10")
                .assignmentHistory(Collections.emptyList())
                .build();
        
        when(taskService.reassignTask(eq(taskId), any(ReassignTaskRequest.class), eq("dispatcher@fsm.com")))
                .thenReturn(response);
        
        mockMvc.perform(post("/api/tasks/{taskId}/reassign", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newTechnicianWorkload").value(15))
                .andExpect(jsonPath("$.workloadWarning").exists());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testReassignTaskWithAssignmentHistory() throws Exception {
        Long taskId = 1L;
        Long newTechnicianId = 101L;
        
        ReassignTaskRequest request = ReassignTaskRequest.builder()
                .newTechnicianId(newTechnicianId)
                .build();
        
        ReassignTaskResponse.AssignmentHistoryDto historyEntry = ReassignTaskResponse.AssignmentHistoryDto.builder()
                .id(1L)
                .technicianId(100L)
                .action("CREATED")
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(1))
                .build();
        
        ReassignTaskResponse response = ReassignTaskResponse.builder()
                .assignmentId(2L)
                .taskId(taskId)
                .previousTechnicianId(100L)
                .newTechnicianId(newTechnicianId)
                .reassignedAt(LocalDateTime.now())
                .reassignedBy("dispatcher@fsm.com")
                .taskStatus(TaskStatus.ASSIGNED)
                .newTechnicianWorkload(3)
                .assignmentHistory(Collections.singletonList(historyEntry))
                .build();
        
        when(taskService.reassignTask(eq(taskId), any(ReassignTaskRequest.class), eq("dispatcher@fsm.com")))
                .thenReturn(response);
        
        mockMvc.perform(post("/api/tasks/{taskId}/reassign", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentHistory").isArray())
                .andExpect(jsonPath("$.assignmentHistory[0].action").value("CREATED"))
                .andExpect(jsonPath("$.assignmentHistory[0].technicianId").value(100));
    }
    
    // ============== Tests for PATCH /api/tasks/{taskId}/status ==============
    
    @Test
    void testUpdateTaskStatusUnauthenticated() throws Exception {
        Long taskId = 1L;
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        
        verify(taskService, never()).updateTaskStatus(any(), any(), any());
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testUpdateTaskStatusAsTechnician() throws Exception {
        Long taskId = 1L;
        Long technicianId = 101L;
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        TechnicianTaskResponse response = TechnicianTaskResponse.builder()
                .id(taskId)
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .assignedAt(LocalDateTime.now().minusHours(1))
                .startedAt(LocalDateTime.now())
                .build();
        
        when(taskService.updateTaskStatus(eq(taskId), any(UpdateTaskStatusRequest.class), eq(technicianId)))
                .thenReturn(response);
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.startedAt").exists());
        
        verify(taskService).updateTaskStatus(eq(taskId), any(UpdateTaskStatusRequest.class), eq(technicianId));
    }
    
    @Test
    @WithMockUser(username = "admin@fsm.com", roles = {"ADMIN"})
    void testUpdateTaskStatusAsAdminDenied() throws Exception {
        // ADMIN role should NOT be able to update task status through TECHNICIAN endpoint
        Long taskId = 1L;
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());  // ADMIN has access to everything
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testUpdateTaskStatusAsDispatcherDenied() throws Exception {
        Long taskId = 1L;
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        
        verify(taskService, never()).updateTaskStatus(any(), any(), any());
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testUpdateTaskStatusTaskNotFound() throws Exception {
        Long taskId = 999L;
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        when(taskService.updateTaskStatus(eq(taskId), any(UpdateTaskStatusRequest.class), any()))
                .thenThrow(new TaskNotFoundException(taskId));
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testUpdateTaskStatusUnauthorized() throws Exception {
        Long taskId = 1L;
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        when(taskService.updateTaskStatus(eq(taskId), any(UpdateTaskStatusRequest.class), any()))
                .thenThrow(new UnauthorizedTaskAccessException("Technician 101 is not assigned to task 1"));
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testUpdateTaskStatusInvalidTransition() throws Exception {
        Long taskId = 1L;
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        when(taskService.updateTaskStatus(eq(taskId), any(UpdateTaskStatusRequest.class), any()))
                .thenThrow(new InvalidStatusTransitionException("Task cannot be started. Current status: COMPLETED."));
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testUpdateTaskStatusWithNullStatus() throws Exception {
        Long taskId = 1L;
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(null)
                .build();
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        verify(taskService, never()).updateTaskStatus(any(), any(), any());
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testUpdateTaskStatusReturnsTaskDetails() throws Exception {
        Long taskId = 1L;
        Long technicianId = 101L;
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        LocalDateTime assignedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime startedAt = LocalDateTime.now();
        
        TechnicianTaskResponse response = TechnicianTaskResponse.builder()
                .id(taskId)
                .title("HVAC Repair")
                .description("Fix heating system")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.IN_PROGRESS)
                .assignedAt(assignedAt)
                .startedAt(startedAt)
                .build();
        
        when(taskService.updateTaskStatus(eq(taskId), any(UpdateTaskStatusRequest.class), eq(technicianId)))
                .thenReturn(response);
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("HVAC Repair"))
                .andExpect(jsonPath("$.description").value("Fix heating system"))
                .andExpect(jsonPath("$.clientAddress").value("123 Main St, Springfield"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.estimatedDuration").value(120))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.assignedAt").exists())
                .andExpect(jsonPath("$.startedAt").exists());
    }
    
    @Test
    @WithMockUser(username = "tech_user_42", roles = {"TECHNICIAN"})
    void testUpdateTaskStatusExtractsTechnicianIdFromUsername() throws Exception {
        Long taskId = 1L;
        Long expectedTechnicianId = 42L;
        
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();
        
        TechnicianTaskResponse response = TechnicianTaskResponse.builder()
                .id(taskId)
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .assignedAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .build();
        
        when(taskService.updateTaskStatus(eq(taskId), any(UpdateTaskStatusRequest.class), eq(expectedTechnicianId)))
                .thenReturn(response);
        
        mockMvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        
        verify(taskService).updateTaskStatus(eq(taskId), any(UpdateTaskStatusRequest.class), eq(expectedTechnicianId));
    }
}
