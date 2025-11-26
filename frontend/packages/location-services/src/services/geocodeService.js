/**
 * Geocoding utilities for converting addresses to coordinates
 * 
 * Note: In production, this would integrate with a geocoding service like
 * Google Maps Geocoding API or OpenCage. For demonstration, we generate
 * mock coordinates based on address hash.
 */

/**
 * Default center coordinates (San Francisco)
 */
export const DEFAULT_CENTER = {
  lat: 37.7749,
  lng: -122.4194,
};

/**
 * Generates a deterministic hash from a string
 * @param {string} str - Input string
 * @returns {number} Hash value
 */
function hashString(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // Convert to 32bit integer
  }
  return Math.abs(hash);
}

/**
 * Generates mock coordinates based on address string
 * Creates consistent coordinates for the same address
 * Coordinates are distributed around the default center
 * 
 * @param {string} address - Client address
 * @returns {{ lat: number, lng: number } | null} Coordinates or null if address is invalid
 */
export function geocodeAddress(address) {
  if (!address || typeof address !== 'string' || address.trim() === '') {
    return null;
  }

  const hash = hashString(address);
  
  // Generate offset within ~10km radius of default center
  // Using hash to create deterministic but varied positions
  const latOffset = ((hash % 1000) / 1000 - 0.5) * 0.1; // ~5.5km max
  const lngOffset = (((hash >> 10) % 1000) / 1000 - 0.5) * 0.1;
  
  return {
    lat: DEFAULT_CENTER.lat + latOffset,
    lng: DEFAULT_CENTER.lng + lngOffset,
  };
}

/**
 * Checks if coordinates are valid
 * @param {{ lat: number, lng: number }} coords - Coordinates object
 * @returns {boolean} True if coordinates are valid
 */
export function isValidCoordinates(coords) {
  if (!coords || typeof coords !== 'object') {
    return false;
  }
  
  const { lat, lng } = coords;
  
  return (
    typeof lat === 'number' &&
    typeof lng === 'number' &&
    !isNaN(lat) &&
    !isNaN(lng) &&
    lat >= -90 &&
    lat <= 90 &&
    lng >= -180 &&
    lng <= 180
  );
}

/**
 * Processes tasks to add coordinates
 * Filters out tasks with invalid or missing coordinates
 * 
 * @param {Array} tasks - Array of task objects
 * @returns {Array} Tasks with valid coordinates
 */
export function processTasksWithCoordinates(tasks) {
  if (!Array.isArray(tasks)) {
    return [];
  }

  return tasks
    .map(task => {
      const coords = geocodeAddress(task.clientAddress);
      if (!coords || !isValidCoordinates(coords)) {
        return null;
      }
      return {
        ...task,
        coordinates: coords,
      };
    })
    .filter(Boolean);
}
