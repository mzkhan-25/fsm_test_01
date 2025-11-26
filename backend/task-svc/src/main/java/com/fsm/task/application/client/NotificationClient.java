package com.fsm.task.application.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for calling notification-svc to send notifications.
 * 
 * Domain Invariants:
 * - Failed notifications are logged but don't block the calling operation
 * - Service communication errors are handled gracefully
 */
@Component
@Slf4j
public class NotificationClient {
    
    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;
    private final boolean notificationEnabled;
    private final ObjectMapper objectMapper;
    
    /**
     * Creates a NotificationClient with configurable behavior.
     * 
     * @param restTemplate the RestTemplate for HTTP calls
     * @param notificationServiceUrl the URL of the notification-svc
     * @param notificationEnabled whether to enable notification sending
     * @param objectMapper Jackson ObjectMapper for JSON serialization
     */
    public NotificationClient(
            RestTemplate restTemplate,
            @Value("${notification.service.url:http://localhost:8083}") String notificationServiceUrl,
            @Value("${notification.service.enabled:true}") boolean notificationEnabled,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
        this.notificationEnabled = notificationEnabled;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Sends a task assignment notification to a technician.
     * 
     * @param technicianId the technician's user ID
     * @param deviceToken the technician's device token for push notifications
     * @param taskId the assigned task ID
     * @param taskTitle the task title
     * @param priority the task priority
     * @param clientAddress the task location
     * @return true if notification was sent successfully, false otherwise
     */
    public boolean sendTaskAssignmentNotification(
            Long technicianId,
            String deviceToken,
            Long taskId,
            String taskTitle,
            String priority,
            String clientAddress) {
        
        if (!notificationEnabled) {
            log.debug("Notification service is disabled, skipping notification for technician {}", technicianId);
            return false;
        }
        
        log.info("Sending task assignment notification to technician {} for task {}", technicianId, taskId);
        
        try {
            String url = notificationServiceUrl + "/api/notifications/send";
            
            // Build notification message
            String title = "New Task Assigned";
            String message = String.format("Task: %s\nPriority: %s\nLocation: %s", 
                    taskTitle, priority, clientAddress);
            
            // Build data payload for deep linking using ObjectMapper to prevent JSON injection
            Map<String, Object> dataPayload = new HashMap<>();
            dataPayload.put("taskId", taskId);
            dataPayload.put("taskTitle", taskTitle);
            dataPayload.put("priority", priority);
            dataPayload.put("clientAddress", clientAddress);
            String data = objectMapper.writeValueAsString(dataPayload);
            
            // Build request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", technicianId);
            requestBody.put("deviceToken", deviceToken);
            requestBody.put("title", title);
            requestBody.put("message", message);
            requestBody.put("data", data);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Task assignment notification sent successfully to technician {}", technicianId);
                return true;
            } else {
                log.warn("Failed to send notification to technician {}. Status: {}", 
                        technicianId, response.getStatusCode());
                return false;
            }
            
        } catch (RestClientException e) {
            // Log error but don't block the assignment operation
            log.error("Error sending notification to technician {}: {}. Assignment will proceed.", 
                    technicianId, e.getMessage());
            return false;
        } catch (Exception e) {
            // Catch any other unexpected errors
            log.error("Unexpected error sending notification to technician {}: {}. Assignment will proceed.", 
                    technicianId, e.getMessage(), e);
            return false;
        }
    }
}
