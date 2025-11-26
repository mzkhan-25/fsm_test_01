package com.fsm.task.application.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationClient.
 */
@ExtendWith(MockitoExtension.class)
class NotificationClientTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    private NotificationClient notificationClient;
    
    private ObjectMapper objectMapper;
    
    private static final String NOTIFICATION_SERVICE_URL = "http://localhost:8083";
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        notificationClient = new NotificationClient(restTemplate, NOTIFICATION_SERVICE_URL, true, objectMapper);
    }
    
    @Test
    void testSendTaskAssignmentNotification_Success() {
        // Given
        Long technicianId = 101L;
        String deviceToken = "device_token_123";
        Long taskId = 1L;
        String taskTitle = "Fix HVAC System";
        String priority = "HIGH";
        String clientAddress = "123 Main St";
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", 1);
        responseBody.put("sent", true);
        responseBody.put("delivered", true);
        
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);
        
        // When
        boolean result = notificationClient.sendTaskAssignmentNotification(
                technicianId, deviceToken, taskId, taskTitle, priority, clientAddress);
        
        // Then
        assertTrue(result);
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }
    
    @Test
    void testSendTaskAssignmentNotification_VerifyRequestPayload() {
        // Given
        Long technicianId = 101L;
        String deviceToken = "device_token_123";
        Long taskId = 1L;
        String taskTitle = "Fix HVAC System";
        String priority = "HIGH";
        String clientAddress = "123 Main St";
        
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);
        
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        
        // When
        notificationClient.sendTaskAssignmentNotification(
                technicianId, deviceToken, taskId, taskTitle, priority, clientAddress);
        
        // Then
        verify(restTemplate).postForEntity(urlCaptor.capture(), entityCaptor.capture(), eq(Map.class));
        
        String url = urlCaptor.getValue();
        assertEquals(NOTIFICATION_SERVICE_URL + "/api/notifications/send", url);
        
        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> requestBody = entity.getBody();
        
        assertNotNull(requestBody);
        assertEquals(technicianId, requestBody.get("userId"));
        assertEquals(deviceToken, requestBody.get("deviceToken"));
        assertEquals("New Task Assigned", requestBody.get("title"));
        assertTrue(requestBody.get("message").toString().contains(taskTitle));
        assertTrue(requestBody.get("message").toString().contains(priority));
        assertTrue(requestBody.get("message").toString().contains(clientAddress));
        assertNotNull(requestBody.get("data"));
    }
    
    @Test
    void testSendTaskAssignmentNotification_ServiceUnavailable() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));
        
        // When
        boolean result = notificationClient.sendTaskAssignmentNotification(
                101L, "device_token_123", 1L, "Fix HVAC", "HIGH", "123 Main St");
        
        // Then
        assertFalse(result);
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }
    
    @Test
    void testSendTaskAssignmentNotification_NonSuccessStatus() {
        // Given
        ResponseEntity<Map> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);
        
        // When
        boolean result = notificationClient.sendTaskAssignmentNotification(
                101L, "device_token_123", 1L, "Fix HVAC", "HIGH", "123 Main St");
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testSendTaskAssignmentNotification_UnexpectedException() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        
        // When
        boolean result = notificationClient.sendTaskAssignmentNotification(
                101L, "device_token_123", 1L, "Fix HVAC", "HIGH", "123 Main St");
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testSendTaskAssignmentNotification_DisabledNotifications() {
        // Given - Create client with notifications disabled
        NotificationClient disabledClient = new NotificationClient(restTemplate, NOTIFICATION_SERVICE_URL, false, objectMapper);
        
        // When
        boolean result = disabledClient.sendTaskAssignmentNotification(
                101L, "device_token_123", 1L, "Fix HVAC", "HIGH", "123 Main St");
        
        // Then
        assertFalse(result);
        verify(restTemplate, never()).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }
    
    @Test
    void testSendTaskAssignmentNotification_DataPayloadFormat() {
        // Given
        Long taskId = 42L;
        String taskTitle = "Emergency Repair";
        String priority = "HIGH";
        String clientAddress = "456 Oak Ave";
        
        Map<String, Object> responseBody = new HashMap<>();
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.CREATED);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);
        
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        
        // When
        notificationClient.sendTaskAssignmentNotification(
                101L, "device_token_123", taskId, taskTitle, priority, clientAddress);
        
        // Then
        verify(restTemplate).postForEntity(anyString(), entityCaptor.capture(), eq(Map.class));
        
        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        Map<String, Object> requestBody = entity.getBody();
        String data = (String) requestBody.get("data");
        
        assertNotNull(data);
        assertTrue(data.contains("\"taskId\":" + taskId));
        assertTrue(data.contains("\"taskTitle\":\"" + taskTitle + "\""));
        assertTrue(data.contains("\"priority\":\"" + priority + "\""));
        assertTrue(data.contains("\"clientAddress\":\"" + clientAddress + "\""));
    }
}
