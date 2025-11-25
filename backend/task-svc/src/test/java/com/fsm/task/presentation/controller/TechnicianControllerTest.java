package com.fsm.task.presentation.controller;

import com.fsm.task.application.dto.TechnicianTaskListResponse;
import com.fsm.task.application.dto.TechnicianTaskResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TechnicianController
 */
@WebMvcTest(TechnicianController.class)
@Import(RoleAuthorizationAspect.class)
@EnableAspectJAutoProxy
class TechnicianControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TaskService taskService;
    
    private TechnicianTaskListResponse emptyResponse;
    private TechnicianTaskListResponse responseWithTasks;
    
    @BeforeEach
    void setUp() {
        emptyResponse = TechnicianTaskListResponse.builder()
                .tasks(Collections.emptyList())
                .totalTasks(0)
                .build();
        
        TechnicianTaskResponse task1 = TechnicianTaskResponse.builder()
                .id(1L)
                .title("High Priority Task")
                .description("Urgent repair needed")
                .clientAddress("123 Main St")
                .priority(Priority.HIGH)
                .estimatedDuration(120)
                .status(TaskStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();
        
        TechnicianTaskResponse task2 = TechnicianTaskResponse.builder()
                .id(2L)
                .title("Medium Priority Task")
                .description("Regular maintenance")
                .clientAddress("456 Oak Ave")
                .priority(Priority.MEDIUM)
                .estimatedDuration(60)
                .status(TaskStatus.IN_PROGRESS)
                .assignedAt(LocalDateTime.now().minusHours(1))
                .build();
        
        responseWithTasks = TechnicianTaskListResponse.builder()
                .tasks(Arrays.asList(task1, task2))
                .totalTasks(2)
                .build();
    }
    
    // ============== Authentication and Authorization Tests ==============
    
    @Test
    void testGetTechnicianTasksRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isUnauthorized());
        
        verify(taskService, never()).getTechnicianTasks(any(), any());
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testGetTechnicianTasksAsTechnician() throws Exception {
        when(taskService.getTechnicianTasks(any(), eq("all"))).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.totalTasks").value(0));
        
        verify(taskService).getTechnicianTasks(any(), eq("all"));
    }
    
    @Test
    @WithMockUser(username = "admin@fsm.com", roles = {"ADMIN"})
    void testGetTechnicianTasksAsAdmin() throws Exception {
        // ADMIN role should have access to all endpoints
        when(taskService.getTechnicianTasks(any(), eq("all"))).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isOk());
        
        verify(taskService).getTechnicianTasks(any(), eq("all"));
    }
    
    @Test
    @WithMockUser(username = "dispatcher@fsm.com", roles = {"DISPATCHER"})
    void testGetTechnicianTasksAsDispatcherDenied() throws Exception {
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isForbidden());
        
        verify(taskService, never()).getTechnicianTasks(any(), any());
    }
    
    @Test
    @WithMockUser(username = "supervisor@fsm.com", roles = {"SUPERVISOR"})
    void testGetTechnicianTasksAsSupervisorDenied() throws Exception {
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isForbidden());
        
        verify(taskService, never()).getTechnicianTasks(any(), any());
    }
    
    // ============== Status Filter Tests ==============
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testGetTechnicianTasksWithDefaultStatusFilter() throws Exception {
        when(taskService.getTechnicianTasks(any(), eq("all"))).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isOk());
        
        verify(taskService).getTechnicianTasks(any(), eq("all"));
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testGetTechnicianTasksWithAssignedStatusFilter() throws Exception {
        when(taskService.getTechnicianTasks(any(), eq("assigned"))).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks")
                        .param("status", "assigned"))
                .andExpect(status().isOk());
        
        verify(taskService).getTechnicianTasks(any(), eq("assigned"));
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testGetTechnicianTasksWithInProgressStatusFilter() throws Exception {
        when(taskService.getTechnicianTasks(any(), eq("in_progress"))).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks")
                        .param("status", "in_progress"))
                .andExpect(status().isOk());
        
        verify(taskService).getTechnicianTasks(any(), eq("in_progress"));
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testGetTechnicianTasksWithCompletedStatusFilter() throws Exception {
        when(taskService.getTechnicianTasks(any(), eq("completed"))).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks")
                        .param("status", "completed"))
                .andExpect(status().isOk());
        
        verify(taskService).getTechnicianTasks(any(), eq("completed"));
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testGetTechnicianTasksWithAllStatusFilter() throws Exception {
        when(taskService.getTechnicianTasks(any(), eq("all"))).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks")
                        .param("status", "all"))
                .andExpect(status().isOk());
        
        verify(taskService).getTechnicianTasks(any(), eq("all"));
    }
    
    // ============== Response Content Tests ==============
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testGetTechnicianTasksReturnsTasks() throws Exception {
        when(taskService.getTechnicianTasks(any(), any())).thenReturn(responseWithTasks);
        
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks.length()").value(2))
                .andExpect(jsonPath("$.totalTasks").value(2))
                .andExpect(jsonPath("$.tasks[0].id").value(1))
                .andExpect(jsonPath("$.tasks[0].title").value("High Priority Task"))
                .andExpect(jsonPath("$.tasks[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.tasks[0].status").value("ASSIGNED"))
                .andExpect(jsonPath("$.tasks[1].id").value(2))
                .andExpect(jsonPath("$.tasks[1].title").value("Medium Priority Task"))
                .andExpect(jsonPath("$.tasks[1].priority").value("MEDIUM"))
                .andExpect(jsonPath("$.tasks[1].status").value("IN_PROGRESS"));
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testGetTechnicianTasksReturnsEmptyListWhenNoTasks() throws Exception {
        when(taskService.getTechnicianTasks(any(), any())).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks.length()").value(0))
                .andExpect(jsonPath("$.totalTasks").value(0));
    }
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testGetTechnicianTasksReturnsTaskDetails() throws Exception {
        TechnicianTaskResponse taskWithDetails = TechnicianTaskResponse.builder()
                .id(1L)
                .title("HVAC Repair")
                .description("Fix heating system")
                .clientAddress("789 Pine St, Springfield, IL")
                .priority(Priority.HIGH)
                .estimatedDuration(180)
                .status(TaskStatus.ASSIGNED)
                .assignedAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .build();
        
        TechnicianTaskListResponse response = TechnicianTaskListResponse.builder()
                .tasks(Collections.singletonList(taskWithDetails))
                .totalTasks(1)
                .build();
        
        when(taskService.getTechnicianTasks(any(), any())).thenReturn(response);
        
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].id").value(1))
                .andExpect(jsonPath("$.tasks[0].title").value("HVAC Repair"))
                .andExpect(jsonPath("$.tasks[0].description").value("Fix heating system"))
                .andExpect(jsonPath("$.tasks[0].clientAddress").value("789 Pine St, Springfield, IL"))
                .andExpect(jsonPath("$.tasks[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.tasks[0].estimatedDuration").value(180))
                .andExpect(jsonPath("$.tasks[0].status").value("ASSIGNED"))
                .andExpect(jsonPath("$.tasks[0].assignedAt").exists());
    }
    
    // ============== Technician ID Extraction Tests ==============
    
    @Test
    @WithMockUser(username = "technician_101", roles = {"TECHNICIAN"})
    void testExtractsTechnicianIdFromUsername() throws Exception {
        when(taskService.getTechnicianTasks(eq(101L), any())).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isOk());
        
        verify(taskService).getTechnicianTasks(eq(101L), any());
    }
    
    @Test
    @WithMockUser(username = "tech@fsm.com", roles = {"TECHNICIAN"})
    void testExtractsTechnicianIdFromEmailHash() throws Exception {
        // When username is an email, should use hash as ID
        when(taskService.getTechnicianTasks(any(), any())).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isOk());
        
        // Just verify the service was called with some Long value
        verify(taskService).getTechnicianTasks(any(Long.class), any());
    }
    
    @Test
    @WithMockUser(username = "tech_user_42", roles = {"TECHNICIAN"})
    void testExtractsTechnicianIdFromLastUnderscorePart() throws Exception {
        when(taskService.getTechnicianTasks(eq(42L), any())).thenReturn(emptyResponse);
        
        mockMvc.perform(get("/api/technicians/me/tasks"))
                .andExpect(status().isOk());
        
        verify(taskService).getTechnicianTasks(eq(42L), any());
    }
}
