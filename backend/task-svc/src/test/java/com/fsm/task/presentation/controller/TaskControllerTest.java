package com.fsm.task.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.task.application.dto.AssignTaskRequest;
import com.fsm.task.application.dto.AssignTaskResponse;
import com.fsm.task.application.dto.CreateTaskRequest;
import com.fsm.task.application.dto.TaskResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    
    // ==================== Task Assignment Endpoint Tests ====================
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testAssignTaskAsDispatcher() throws Exception {
        AssignTaskRequest assignRequest = AssignTaskRequest.builder()
                .technicianId(101L)
                .build();
        
        AssignTaskResponse assignResponse = AssignTaskResponse.builder()
                .assignmentId(1L)
                .taskId(1L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .taskStatus(TaskStatus.ASSIGNED)
                .technicianWorkload(5)
                .workloadWarning(null)
                .build();
        
        when(taskService.assignTask(eq(1L), any(AssignTaskRequest.class), eq("dispatcher@fsm.com")))
                .thenReturn(assignResponse);
        
        mockMvc.perform(post("/api/tasks/1/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentId").value(1))
                .andExpect(jsonPath("$.taskId").value(1))
                .andExpect(jsonPath("$.technicianId").value(101))
                .andExpect(jsonPath("$.taskStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$.technicianWorkload").value(5))
                .andExpect(jsonPath("$.workloadWarning").doesNotExist());
        
        verify(taskService, times(1)).assignTask(eq(1L), any(AssignTaskRequest.class), eq("dispatcher@fsm.com"));
    }
    
    @Test
    @WithMockUser(username = "admin@fsm.com", roles = {"ADMIN"})
    void testAssignTaskAsAdmin() throws Exception {
        AssignTaskRequest assignRequest = AssignTaskRequest.builder()
                .technicianId(102L)
                .build();
        
        AssignTaskResponse assignResponse = AssignTaskResponse.builder()
                .assignmentId(2L)
                .taskId(5L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("admin@fsm.com")
                .taskStatus(TaskStatus.ASSIGNED)
                .technicianWorkload(3)
                .build();
        
        when(taskService.assignTask(eq(5L), any(AssignTaskRequest.class), eq("admin@fsm.com")))
                .thenReturn(assignResponse);
        
        mockMvc.perform(post("/api/tasks/5/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentId").value(2))
                .andExpect(jsonPath("$.technicianId").value(102));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testAssignTaskWithWorkloadWarning() throws Exception {
        AssignTaskRequest assignRequest = AssignTaskRequest.builder()
                .technicianId(101L)
                .build();
        
        AssignTaskResponse assignResponse = AssignTaskResponse.builder()
                .assignmentId(1L)
                .taskId(1L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .taskStatus(TaskStatus.ASSIGNED)
                .technicianWorkload(12)
                .workloadWarning("Warning: Technician has 12 active tasks, which exceeds the recommended threshold of 10")
                .build();
        
        when(taskService.assignTask(eq(1L), any(AssignTaskRequest.class), eq("dispatcher@fsm.com")))
                .thenReturn(assignResponse);
        
        mockMvc.perform(post("/api/tasks/1/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianWorkload").value(12))
                .andExpect(jsonPath("$.workloadWarning").exists())
                .andExpect(jsonPath("$.workloadWarning").value("Warning: Technician has 12 active tasks, which exceeds the recommended threshold of 10"));
    }
    
    @Test
    void testAssignTaskUnauthenticated() throws Exception {
        AssignTaskRequest assignRequest = AssignTaskRequest.builder()
                .technicianId(101L)
                .build();
        
        mockMvc.perform(post("/api/tasks/1/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isUnauthorized());
        
        verify(taskService, never()).assignTask(any(), any(), any());
    }
    
    @Test
    @WithMockUser(username = "technician@fsm.com", roles = {"TECHNICIAN"})
    void testAssignTaskForbiddenForTechnician() throws Exception {
        AssignTaskRequest assignRequest = AssignTaskRequest.builder()
                .technicianId(101L)
                .build();
        
        mockMvc.perform(post("/api/tasks/1/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isForbidden());
        
        verify(taskService, never()).assignTask(any(), any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testAssignTaskWithNullTechnicianId() throws Exception {
        String invalidRequest = "{}";
        
        mockMvc.perform(post("/api/tasks/1/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
        
        verify(taskService, never()).assignTask(any(), any(), any());
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testAssignTaskNotFound() throws Exception {
        AssignTaskRequest assignRequest = AssignTaskRequest.builder()
                .technicianId(101L)
                .build();
        
        when(taskService.assignTask(eq(999L), any(AssignTaskRequest.class), eq("dispatcher@fsm.com")))
                .thenThrow(new IllegalArgumentException("Task not found with ID: 999"));
        
        mockMvc.perform(post("/api/tasks/999/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isBadRequest());
    }
}
