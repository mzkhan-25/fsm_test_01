import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { addBadgeListener } from '../services/notificationService';
import './NotificationBadge.css';

/**
 * NotificationBadge component displays the unread notification count.
 */
function NotificationBadge({ className }) {
  const [count, setCount] = useState(0);

  useEffect(() => {
    const unsubscribe = addBadgeListener(setCount);
    return unsubscribe;
  }, []);

  if (count === 0) {
    return null;
  }

  return (
    <span 
      className={`notification-badge ${className || ''}`}
      aria-label={`${count} unread notification${count !== 1 ? 's' : ''}`}
    >
      {count > 99 ? '99+' : count}
    </span>
  );
}

NotificationBadge.propTypes = {
  className: PropTypes.string,
};

export default NotificationBadge;
