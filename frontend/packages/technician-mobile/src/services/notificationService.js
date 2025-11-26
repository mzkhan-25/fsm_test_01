const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
import { getAuthHeaders, getCurrentUser } from './authService';

// Notification permission states
export const PERMISSION_STATES = {
  GRANTED: 'granted',
  DENIED: 'denied',
  DEFAULT: 'default',
};

// Notification types
export const NOTIFICATION_TYPES = {
  TASK_ASSIGNED: 'TASK_ASSIGNED',
  TASK_UPDATED: 'TASK_UPDATED',
  SYSTEM: 'SYSTEM',
};

// Store for notification listeners
const notificationListeners = new Set();

// Store for unread count listeners
const badgeListeners = new Set();

// Current unread count
let unreadCount = 0;

// Device token storage key
const DEVICE_TOKEN_KEY = 'fcm_device_token';

/**
 * Check if push notifications are supported by the browser
 * @returns {boolean} Whether push notifications are supported
 */
export const isPushNotificationSupported = () => {
  return 'Notification' in window && 'serviceWorker' in navigator && 'PushManager' in window;
};

/**
 * Get the current notification permission status
 * @returns {string} Current permission status ('granted', 'denied', or 'default')
 */
export const getNotificationPermission = () => {
  if (!isPushNotificationSupported()) {
    return PERMISSION_STATES.DENIED;
  }
  return Notification.permission;
};

/**
 * Request permission to send notifications
 * @returns {Promise<string>} The permission result ('granted', 'denied', or 'default')
 */
export const requestNotificationPermission = async () => {
  if (!isPushNotificationSupported()) {
    console.warn('Push notifications are not supported in this browser');
    return PERMISSION_STATES.DENIED;
  }

  try {
    const permission = await Notification.requestPermission();
    return permission;
  } catch (error) {
    console.error('Error requesting notification permission:', error);
    return PERMISSION_STATES.DENIED;
  }
};

/**
 * Generate a mock device token for web push notifications
 * In a real implementation, this would come from Firebase Cloud Messaging
 * @returns {string} A device token
 */
export const generateDeviceToken = () => {
  // Generate a unique token for this browser/device
  // In production, this would be obtained from FCM SDK
  const existingToken = localStorage.getItem(DEVICE_TOKEN_KEY);
  if (existingToken) {
    return existingToken;
  }

  const newToken = `web_${Date.now()}_${Math.random().toString(36).substring(2, 15)}`;
  localStorage.setItem(DEVICE_TOKEN_KEY, newToken);
  return newToken;
};

/**
 * Get the stored device token
 * @returns {string|null} The stored device token or null
 */
export const getDeviceToken = () => {
  return localStorage.getItem(DEVICE_TOKEN_KEY);
};

/**
 * Clear the stored device token
 */
export const clearDeviceToken = () => {
  localStorage.removeItem(DEVICE_TOKEN_KEY);
};

/**
 * Register device token with the backend
 * @param {string} deviceToken - The device token to register
 * @returns {Promise<Object>} The registration response
 */
export const registerDeviceToken = async (deviceToken) => {
  const user = getCurrentUser();
  if (!user || !user.id) {
    throw new Error('User must be logged in to register device token');
  }

  const response = await fetch(`${API_BASE_URL}/api/notifications/devices`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({
      userId: parseInt(user.id, 10),
      deviceToken,
      platform: 'web',
    }),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Failed to register device' }));
    throw new Error(error.message || 'Failed to register device token');
  }

  return response.json();
};

/**
 * Unregister device token from the backend
 * @returns {Promise<void>}
 */
