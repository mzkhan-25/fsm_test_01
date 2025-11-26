import L from 'leaflet';
import { getPriorityColor } from '../services/taskService';
import { getStatusColor } from '../services/technicianService';

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

/**
 * Creates a custom marker icon for technicians with status color
 * @param {string} status - Technician status (available, busy, offline)
 * @returns {L.DivIcon} Leaflet DivIcon
 */
export function createTechnicianMarkerIcon(status) {
  const color = getStatusColor(status);
  
  return L.divIcon({
    className: 'technician-marker-icon',
    html: `
      <div class="technician-marker" style="background-color: ${color};">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="white">
          <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
        </svg>
      </div>
    `,
    iconSize: [32, 40],
    iconAnchor: [16, 40],
    popupAnchor: [0, -40],
  });
}
