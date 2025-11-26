import { Marker, Popup } from 'react-leaflet';
import { getPriorityColor, getPriorityLabel, getPriorityTextColor } from '../services/taskService';
import { createTaskMarkerIcon } from '../utils/markerUtils';

/**
 * TaskMarker component that displays a task on the map
 * @param {Object} props - Component props
 * @param {Object} props.task - Task object with coordinates
 * @param {number} props.task.id - Task ID
 * @param {string} props.task.title - Task title
 * @param {string} props.task.description - Task description
 * @param {string} props.task.clientAddress - Client address
 * @param {string} props.task.priority - Task priority (HIGH, MEDIUM, LOW)
 * @param {Object} props.task.coordinates - Task coordinates
 * @param {number} props.task.coordinates.lat - Latitude
 * @param {number} props.task.coordinates.lng - Longitude
 * @param {function} props.onClick - Optional click handler
 */
const TaskMarker = ({ task, onClick }) => {
  if (!task || !task.coordinates) {
    return null;
  }

  const { coordinates, id, title, description, clientAddress, priority } = task;
  const icon = createTaskMarkerIcon(priority);
  const priorityLabel = getPriorityLabel(priority);

  const handleClick = () => {
    if (onClick) {
      onClick(task);
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
          <p className="task-popup-id">Task #{id}</p>
          <p className="task-popup-address">{clientAddress}</p>
          {description && (
            <p className="task-popup-description">{description}</p>
          )}
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
      </Popup>
    </Marker>
  );
};

export default TaskMarker;
