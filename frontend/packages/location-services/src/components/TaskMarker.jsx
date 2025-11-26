import { Marker, Popup } from 'react-leaflet';
import { getPriorityColor, getPriorityLabel, getPriorityTextColor } from '../services/taskService';
import { createTaskMarkerIcon } from '../utils/markerUtils';

/**
 * Truncates description to a maximum length
 * @param {string} text - Text to truncate
 * @param {number} maxLength - Maximum length
 * @returns {string} Truncated text
 */
const truncateDescription = (text, maxLength = 100) => {
  if (!text || text.length <= maxLength) {
    return text;
  }
  return text.substring(0, maxLength).trim() + '...';
};

/**
 * TaskMarker component that displays a task on the map
 * @param {Object} props - Component props
 * @param {Object} props.task - Task object with coordinates
 * @param {number} props.task.id - Task ID
 * @param {string} props.task.title - Task title
 * @param {string} props.task.description - Task description
 * @param {string} props.task.clientAddress - Client address
 * @param {string} props.task.priority - Task priority (HIGH, MEDIUM, LOW)
 * @param {number} props.task.estimatedDuration - Estimated duration in minutes
 * @param {Object} props.task.coordinates - Task coordinates
 * @param {number} props.task.coordinates.lat - Latitude
 * @param {number} props.task.coordinates.lng - Longitude
 * @param {function} props.onClick - Optional click handler
 * @param {function} props.onAssignTask - Optional handler for assigning task
 * @param {function} props.onViewDetails - Optional handler for viewing full task details
 */
const TaskMarker = ({ task, onClick, onAssignTask, onViewDetails }) => {
  if (!task || !task.coordinates) {
    return null;
  }

  const { coordinates, id, title, description, clientAddress, priority, estimatedDuration } = task;
  const icon = createTaskMarkerIcon(priority);
  const priorityLabel = getPriorityLabel(priority);
  const truncatedDescription = truncateDescription(description);

  const handleClick = () => {
    if (onClick) {
      onClick(task);
    }
  };

  const handleAssignClick = (e) => {
    e.stopPropagation();
    if (onAssignTask) {
      onAssignTask(task);
    }
  };

  const handleViewDetailsClick = (e) => {
    e.stopPropagation();
    if (onViewDetails) {
      onViewDetails(task);
    }
  };

  return (
    <Marker
      position={[coordinates.lat, coordinates.lng]}
      icon={icon}
      eventHandlers={{ click: handleClick }}
    >
      <Popup>
        <div className="task-popup">
          <h3 className="task-popup-title">{title}</h3>
          <div className="task-popup-meta">
            <p className="task-popup-id">Task #{id}</p>
            <span 
              className="task-popup-priority"
              style={{ 
                backgroundColor: getPriorityColor(priority),
                color: getPriorityTextColor(priority),
              }}
            >
              {priorityLabel} Priority
            </span>
          </div>
          <p className="task-popup-address">
            <svg className="task-popup-icon" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
            </svg>
            {clientAddress}
          </p>
          {estimatedDuration && (
            <p className="task-popup-duration">
              <svg className="task-popup-icon" width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm4.2 14.2L11 13V7h1.5v5.2l4.5 2.7-.8 1.3z"/>
              </svg>
              {estimatedDuration} min
            </p>
          )}
          {description && (
            <p className="task-popup-description" title={description}>
              {truncatedDescription}
            </p>
          )}
          <div className="task-popup-actions">
            <button 
              className="task-popup-button task-popup-button-primary"
              onClick={handleAssignClick}
              type="button"
            >
              Assign Task
            </button>
            <button 
              className="task-popup-button task-popup-button-secondary"
              onClick={handleViewDetailsClick}
              type="button"
            >
              View Details
            </button>
          </div>
        </div>
      </Popup>
    </Marker>
  );
};

export default TaskMarker;
