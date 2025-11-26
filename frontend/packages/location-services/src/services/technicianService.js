/**
 * Technician service for fetching technician location data from the API
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * Technician status enum
 */
export const TechnicianStatus = {
  AVAILABLE: 'available',
  BUSY: 'busy',
  OFFLINE: 'offline',
};

/**
 * Fetches all active technician locations from the API
 * @returns {Promise<Array>} Array of technician location objects
 */
export async function fetchTechnicianLocations() {
  const url = `${API_BASE_URL}/technicians/locations`;

  const response = await fetch(url);
  
  if (!response.ok) {
    throw new Error(`Failed to fetch technician locations: ${response.status} ${response.statusText}`);
  }
  
  return response.json();
}

/**
 * Gets color for a technician status
 * @param {string} status - Technician status (available, busy, offline)
 * @returns {string} CSS color value
 */
export function getStatusColor(status) {
  switch (status) {
    case TechnicianStatus.AVAILABLE:
      return '#28a745'; // Green
    case TechnicianStatus.BUSY:
      return '#ffc107'; // Yellow
    case TechnicianStatus.OFFLINE:
      return '#6c757d'; // Gray
    default:
      return '#6c757d'; // Gray for unknown
  }
}

/**
 * Gets status label for display
 * @param {string} status - Technician status
 * @returns {string} Human-readable status label
 */
export function getStatusLabel(status) {
  switch (status) {
    case TechnicianStatus.AVAILABLE:
      return 'Available';
    case TechnicianStatus.BUSY:
      return 'Busy';
    case TechnicianStatus.OFFLINE:
      return 'Offline';
    default:
      return 'Unknown';
  }
}
