import { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import { addNotificationListener } from '../services/notificationService';
import './NotificationAlert.css';

/**
 * NotificationAlert component displays in-app notifications
 * when the app is in the foreground.
 */
function NotificationAlert({ onNotificationTap }) {
  const [notification, setNotification] = useState(null);
  const [isVisible, setIsVisible] = useState(false);

  const handleDismiss = useCallback(() => {
    setIsVisible(false);
    setTimeout(() => setNotification(null), 300); // Wait for animation
  }, []);

  const handleTap = useCallback(() => {
    if (notification && onNotificationTap) {
      // Parse data if it's a string
      let data = notification.data;
      if (typeof data === 'string') {
        try {
          data = JSON.parse(data);
        } catch {
          console.warn('Failed to parse notification data');
          data = {};
        }
      }

      if (data?.taskId) {
        onNotificationTap(data.taskId);
      }
    }
    handleDismiss();
  }, [notification, onNotificationTap, handleDismiss]);

  useEffect(() => {
    const handleNotificationEvent = (event) => {
      if (event.type === 'foreground_notification') {
        setNotification(event.notification);
        setIsVisible(true);

        // Auto-dismiss after 5 seconds
        const timer = setTimeout(() => {
          handleDismiss();
        }, 5000);

        return () => clearTimeout(timer);
      }
    };

    const unsubscribe = addNotificationListener(handleNotificationEvent);
    return unsubscribe;
  }, [handleDismiss]);

  if (!notification || !isVisible) {
    return null;
  }

  return (
    <div 
      className={`notification-alert ${isVisible ? 'visible' : ''}`}
      role="alert"
      aria-live="assertive"
    >
      <div className="notification-alert-content" onClick={handleTap}>
        <div className="notification-alert-icon" aria-hidden="true">
          🔔
        </div>
        <div className="notification-alert-text">
          <h4 className="notification-alert-title">{notification.title}</h4>
          <p className="notification-alert-message">{notification.message}</p>
        </div>
        <button 
          className="notification-alert-close"
          onClick={(e) => {
            e.stopPropagation();
            handleDismiss();
          }}
          aria-label="Dismiss notification"
        >
          ✕
        </button>
      </div>
    </div>
  );
}

NotificationAlert.propTypes = {
  onNotificationTap: PropTypes.func,
};

export default NotificationAlert;
