package com.fsm.notification.domain.repository;

import com.fsm.notification.domain.model.Notification;
import com.fsm.notification.domain.model.Notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Data JPA repository for Notification entity.
 * Provides database persistence operations for notifications.
 * Inherits CRUD operations from JpaRepository: create, findById, findAll, update, delete.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find notifications by user ID
     * @param userId The user's ID
     * @return List of notifications for the user
     */
    List<Notification> findByUserId(Long userId);
    
    /**
     * Find notifications by user ID and read status
     * @param userId The user's ID
     * @param read The read status
     * @return List of notifications matching the criteria
     */
    List<Notification> findByUserIdAndRead(Long userId, Boolean read);
    
    /**
     * Find unread notifications for a user ordered by creation date (most recent first)
     * @param userId The user's ID
     * @return List of unread notifications
     */
    List<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, Boolean read);
    
    /**
     * Find notifications by user ID and type
     * @param userId The user's ID
     * @param type The notification type
     * @return List of notifications matching the criteria
     */
    List<Notification> findByUserIdAndType(Long userId, NotificationType type);
    
    /**
     * Mark a notification as read by ID
     * @param notificationId The notification's ID
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId);
    
    /**
     * Mark all notifications for a user as read
     * @param userId The user's ID
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.read = false")
    void markAllAsReadForUser(@Param("userId") Long userId);
    
    /**
     * Count unread notifications for a user
     * @param userId The user's ID
     * @return Count of unread notifications
     */
    long countByUserIdAndRead(Long userId, Boolean read);
    
    /**
     * Find user's recent notifications from the last specified number of days, ordered by creation date descending.
     * Domain invariant: Notifications are retained for 30 days, then archived.
     * Uses createdAt to include all notifications including those not yet sent.
     * @param userId The user's ID
     * @param sinceDate The start date (typically 30 days ago)
     * @return List of notifications created after the specified date, ordered by creation date descending
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :sinceDate ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUserId(@Param("userId") Long userId, @Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Find user's recent notifications from the last 30 days, ordered by creation date descending.
     * Convenience method that uses the default 30-day retention period.
     * @param userId The user's ID
     * @return List of notifications from the last 30 days
     */
    default List<Notification> findRecentNotificationsForUser(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return findRecentByUserId(userId, thirtyDaysAgo);
    }
    
    /**
     * Count unread notifications for a user.
     * Convenience method for counting unread notifications.
     * @param userId The user's ID
     * @return Count of unread notifications
     */
    default long countUnreadForUser(Long userId) {
        return countByUserIdAndRead(userId, false);
    }
    
    /**
     * Returns hardcoded sample notifications for initial development
     * This method provides sample notifications with various types and statuses
     */
    default List<Notification> getHardcodedNotifications() {
        LocalDateTime now = LocalDateTime.now();
        
        return Arrays.asList(
            Notification.builder()
                .id(1L)
                .userId(101L)
                .type(NotificationType.PUSH)
                .title("Task Assignment")
                .message("You have been assigned to task #123: Repair HVAC System")
                .data("{\"taskId\": 123, \"priority\": \"HIGH\"}")
                .read(false)
                .sentAt(now.minusMinutes(30))
                .deliveredAt(now.minusMinutes(29))
                .createdAt(now.minusMinutes(30))
                .build(),
            
            Notification.builder()
                .id(2L)
                .userId(101L)
                .type(NotificationType.EMAIL)
                .title("Task Due Soon")
                .message("Your assigned task is due in 2 hours")
                .data("{\"taskId\": 123, \"dueTime\": \"" + now.plusHours(2) + "\"}")
                .read(true)
                .sentAt(now.minusHours(2))
                .deliveredAt(now.minusHours(2))
                .createdAt(now.minusHours(2))
                .build(),
            
            Notification.builder()
                .id(3L)
                .userId(102L)
                .type(NotificationType.PUSH)
                .title("New Task Available")
                .message("A new high-priority task has been created in your area")
                .data("{\"taskId\": 456, \"priority\": \"HIGH\", \"location\": \"Springfield, IL\"}")
                .read(false)
                .sentAt(now.minusMinutes(15))
                .deliveredAt(now.minusMinutes(14))
                .createdAt(now.minusMinutes(15))
                .build(),
            
            Notification.builder()
                .id(4L)
                .userId(101L)
                .type(NotificationType.SMS)
                .title("Task Reassignment")
                .message("Task #456 has been reassigned to you")
                .data("{\"taskId\": 456, \"previousTechnician\": 103}")
                .read(false)
                .sentAt(now.minusMinutes(5))
                .createdAt(now.minusMinutes(5))
                .build(),
            
            Notification.builder()
                .id(5L)
                .userId(103L)
                .type(NotificationType.PUSH)
                .title("Task Completed")
                .message("Task #789 has been marked as completed")
                .data("{\"taskId\": 789, \"completedBy\": 101}")
                .read(true)
                .sentAt(now.minusHours(1))
                .deliveredAt(now.minusHours(1))
                .createdAt(now.minusHours(1))
                .build(),
            
            Notification.builder()
                .id(6L)
                .userId(102L)
                .type(NotificationType.EMAIL)
                .title("Weekly Task Summary")
                .message("You completed 15 tasks this week. Great job!")
                .data("{\"completedTasks\": 15, \"weekStart\": \"" + now.minusDays(6).toLocalDate() + "\", \"weekEnd\": \"" + now.toLocalDate() + "\"}")
                .read(false)
                .sentAt(now.minusHours(3))
                .deliveredAt(now.minusHours(3))
                .createdAt(now.minusHours(3))
                .build()
        );
    }
}
