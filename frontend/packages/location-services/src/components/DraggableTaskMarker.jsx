import { useRef, useCallback, useMemo } from 'react';
import { Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
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
 * Creates a dragging marker icon with visual feedback
 * @param {string} priority - Task priority
 * @returns {L.DivIcon} Leaflet DivIcon
 */
const createDraggingTaskMarkerIcon = (priority) => {
  const color = getPriorityColor(priority);
  
  return L.divIcon({
    className: 'task-marker-icon dragging',
    html: `
      <div class="task-marker dragging" style="background-color: ${color}; opacity: 0.8; transform: rotate(-45deg) scale(1.2);">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="white">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
        </svg>
      </div>
    `,
    iconSize: [38, 47],
    iconAnchor: [19, 47],
    popupAnchor: [0, -47],
  });
};

/**
 * DraggableTaskMarker component that displays a draggable task on the map
 * @param {Object} props - Component props
 * @param {Object} props.task - Task object with coordinates
 * @param {function} props.onClick - Optional click handler
 * @param {function} props.onAssignTask - Optional handler for assigning task
 * @param {function} props.onViewDetails - Optional handler for viewing full task details
 * @param {function} props.onDragStart - Callback when drag starts
 * @param {function} props.onDrag - Callback during drag with current position
 * @param {function} props.onDragEnd - Callback when drag ends with final position
 * @param {Array} props.technicians - Array of technician objects for drop detection
 * @param {number} props.dropRadius - Radius in pixels for drop zone detection (default: 50)
 */
const DraggableTaskMarker = ({ 
  task, 
  onClick, 
  onAssignTask, 
  onViewDetails,
  onDragStart,
  onDrag,
  onDragEnd,
  technicians = [],
  dropRadius = 50,
}) => {
  const markerRef = useRef(null);
  const originalPositionRef = useRef(null);
  const isDraggingRef = useRef(false);
  const map = useMap();

  // Memoize icons - these need to be called unconditionally
  const normalIcon = useMemo(() => createTaskMarkerIcon(task?.priority), [task?.priority]);
  const draggingIcon = useMemo(() => createDraggingTaskMarkerIcon(task?.priority), [task?.priority]);

  // Memoize click handler
  const handleClick = useCallback(() => {
    if (onClick && task) {
      onClick(task);
    }
  }, [onClick, task]);

  // Memoize assign click handler
  const handleAssignClick = useCallback((e) => {
    e.stopPropagation();
    if (onAssignTask && task) {
      onAssignTask(task);
    }
  }, [onAssignTask, task]);

  // Memoize view details click handler
  const handleViewDetailsClick = useCallback((e) => {
    e.stopPropagation();
    if (onViewDetails && task) {
      onViewDetails(task);
    }
  }, [onViewDetails, task]);

  /**
   * Finds the technician under the current drag position
   * @param {L.LatLng} latlng - Current drag position
   * @returns {Object|null} Technician object or null if none found
   */
  const findTechnicianAtPosition = useCallback((latlng) => {
    if (!map || !technicians || technicians.length === 0) {
      return null;
    }

    const dragPoint = map.latLngToContainerPoint(latlng);
    
    for (const technician of technicians) {
      if (technician.latitude === null || technician.latitude === undefined || 
          technician.longitude === null || technician.longitude === undefined) {
        continue;
      }
      
      const techPoint = map.latLngToContainerPoint([technician.latitude, technician.longitude]);
      const distance = Math.sqrt(
        Math.pow(dragPoint.x - techPoint.x, 2) + 
        Math.pow(dragPoint.y - techPoint.y, 2)
      );
      
      if (distance <= dropRadius) {
        return technician;
      }
    }
    
    return null;
  }, [map, technicians, dropRadius]);

  // Event handlers for the marker
  const eventHandlers = useMemo(() => ({
    click: handleClick,
    dragstart: (e) => {
      const marker = e.target;
      originalPositionRef.current = marker.getLatLng();
      isDraggingRef.current = true;
      
      // Change icon to dragging style
      marker.setIcon(draggingIcon);
      marker.setZIndexOffset(2000);
      
      if (onDragStart && task) {
        onDragStart(task, originalPositionRef.current);
      }
    },
    drag: (e) => {
      const marker = e.target;
      const currentLatLng = marker.getLatLng();
      const nearbyTechnician = findTechnicianAtPosition(currentLatLng);
      
      if (onDrag && task) {
        onDrag(task, currentLatLng, nearbyTechnician);
      }
    },
    dragend: (e) => {
      const marker = e.target;
      const finalLatLng = marker.getLatLng();
      const droppedOnTechnician = findTechnicianAtPosition(finalLatLng);
      
      isDraggingRef.current = false;
      
      // Reset icon back to normal
      marker.setIcon(normalIcon);
      marker.setZIndexOffset(0);
      
      // Always return marker to original position
      if (originalPositionRef.current) {
        marker.setLatLng(originalPositionRef.current);
      }
      
      if (onDragEnd && task) {
        onDragEnd(task, finalLatLng, droppedOnTechnician, originalPositionRef.current);
      }
    },
  }), [handleClick, task, onDragStart, onDrag, onDragEnd, findTechnicianAtPosition, normalIcon, draggingIcon]);

  // Early return after all hooks
  if (!task || !task.coordinates) {
    return null;
  }

  const { coordinates, id, title, description, clientAddress, priority, estimatedDuration } = task;
  const priorityLabel = getPriorityLabel(priority);
  const truncatedDescription = truncateDescription(description);

  return (
    <Marker
      ref={markerRef}
      position={[coordinates.lat, coordinates.lng]}
      icon={normalIcon}
      draggable={true}
      eventHandlers={eventHandlers}
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
          <p className="task-popup-hint">
            <small>💡 Drag this marker onto a technician to assign</small>
          </p>
        </div>
      </Popup>
    </Marker>
  );
};

export default DraggableTaskMarker;
