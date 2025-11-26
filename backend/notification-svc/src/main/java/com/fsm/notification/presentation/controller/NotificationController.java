package com.fsm.notification.presentation.controller;

import com.fsm.notification.application.dto.NotificationResponse;
import com.fsm.notification.application.dto.SendNotificationRequest;
import com.fsm.notification.application.service.NotificationService;
import com.fsm.notification.domain.model.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification operations.
 * Provides endpoints for sending notifications to users.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification API endpoints")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * Send a push notification to a user.
     * Creates a notification record in the database and attempts to deliver it via FCM.
     * 
     * Domain Invariants:
     * - User ID must be valid
     * - Device token must be associated with the user
     * - Failed deliveries are logged but return success with sent=false
     * 
     * @param request the notification request with user details and message
     * @return ResponseEntity with notification details and delivery status
     */
    @PostMapping("/send")
    @Operation(
            summary = "Send a push notification",
            description = "Sends a push notification to a user's device. " +
                    "Creates a notification record and attempts delivery via FCM. " +
                    "Failed deliveries are logged but do not cause the endpoint to fail."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Notification created and sent",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - validation failed",
                    content = @Content
            )
    })
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        log.info("Received request to send notification to user {}: {}", request.getUserId(), request.getTitle());
        
        try {
            Notification notification = notificationService.sendPushNotification(
                    request.getUserId(),
                    request.getDeviceToken(),
                    request.getTitle(),
                    request.getMessage(),
                    request.getData()
            );
            
            NotificationResponse response = NotificationResponse.fromEntity(notification);
            log.info("Notification sent successfully. ID: {}, Delivered: {}", 
                    notification.getId(), notification.isDelivered());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", request.getUserId(), e.getMessage(), e);
            // Return error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