export const unregisterDeviceToken = async () => {
  const deviceToken = getDeviceToken();
  if (!deviceToken) {
    return;
  }

  try {
    await fetch(`${API_BASE_URL}/api/notifications/devices/${deviceToken}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });
  } catch (error) {
    console.error('Error unregistering device token:', error);
  }

  clearDeviceToken();
};

/**
 * Display a system notification
 * @param {Object} notification - The notification data
 * @param {string} notification.title - The notification title
 * @param {string} notification.message - The notification body
 * @param {Object} [notification.data] - Additional notification data
 * @returns {Notification|null} The created notification or null
 */
export const showSystemNotification = (notification) => {
  if (getNotificationPermission() !== PERMISSION_STATES.GRANTED) {
    console.warn('Notification permission not granted');
    return null;
  }

  try {
    const systemNotification = new Notification(notification.title, {
      body: notification.message,
      icon: '/notification-icon.png',
      badge: '/badge-icon.png',
      tag: notification.id ? `notification-${notification.id}` : undefined,
      data: notification.data,
      requireInteraction: true,
    });

    systemNotification.onclick = () => {
      window.focus();
      // Parse data if it's a string
      let data = notification.data;
      if (typeof data === 'string') {
        try {
          data = JSON.parse(data);
        } catch {
          console.warn('Failed to parse notification data:', data);
          data = {};
        }
      }

      if (data?.taskId) {
        // Notify listeners about notification tap with task ID
        notifyListeners({
          type: 'notification_tap',
          taskId: data.taskId,
        });
      }
      systemNotification.close();
    };

    return systemNotification;
  } catch (error) {
    console.error('Error showing system notification:', error);
    return null;
  }
};

/**
 * Add a listener for notification events
 * @param {Function} listener - The listener function
 * @returns {Function} Cleanup function to remove the listener
 */
export const addNotificationListener = (listener) => {
  notificationListeners.add(listener);
  return () => notificationListeners.delete(listener);
};

/**
 * Notify all listeners about a notification event
 * @param {Object} event - The notification event
 */
export const notifyListeners = (event) => {
  notificationListeners.forEach(listener => {
    try {
      listener(event);
    } catch (error) {
      console.error('Error in notification listener:', error);
    }
  });
};

/**
 * Handle incoming push notification
 * @param {Object} notification - The notification data
 * @param {boolean} isBackground - Whether the app is in background
 */
export const handlePushNotification = (notification, isBackground = false) => {
  // Parse data if it's a string
  let data = notification.data;
  if (typeof data === 'string') {
    try {
      data = JSON.parse(data);
    } catch {
      console.warn('Failed to parse notification data:', data);
      data = {};
    }
  }

  if (isBackground) {
    // Show system notification for background
    showSystemNotification(notification);
  } else {
    // Emit event for foreground handling (in-app alert)
    notifyListeners({
      type: 'foreground_notification',
      notification: {
        ...notification,
        data,
      },
    });
  }

  // Increment unread count
  incrementUnreadCount();
};

/**
 * Get unread notification count
 * @returns {number} The unread count
 */
export const getUnreadCount = () => {
  return unreadCount;
};

/**
 * Increment unread count
 */
export const incrementUnreadCount = () => {
  unreadCount++;
  updateBadge(unreadCount);
  notifyBadgeListeners();
};

/**
 * Decrement unread count
 */
export const decrementUnreadCount = () => {
  if (unreadCount > 0) {
    unreadCount--;
    updateBadge(unreadCount);
    notifyBadgeListeners();
  }
};

/**
 * Reset unread count to zero
 */
export const resetUnreadCount = () => {
  unreadCount = 0;
  updateBadge(0);
  notifyBadgeListeners();
};

/**
 * Set unread count to a specific value
 * @param {number} count - The new unread count
 */
export const setUnreadCount = (count) => {
  unreadCount = Math.max(0, count);
  updateBadge(unreadCount);
  notifyBadgeListeners();
};

/**
 * Update the app badge with unread count
 * @param {number} count - The badge count
 */
export const updateBadge = (count) => {
  // Use Badge API if available
  if ('setAppBadge' in navigator) {
    if (count > 0) {
      navigator.setAppBadge(count).catch(console.error);
    } else {
      navigator.clearAppBadge().catch(console.error);
    }
  }

  // Also update document title as fallback
  const baseTitle = 'FSM Technician';
  document.title = count > 0 ? `(${count}) ${baseTitle}` : baseTitle;
};

/**
 * Add a listener for badge count changes
 * @param {Function} listener - The listener function
 * @returns {Function} Cleanup function to remove the listener
 */
export const addBadgeListener = (listener) => {
  badgeListeners.add(listener);
  // Immediately call with current count
  listener(unreadCount);
  return () => badgeListeners.delete(listener);
};

/**
 * Notify all badge listeners about count change
 */
const notifyBadgeListeners = () => {
  badgeListeners.forEach(listener => {
    try {
      listener(unreadCount);
    } catch (error) {
      console.error('Error in badge listener:', error);
    }
  });
};

/**
 * Fetch notifications from the backend
 * @param {Object} options - Query options
 * @param {boolean} [options.unreadOnly] - Only fetch unread notifications
 * @param {number} [options.limit] - Maximum number of notifications to fetch
 * @returns {Promise<Array>} Array of notifications
 */
export const fetchNotifications = async ({ unreadOnly = false, limit = 20 } = {}) => {
  const user = getCurrentUser();
  if (!user || !user.id) {
    throw new Error('User must be logged in to fetch notifications');
  }

  const url = new URL(`${API_BASE_URL}/api/notifications`);
  url.searchParams.append('userId', user.id);
  if (unreadOnly) {
    url.searchParams.append('unread', 'true');
  }
  url.searchParams.append('limit', limit.toString());

  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Failed to fetch notifications' }));
    throw new Error(error.message || 'Failed to fetch notifications');
  }

  const data = await response.json();
  const notifications = data.notifications || data;

  // Update unread count based on fetched data
  if (Array.isArray(notifications)) {
    const unread = notifications.filter(n => !n.read).length;
    setUnreadCount(unread);
  }

  return notifications;
};

/**
 * Mark a notification as read
 * @param {number} notificationId - The notification ID
 * @returns {Promise<Object>} The updated notification
 */
export const markNotificationAsRead = async (notificationId) => {
  const response = await fetch(`${API_BASE_URL}/api/notifications/${notificationId}/read`, {
    method: 'PUT',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Failed to mark notification as read' }));
    throw new Error(error.message || 'Failed to mark notification as read');
  }

  decrementUnreadCount();
  return response.json();
};

/**
 * Mark all notifications as read
 * @returns {Promise<void>}
 */
export const markAllNotificationsAsRead = async () => {
  const user = getCurrentUser();
  if (!user || !user.id) {
    throw new Error('User must be logged in');
  }

  const response = await fetch(`${API_BASE_URL}/api/notifications/read-all`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify({ userId: parseInt(user.id, 10) }),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Failed to mark all as read' }));
    throw new Error(error.message || 'Failed to mark all notifications as read');
  }

  resetUnreadCount();
};

/**
 * Initialize push notifications for the logged-in user
 * @returns {Promise<Object>} Initialization result
 */
export const initializePushNotifications = async () => {
  const result = {
    supported: isPushNotificationSupported(),
    permission: getNotificationPermission(),
    deviceToken: null,
    registered: false,
  };

  if (!result.supported) {
    console.warn('Push notifications are not supported');
    return result;
  }

  // Request permission if not already granted
  if (result.permission === PERMISSION_STATES.DEFAULT) {
    result.permission = await requestNotificationPermission();
  }

  if (result.permission !== PERMISSION_STATES.GRANTED) {
    console.warn('Notification permission not granted');
    return result;
  }

  // Generate/get device token
  result.deviceToken = generateDeviceToken();

  // Register device token with backend
  try {
    await registerDeviceToken(result.deviceToken);
    result.registered = true;
  } catch (error) {
    console.error('Failed to register device token:', error);
  }

  return result;
};

/**
 * Cleanup push notifications on logout
 * @returns {Promise<void>}
 */
export const cleanupPushNotifications = async () => {
  try {
    await unregisterDeviceToken();
  } catch (error) {
    console.error('Error cleaning up push notifications:', error);
  }
  resetUnreadCount();
};
