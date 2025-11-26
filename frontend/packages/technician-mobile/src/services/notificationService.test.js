import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  isPushNotificationSupported,
  getNotificationPermission,
  requestNotificationPermission,
  generateDeviceToken,
  getDeviceToken,
  clearDeviceToken,
  registerDeviceToken,
  unregisterDeviceToken,
  showSystemNotification,
  addNotificationListener,
  notifyListeners,
  handlePushNotification,
  getUnreadCount,
  incrementUnreadCount,
  decrementUnreadCount,
  resetUnreadCount,
  setUnreadCount,
  updateBadge,
  addBadgeListener,
  fetchNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  initializePushNotifications,
  cleanupPushNotifications,
  PERMISSION_STATES,
  NOTIFICATION_TYPES,
} from './notificationService';

// Mock fetch
global.fetch = vi.fn();

describe('notificationService', () => {
  let mockNotification;
  let mockNavigator;

  beforeEach(() => {
    // Clear localStorage
    localStorage.clear();
    
    // Reset fetch mock
    vi.mocked(fetch).mockReset();
    
    // Store original navigator properties
    mockNavigator = {
      setAppBadge: vi.fn().mockResolvedValue(undefined),
      clearAppBadge: vi.fn().mockResolvedValue(undefined),
    };

    // Reset unread count
    resetUnreadCount();
    
    // Mock Notification API
    mockNotification = vi.fn().mockImplementation(function(title, options) {
      this.title = title;
      this.options = options;
      this.onclick = null;
      this.close = vi.fn();
    });
    mockNotification.permission = 'default';
    mockNotification.requestPermission = vi.fn().mockResolvedValue('granted');
    
    global.Notification = mockNotification;
    
    // Mock navigator badge API
    Object.defineProperty(navigator, 'setAppBadge', {
      value: mockNavigator.setAppBadge,
      configurable: true,
    });
    Object.defineProperty(navigator, 'clearAppBadge', {
      value: mockNavigator.clearAppBadge,
      configurable: true,
    });
    
    // Mock serviceWorker
    Object.defineProperty(navigator, 'serviceWorker', {
      value: {},
      configurable: true,
    });
    
    // Mock PushManager
    global.PushManager = {};
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('constants', () => {
    it('should export PERMISSION_STATES', () => {
      expect(PERMISSION_STATES).toEqual({
        GRANTED: 'granted',
        DENIED: 'denied',
        DEFAULT: 'default',
      });
    });

    it('should export NOTIFICATION_TYPES', () => {
      expect(NOTIFICATION_TYPES).toEqual({
        TASK_ASSIGNED: 'TASK_ASSIGNED',
        TASK_UPDATED: 'TASK_UPDATED',
        SYSTEM: 'SYSTEM',
      });
    });
  });

  describe('isPushNotificationSupported', () => {
    it('should return true when all APIs are available', () => {
      expect(isPushNotificationSupported()).toBe(true);
    });

    it('should return false when Notification is not available', () => {
      delete global.Notification;
      expect(isPushNotificationSupported()).toBe(false);
      global.Notification = mockNotification;
    });

    it('should return false when serviceWorker is not available', () => {
      // Create a new object without serviceWorker
      const originalServiceWorker = navigator.serviceWorker;
      delete navigator.serviceWorker;
      expect(isPushNotificationSupported()).toBe(false);
      // Restore
      Object.defineProperty(navigator, 'serviceWorker', {
        value: originalServiceWorker,
        configurable: true,
      });
    });

    it('should return false when PushManager is not available', () => {
      delete global.PushManager;
      expect(isPushNotificationSupported()).toBe(false);
      global.PushManager = {};
    });
  });

  describe('getNotificationPermission', () => {
    it('should return current permission status', () => {
      mockNotification.permission = 'granted';
      expect(getNotificationPermission()).toBe('granted');
    });

    it('should return denied when push not supported', () => {
      delete global.Notification;
      expect(getNotificationPermission()).toBe('denied');
      global.Notification = mockNotification;
    });
  });

  describe('requestNotificationPermission', () => {
    it('should request permission and return result', async () => {
      mockNotification.requestPermission.mockResolvedValue('granted');
      const result = await requestNotificationPermission();
      expect(result).toBe('granted');
      expect(mockNotification.requestPermission).toHaveBeenCalled();
    });

    it('should return denied when not supported', async () => {
      delete global.Notification;
      const result = await requestNotificationPermission();
      expect(result).toBe('denied');
      global.Notification = mockNotification;
    });

    it('should return denied on error', async () => {
      mockNotification.requestPermission.mockRejectedValue(new Error('Permission error'));
      const result = await requestNotificationPermission();
      expect(result).toBe('denied');
    });
  });

  describe('device token management', () => {
    it('should generate and store a new device token', () => {
      const token = generateDeviceToken();
      expect(token).toMatch(/^web_\d+_[a-z0-9]+$/);
      expect(localStorage.getItem('fcm_device_token')).toBe(token);
    });

    it('should return existing token if already stored', () => {
      const existingToken = 'existing_token_123';
      localStorage.setItem('fcm_device_token', existingToken);
      const token = generateDeviceToken();
      expect(token).toBe(existingToken);
    });

    it('should get stored device token', () => {
      const token = 'test_token';
      localStorage.setItem('fcm_device_token', token);
      expect(getDeviceToken()).toBe(token);
    });

    it('should return null when no token stored', () => {
      expect(getDeviceToken()).toBeNull();
    });

    it('should clear device token', () => {
      localStorage.setItem('fcm_device_token', 'test_token');
      clearDeviceToken();
      expect(localStorage.getItem('fcm_device_token')).toBeNull();
    });
  });

  describe('registerDeviceToken', () => {
    beforeEach(() => {
      localStorage.setItem('userId', '123');
      localStorage.setItem('token', 'auth_token');
    });

    it('should register device token with backend', async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ success: true }),
      });

      const result = await registerDeviceToken('device_token_123');
      
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/notifications/devices'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({
            userId: 123,
            deviceToken: 'device_token_123',
            platform: 'web',
          }),
        })
      );
      expect(result).toEqual({ success: true });
    });

    it('should throw error when user not logged in', async () => {
      localStorage.clear();
      await expect(registerDeviceToken('token')).rejects.toThrow('User must be logged in');
    });

    it('should throw error on API failure', async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: false,
        json: () => Promise.resolve({ message: 'Registration failed' }),
      });

      await expect(registerDeviceToken('token')).rejects.toThrow('Registration failed');
    });
  });

  describe('unregisterDeviceToken', () => {
    beforeEach(() => {
      localStorage.setItem('fcm_device_token', 'test_token');
      localStorage.setItem('token', 'auth_token');
    });

    it('should unregister device token from backend', async () => {
      vi.mocked(fetch).mockResolvedValue({ ok: true });

      await unregisterDeviceToken();

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/notifications/devices/test_token'),
        expect.objectContaining({ method: 'DELETE' })
      );
      expect(localStorage.getItem('fcm_device_token')).toBeNull();
    });

    it('should do nothing when no token stored', async () => {
      localStorage.removeItem('fcm_device_token');
      await unregisterDeviceToken();
      expect(fetch).not.toHaveBeenCalled();
    });

    it('should clear token even on API error', async () => {
      vi.mocked(fetch).mockRejectedValue(new Error('Network error'));
      await unregisterDeviceToken();
      expect(localStorage.getItem('fcm_device_token')).toBeNull();
    });
  });

  describe('showSystemNotification', () => {
    beforeEach(() => {
      mockNotification.permission = 'granted';
    });

    it('should create system notification', () => {
      const notification = showSystemNotification({
        title: 'Test Title',
        message: 'Test message',
        id: 123,
        data: { taskId: 456 },
      });

      expect(notification).toBeInstanceOf(mockNotification);
      expect(mockNotification).toHaveBeenCalledWith('Test Title', expect.objectContaining({
        body: 'Test message',
        tag: 'notification-123',
        data: { taskId: 456 },
      }));
    });

    it('should return null when permission not granted', () => {
      mockNotification.permission = 'denied';
      const notification = showSystemNotification({
        title: 'Test',
        message: 'Test',
      });
      expect(notification).toBeNull();
    });

    it('should handle notification click with taskId', () => {
      const listener = vi.fn();
      addNotificationListener(listener);

      const notification = showSystemNotification({
        title: 'Test',
        message: 'Test',
        data: { taskId: 123 },
      });

      // Simulate click
      notification.onclick();

      expect(listener).toHaveBeenCalledWith({
        type: 'notification_tap',
        taskId: 123,
      });
      expect(notification.close).toHaveBeenCalled();
    });

    it('should parse string data in notification', () => {
      const listener = vi.fn();
      addNotificationListener(listener);

      const notification = showSystemNotification({
        title: 'Test',
        message: 'Test',
        data: JSON.stringify({ taskId: 789 }),
      });

      notification.onclick();

      expect(listener).toHaveBeenCalledWith({
        type: 'notification_tap',
        taskId: 789,
      });
    });

    it('should return null on error creating notification', () => {
      mockNotification.mockImplementation(() => {
        throw new Error('Notification error');
      });

      const notification = showSystemNotification({
        title: 'Test',
        message: 'Test',
      });

      expect(notification).toBeNull();
    });
  });

  describe('notification listeners', () => {
    it('should add and notify listeners', () => {
      const listener = vi.fn();
      const unsubscribe = addNotificationListener(listener);

      notifyListeners({ type: 'test', data: 'value' });

      expect(listener).toHaveBeenCalledWith({ type: 'test', data: 'value' });

      unsubscribe();
      listener.mockClear();
      notifyListeners({ type: 'test2' });
      expect(listener).not.toHaveBeenCalled();
    });

    it('should handle listener errors gracefully', () => {
      const errorListener = vi.fn().mockImplementation(() => {
        throw new Error('Listener error');
      });
      const goodListener = vi.fn();

      addNotificationListener(errorListener);
      addNotificationListener(goodListener);

      notifyListeners({ type: 'test' });

      expect(goodListener).toHaveBeenCalled();
    });
  });

  describe('handlePushNotification', () => {
    beforeEach(() => {
      mockNotification.permission = 'granted';
      resetUnreadCount();
    });

    it('should handle foreground notification', () => {
      const listener = vi.fn();
      addNotificationListener(listener);

      handlePushNotification({
        title: 'New Task',
        message: 'You have a new task',
        data: { taskId: 123 },
      }, false);

      expect(listener).toHaveBeenCalledWith({
        type: 'foreground_notification',
        notification: expect.objectContaining({
          title: 'New Task',
          message: 'You have a new task',
          data: { taskId: 123 },
        }),
      });
      expect(getUnreadCount()).toBe(1);
    });

    it('should handle background notification', () => {
      handlePushNotification({
        title: 'New Task',
        message: 'You have a new task',
        data: { taskId: 123 },
      }, true);

      expect(mockNotification).toHaveBeenCalledWith('New Task', expect.any(Object));
      expect(getUnreadCount()).toBe(1);
    });

    it('should parse string data', () => {
      const listener = vi.fn();
      addNotificationListener(listener);

      handlePushNotification({
        title: 'Test',
        message: 'Test',
        data: JSON.stringify({ taskId: 456 }),
      }, false);

      expect(listener).toHaveBeenCalledWith({
        type: 'foreground_notification',
        notification: expect.objectContaining({
          data: { taskId: 456 },
        }),
      });
    });
  });

  describe('unread count management', () => {
    beforeEach(() => {
      resetUnreadCount();
    });

    it('should get initial unread count of 0', () => {
      expect(getUnreadCount()).toBe(0);
    });

    it('should increment unread count', () => {
      incrementUnreadCount();
      expect(getUnreadCount()).toBe(1);
      incrementUnreadCount();
      expect(getUnreadCount()).toBe(2);
    });

    it('should decrement unread count', () => {
      setUnreadCount(5);
      decrementUnreadCount();
      expect(getUnreadCount()).toBe(4);
    });

    it('should not decrement below zero', () => {
      decrementUnreadCount();
      expect(getUnreadCount()).toBe(0);
    });

    it('should reset unread count', () => {
      setUnreadCount(10);
      resetUnreadCount();
      expect(getUnreadCount()).toBe(0);
    });

    it('should set unread count', () => {
      setUnreadCount(15);
      expect(getUnreadCount()).toBe(15);
    });

    it('should not set negative count', () => {
      setUnreadCount(-5);
      expect(getUnreadCount()).toBe(0);
    });
  });

  describe('updateBadge', () => {
    it('should call setAppBadge with count > 0', () => {
      updateBadge(5);
      expect(mockNavigator.setAppBadge).toHaveBeenCalledWith(5);
      expect(document.title).toContain('(5)');
    });

    it('should call clearAppBadge with count = 0', () => {
      updateBadge(0);
      expect(mockNavigator.clearAppBadge).toHaveBeenCalled();
      expect(document.title).toBe('FSM Technician');
    });
  });

  describe('badge listeners', () => {
    beforeEach(() => {
      resetUnreadCount();
    });

    it('should add badge listener and call immediately', () => {
      setUnreadCount(3);
      const listener = vi.fn();
      addBadgeListener(listener);
      expect(listener).toHaveBeenCalledWith(3);
    });

    it('should notify badge listeners on count change', () => {
      const listener = vi.fn();
      addBadgeListener(listener);
      listener.mockClear();

      incrementUnreadCount();
      expect(listener).toHaveBeenCalledWith(1);
    });

    it('should unsubscribe badge listener', () => {
      const listener = vi.fn();
      const unsubscribe = addBadgeListener(listener);
      listener.mockClear();

      unsubscribe();
      incrementUnreadCount();
      expect(listener).not.toHaveBeenCalled();
    });
  });

  describe('fetchNotifications', () => {
    beforeEach(() => {
      localStorage.setItem('userId', '123');
      localStorage.setItem('token', 'auth_token');
      resetUnreadCount();
    });

    it('should fetch notifications from backend', async () => {
      const notifications = [
        { id: 1, title: 'Test 1', read: false },
        { id: 2, title: 'Test 2', read: true },
      ];
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(notifications),
      });

      const result = await fetchNotifications();

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/notifications?userId=123'),
        expect.any(Object)
      );
      expect(result).toEqual(notifications);
      expect(getUnreadCount()).toBe(1);
    });

    it('should fetch unread only when specified', async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve([]),
      });

      await fetchNotifications({ unreadOnly: true });

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('unread=true'),
        expect.any(Object)
      );
    });

    it('should throw error when user not logged in', async () => {
      localStorage.clear();
      await expect(fetchNotifications()).rejects.toThrow('User must be logged in');
    });

    it('should throw error on API failure', async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: false,
        json: () => Promise.resolve({ message: 'Fetch failed' }),
      });

      await expect(fetchNotifications()).rejects.toThrow('Fetch failed');
    });
  });

  describe('markNotificationAsRead', () => {
    beforeEach(() => {
      localStorage.setItem('token', 'auth_token');
      setUnreadCount(5);
    });

    it('should mark notification as read', async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ id: 123, read: true }),
      });

      const result = await markNotificationAsRead(123);

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/notifications/123/read'),
        expect.objectContaining({ method: 'PUT' })
      );
      expect(result).toEqual({ id: 123, read: true });
      expect(getUnreadCount()).toBe(4);
    });

    it('should throw error on failure', async () => {
      vi.mocked(fetch).mockResolvedValue({
        ok: false,
        json: () => Promise.resolve({ message: 'Mark read failed' }),
      });

      await expect(markNotificationAsRead(123)).rejects.toThrow('Mark read failed');
    });
  });

  describe('markAllNotificationsAsRead', () => {
    beforeEach(() => {
      localStorage.setItem('userId', '123');
      localStorage.setItem('token', 'auth_token');
      setUnreadCount(10);
    });

    it('should mark all notifications as read', async () => {
      vi.mocked(fetch).mockResolvedValue({ ok: true });

      await markAllNotificationsAsRead();

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/notifications/read-all'),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ userId: 123 }),
        })
      );
      expect(getUnreadCount()).toBe(0);
    });

    it('should throw error when user not logged in', async () => {
      localStorage.clear();
      await expect(markAllNotificationsAsRead()).rejects.toThrow('User must be logged in');
    });
  });

  describe('initializePushNotifications', () => {
    beforeEach(() => {
      localStorage.setItem('userId', '123');
      localStorage.setItem('token', 'auth_token');
      mockNotification.permission = 'default';
    });

    it('should initialize push notifications successfully', async () => {
      mockNotification.requestPermission.mockResolvedValue('granted');
      vi.mocked(fetch).mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({ success: true }),
      });

      const result = await initializePushNotifications();

      expect(result.supported).toBe(true);
      expect(result.permission).toBe('granted');
      expect(result.deviceToken).toMatch(/^web_/);
      expect(result.registered).toBe(true);
    });

    it('should handle permission denied', async () => {
      mockNotification.requestPermission.mockResolvedValue('denied');

      const result = await initializePushNotifications();

      expect(result.permission).toBe('denied');
      expect(result.registered).toBe(false);
    });

    it('should handle unsupported browser', async () => {
      delete global.Notification;

      const result = await initializePushNotifications();

      expect(result.supported).toBe(false);
      global.Notification = mockNotification;
    });

    it('should handle registration failure', async () => {
      mockNotification.permission = 'granted';
      vi.mocked(fetch).mockRejectedValue(new Error('Registration failed'));

      const result = await initializePushNotifications();

      expect(result.registered).toBe(false);
    });
  });

  describe('cleanupPushNotifications', () => {
    beforeEach(() => {
      localStorage.setItem('fcm_device_token', 'test_token');
      localStorage.setItem('token', 'auth_token');
      setUnreadCount(5);
    });

    it('should cleanup push notifications', async () => {
      vi.mocked(fetch).mockResolvedValue({ ok: true });

      await cleanupPushNotifications();

      expect(fetch).toHaveBeenCalled();
      expect(getUnreadCount()).toBe(0);
    });

    it('should reset count even on error', async () => {
      vi.mocked(fetch).mockRejectedValue(new Error('Cleanup error'));

      await cleanupPushNotifications();

      expect(getUnreadCount()).toBe(0);
    });
  });
});
