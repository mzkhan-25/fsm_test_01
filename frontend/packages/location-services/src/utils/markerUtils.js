import L from 'leaflet';
import { getPriorityColor } from '../services/taskService';

/**
 * Creates a custom marker icon with priority color
 * @param {string} priority - Task priority
 * @returns {L.DivIcon} Leaflet DivIcon
 */
export function createTaskMarkerIcon(priority) {
  const color = getPriorityColor(priority);
  
  return L.divIcon({
    className: 'task-marker-icon',
    html: `
      <div class="task-marker" style="background-color: ${color};">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="white">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
        </svg>
      </div>
    `,
    iconSize: [32, 40],
    iconAnchor: [16, 40],
    popupAnchor: [0, -40],
  });
}
