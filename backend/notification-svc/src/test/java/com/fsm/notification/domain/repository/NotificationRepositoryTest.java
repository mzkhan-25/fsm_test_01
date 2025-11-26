package com.fsm.notification.domain.repository;

import com.fsm.notification.domain.model.Notification;
import com.fsm.notification.domain.model.Notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for NotificationRepository with actual database operations.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class NotificationRepositoryTest {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }
    
    @Test
    void testRepositoryIsInjected() {
        assertNotNull(notificationRepository);
    }
    
    @Test
    void testGetHardcodedNotificationsReturnsValidList() {
        List<Notification> notifications = notificationRepository.getHardcodedNotifications();
        
        assertNotNull(notifications);
        assertEquals(6, notifications.size(), "Should return 6 hardcoded notifications");
    }
    
    @Test
    void testHardcodedNotificationsHaveValidIds() {
        List<Notification> notifications = notificationRepository.getHardcodedNotifications();
        
        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            assertNotNull(notification.getId(), "Notification " + i + " should have an ID");
            assertEquals((long) (i + 1), notification.getId(), "Notification ID should be sequential");
        }
    }
    
    @Test
    void testHardcodedNotificationsHaveRequiredFields() {
        List<Notification> notifications = notificationRepository.getHardcodedNotifications();
        
        for (Notification notification : notifications) {
            assertNotNull(notification.getUserId(), "Notification should have a user ID");
            assertNotNull(notification.getType(), "Notification should have a type");
            assertNotNull(notification.getTitle(), "Notification should have a title");
            assertNotNull(notification.getMessage(), "Notification should have a message");
            assertNotNull(notification.getRead(), "Notification should have read status");
            assertNotNull(notification.getCreatedAt(), "Notification should have createdAt");
        }
    }
    
    @Test
    void testHardcodedNotificationsHaveValidTypes() {
        List<Notification> notifications = notificationRepository.getHardcodedNotifications();
        
        boolean hasPush = false;
        boolean hasEmail = false;
        boolean hasSms = false;
        
        for (Notification notification : notifications) {
            if (notification.getType() == NotificationType.PUSH) hasPush = true;
            if (notification.getType() == NotificationType.EMAIL) hasEmail = true;
            if (notification.getType() == NotificationType.SMS) hasSms = true;
        }
        
        assertTrue(hasPush, "Should have at least one PUSH notification");
        assertTrue(hasEmail, "Should have at least one EMAIL notification");
        assertTrue(hasSms, "Should have at least one SMS notification");
    }
    
    @Test
    void testHardcodedNotificationsHaveVariedReadStatus() {
        List<Notification> notifications = notificationRepository.getHardcodedNotifications();
        
        boolean hasRead = false;
        boolean hasUnread = false;
        
        for (Notification notification : notifications) {
            if (notification.getRead()) hasRead = true;
            else hasUnread = true;
        }
        
        assertTrue(hasRead, "Should have at least one read notification");
        assertTrue(hasUnread, "Should have at least one unread notification");
    }
    
    @Test
    void testSaveNotification() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Test Notification")
            .message("Test message")
            .data("{\"key\": \"value\"}")
            .read(false)
            .build();
        
        Notification saved = notificationRepository.save(notification);
        
        assertNotNull(saved.getId());
        assertEquals(101L, saved.getUserId());
        assertEquals(NotificationType.PUSH, saved.getType());
        assertEquals("Test Notification", saved.getTitle());
        assertEquals("Test message", saved.getMessage());
        assertEquals("{\"key\": \"value\"}", saved.getData());
        assertFalse(saved.getRead());
    }
    
    @Test
    void testFindById() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .read(false)
            .build();
        
        Notification saved = notificationRepository.save(notification);
        Optional<Notification> found = notificationRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Test", found.get().getTitle());
    }
    
    @Test
    void testFindByUserId() {
        Notification notification1 = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Test 1")
            .message("Message 1")
            .read(false)
            .build();
        
        Notification notification2 = Notification.builder()
            .userId(101L)
            .type(NotificationType.EMAIL)
            .title("Test 2")
            .message("Message 2")
            .read(false)
            .build();
        
        Notification notification3 = Notification.builder()
            .userId(102L)
            .type(NotificationType.PUSH)
            .title("Test 3")
            .message("Message 3")
            .read(false)
            .build();
        
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);
        
        List<Notification> user101Notifications = notificationRepository.findByUserId(101L);
        
        assertEquals(2, user101Notifications.size());
        assertTrue(user101Notifications.stream().allMatch(n -> n.getUserId().equals(101L)));
    }
    
    @Test
    void testFindByUserIdAndRead() {
        Notification notification1 = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Test 1")
            .message("Message 1")
            .read(false)
            .build();
        
        Notification notification2 = Notification.builder()
            .userId(101L)
            .type(NotificationType.EMAIL)
            .title("Test 2")
            .message("Message 2")
            .read(true)
            .build();
        
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndRead(101L, false);
        
        assertEquals(1, unreadNotifications.size());
        assertFalse(unreadNotifications.get(0).getRead());
        assertEquals("Test 1", unreadNotifications.get(0).getTitle());
    }
    
    @Test
    void testFindByUserIdAndReadOrderByCreatedAtDesc() {
        LocalDateTime now = LocalDateTime.now();
        
        Notification notification1 = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Old")
            .message("Message 1")
            .read(false)
            .createdAt(now.minusHours(2))
            .build();
        
        Notification notification2 = Notification.builder()
            .userId(101L)
            .type(NotificationType.EMAIL)
            .title("New")
            .message("Message 2")
            .read(false)
            .createdAt(now)
            .build();
        
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        
        List<Notification> notifications = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(101L, false);
        
        assertEquals(2, notifications.size());
        assertEquals("New", notifications.get(0).getTitle());
        assertEquals("Old", notifications.get(1).getTitle());
    }
    
    @Test
    void testFindByUserIdAndType() {
        Notification notification1 = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Push 1")
            .message("Message 1")
            .read(false)
            .build();
        
        Notification notification2 = Notification.builder()
            .userId(101L)
            .type(NotificationType.EMAIL)
            .title("Email 1")
            .message("Message 2")
            .read(false)
            .build();
        
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        
        List<Notification> pushNotifications = notificationRepository.findByUserIdAndType(101L, NotificationType.PUSH);
        
        assertEquals(1, pushNotifications.size());
        assertEquals(NotificationType.PUSH, pushNotifications.get(0).getType());
        assertEquals("Push 1", pushNotifications.get(0).getTitle());
    }
    
    @Test
    void testCountByUserIdAndRead() {
        Notification notification1 = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Test 1")
            .message("Message 1")
            .read(false)
            .build();
        
        Notification notification2 = Notification.builder()
            .userId(101L)
            .type(NotificationType.EMAIL)
            .title("Test 2")
            .message("Message 2")
            .read(false)
            .build();
        
        Notification notification3 = Notification.builder()
            .userId(101L)
            .type(NotificationType.SMS)
            .title("Test 3")
            .message("Message 3")
            .read(true)
            .build();
        
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);
        
        long unreadCount = notificationRepository.countByUserIdAndRead(101L, false);
        
        assertEquals(2, unreadCount);
    }
    
    @Test
    void testDeleteNotification() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .read(false)
            .build();
        
        Notification saved = notificationRepository.save(notification);
        Long id = saved.getId();
        
        notificationRepository.deleteById(id);
        
        Optional<Notification> found = notificationRepository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    void testUpdateNotification() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Original")
            .message("Original message")
            .read(false)
            .build();
        
        Notification saved = notificationRepository.save(notification);
        saved.setTitle("Updated");
        saved.markAsRead();
        
        Notification updated = notificationRepository.save(saved);
        
        assertEquals("Updated", updated.getTitle());
        assertTrue(updated.getRead());
    }
    
    @Test
    void testFindAll() {
        Notification notification1 = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Test 1")
            .message("Message 1")
            .read(false)
            .build();
        
        Notification notification2 = Notification.builder()
            .userId(102L)
            .type(NotificationType.EMAIL)
            .title("Test 2")
            .message("Message 2")
            .read(false)
            .build();
        
        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        
        List<Notification> allNotifications = notificationRepository.findAll();
        
        assertEquals(2, allNotifications.size());
    }
    
    @Test
    void testFindRecentByUserId() {
        LocalDateTime now = LocalDateTime.now();
        
        // Create notification from 10 days ago (within 30 days)
        Notification recentNotification = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Recent")
            .message("Recent message")
            .read(false)
            .createdAt(now.minusDays(10))
            .build();
        
        // Create notification from 40 days ago (outside 30 days)
        Notification oldNotification = Notification.builder()
            .userId(101L)
            .type(NotificationType.EMAIL)
            .title("Old")
            .message("Old message")
            .read(false)
            .createdAt(now.minusDays(40))
            .build();
        
        // Create notification for different user
        Notification otherUserNotification = Notification.builder()
            .userId(102L)
            .type(NotificationType.PUSH)
            .title("Other User")
            .message("Other user message")
            .read(false)
            .createdAt(now.minusDays(5))
            .build();
        
        notificationRepository.save(recentNotification);
        notificationRepository.save(oldNotification);
        notificationRepository.save(otherUserNotification);
        
        // Query for notifications from last 30 days
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        List<Notification> recentNotifications = notificationRepository.findRecentByUserId(101L, thirtyDaysAgo);
        
        assertEquals(1, recentNotifications.size());
        assertEquals("Recent", recentNotifications.get(0).getTitle());
        assertEquals(101L, recentNotifications.get(0).getUserId());
    }
    
    @Test
    void testFindRecentByUserIdOrdersByCreatedAtDesc() {
        LocalDateTime now = LocalDateTime.now();
        
        Notification older = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Older")
            .message("Older message")
            .read(false)
            .createdAt(now.minusDays(20))
            .build();
        
        Notification newer = Notification.builder()
            .userId(101L)
            .type(NotificationType.EMAIL)
            .title("Newer")
            .message("Newer message")
            .read(false)
            .createdAt(now.minusDays(5))
            .build();
        
        notificationRepository.save(older);
        notificationRepository.save(newer);
        
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        List<Notification> notifications = notificationRepository.findRecentByUserId(101L, thirtyDaysAgo);
        
        assertEquals(2, notifications.size());
        assertEquals("Newer", notifications.get(0).getTitle());
        assertEquals("Older", notifications.get(1).getTitle());
    }
    
    @Test
    void testFindRecentByUserIdReturnsEmptyForNoRecentNotifications() {
        LocalDateTime now = LocalDateTime.now();
        
        // Create notification from 40 days ago (outside 30 days)
        Notification oldNotification = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Old")
            .message("Old message")
            .read(false)
            .createdAt(now.minusDays(40))
            .build();
        
        notificationRepository.save(oldNotification);
        
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        List<Notification> notifications = notificationRepository.findRecentByUserId(101L, thirtyDaysAgo);
        
        assertTrue(notifications.isEmpty());
    }
    
    @Test
    void testFindRecentNotificationsForUser() {
        LocalDateTime now = LocalDateTime.now();
        
        // Create notification within 30 days
        Notification recentNotification = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Recent")
            .message("Recent message")
            .read(false)
            .createdAt(now.minusDays(15))
            .build();
        
        // Create notification outside 30 days
        Notification oldNotification = Notification.builder()
            .userId(101L)
            .type(NotificationType.EMAIL)
            .title("Old")
            .message("Old message")
            .read(false)
            .createdAt(now.minusDays(45))
            .build();
        
        notificationRepository.save(recentNotification);
        notificationRepository.save(oldNotification);
        
        List<Notification> notifications = notificationRepository.findRecentNotificationsForUser(101L);
        
        assertEquals(1, notifications.size());
        assertEquals("Recent", notifications.get(0).getTitle());
    }
    
    @Test
    void testCountUnreadForUser() {
        Notification unread1 = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Unread 1")
            .message("Message 1")
            .read(false)
            .build();
        
        Notification unread2 = Notification.builder()
            .userId(101L)
            .type(NotificationType.EMAIL)
            .title("Unread 2")
            .message("Message 2")
            .read(false)
            .build();
        
        Notification read = Notification.builder()
            .userId(101L)
            .type(NotificationType.SMS)
            .title("Read")
            .message("Read message")
            .read(true)
            .build();
        
        Notification otherUser = Notification.builder()
            .userId(102L)
            .type(NotificationType.PUSH)
            .title("Other User")
            .message("Other user message")
            .read(false)
            .build();
        
        notificationRepository.save(unread1);
        notificationRepository.save(unread2);
        notificationRepository.save(read);
        notificationRepository.save(otherUser);
        
        long count = notificationRepository.countUnreadForUser(101L);
        
        assertEquals(2, count);
    }
    
    @Test
    void testCountUnreadForUserReturnsZeroWhenNoUnread() {
        Notification read = Notification.builder()
            .userId(101L)
            .type(NotificationType.PUSH)
            .title("Read")
            .message("Message")
            .read(true)
            .build();
        
        notificationRepository.save(read);
        
        long count = notificationRepository.countUnreadForUser(101L);
        
        assertEquals(0, count);
    }
    
    @Test
    void testCountUnreadForUserReturnsZeroForNonExistentUser() {
        long count = notificationRepository.countUnreadForUser(999L);
        
        assertEquals(0, count);
    }
}
