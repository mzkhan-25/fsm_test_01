package com.fsm.notification.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notification entity representing an alert delivery in the FSM system.
 * Domain Invariants:
 * - Notification must have a recipient user (userId)
 * - Notification type determines delivery channel (push, email, SMS)
 * - Delivery status tracked for reliability
 * - Read status tracks if notification has been viewed by user
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Message is required")
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Lob
    @Column(name = "data", columnDefinition = "TEXT")
    private String data;
    
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * NotificationType enum representing delivery channels
     */
    public enum NotificationType {
        PUSH,
        EMAIL,
        SMS
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (read == null) {
            read = false;
        }
    }
    
    /**
     * Marks the notification as read
     */
    public void markAsRead() {
        this.read = true;
    }
    
    /**
     * Marks the notification as sent with current timestamp
     */
    public void markAsSent() {
        this.sentAt = LocalDateTime.now();
    }
    
    /**
     * Marks the notification as delivered with current timestamp
     */
    public void markAsDelivered() {
        this.deliveredAt = LocalDateTime.now();
    }
    
    /**
     * Checks if notification has been sent
     * @return true if notification has been sent
     */
    public boolean isSent() {
        return sentAt != null;
    }
    
    /**
     * Checks if notification has been delivered
     * @return true if notification has been delivered
     */
    public boolean isDelivered() {
        return deliveredAt != null;
    }
    
    /**
     * Checks if notification has been read by user
     * @return true if notification has been read
     */
    public boolean isRead() {
        return read != null && read;
    }
}
