package com.fsm.notification.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsm.notification.application.dto.SendNotificationRequest;
import com.fsm.notification.application.service.NotificationService;
import com.fsm.notification.domain.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for NotificationController.
 */
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private NotificationService notificationService;
    
    private SendNotificationRequest validRequest;
    private Notification mockNotification;
    
    @BeforeEach
    void setUp() {
        validRequest = SendNotificationRequest.builder()
                .userId(101L)
                .deviceToken("device_token_123")
                .title("New Task Assigned")
                .message("Task: Fix HVAC\nPriority: HIGH\nLocation: 123 Main St")
                .data("{\"taskId\":1,\"taskTitle\":\"Fix HVAC\",\"priority\":\"HIGH\",\"clientAddress\":\"123 Main St\"}")
                .build();
        
        mockNotification = Notification.builder()
                .id(1L)
                .userId(101L)
                .type(Notification.NotificationType.PUSH)
                .title("New Task Assigned")
                .message("Task: Fix HVAC\nPriority: HIGH\nLocation: 123 Main St")
                .data("{\"taskId\":1,\"taskTitle\":\"Fix HVAC\",\"priority\":\"HIGH\",\"clientAddress\":\"123 Main St\"}")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        mockNotification.markAsSent();
        mockNotification.markAsDelivered();
    }
    
    @Test
    void testSendNotification_Success() throws Exception {
        when(notificationService.sendPushNotification(
                eq(101L),
                eq("device_token_123"),
                eq("New Task Assigned"),
                anyString(),
                anyString()
        )).thenReturn(mockNotification);
        
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(101)))
                .andExpect(jsonPath("$.type", is("PUSH")))
                .andExpect(jsonPath("$.title", is("New Task Assigned")))
                .andExpect(jsonPath("$.sent", is(true)))
                .andExpect(jsonPath("$.delivered", is(true)));
    }
    
    @Test
    void testSendNotification_MissingUserId() throws Exception {
        validRequest.setUserId(null);
        
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSendNotification_MissingDeviceToken() throws Exception {
        validRequest.setDeviceToken("");
        
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSendNotification_MissingTitle() throws Exception {
        validRequest.setTitle("");
        
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSendNotification_MissingMessage() throws Exception {
        validRequest.setMessage("");
        
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSendNotification_ServiceException() throws Exception {
        when(notificationService.sendPushNotification(
                anyLong(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        )).thenThrow(new RuntimeException("FCM service unavailable"));
        
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    void testSendNotification_WithoutDataPayload() throws Exception {
        validRequest.setData(null);
        
        when(notificationService.sendPushNotification(
                eq(101L),
                eq("device_token_123"),
                eq("New Task Assigned"),
                anyString(),
                isNull()
        )).thenReturn(mockNotification);
        
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.delivered", is(true)));
    }
}
