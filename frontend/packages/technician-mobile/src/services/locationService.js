const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
import { getAuthHeaders, getCurrentUser } from './authService';

// Configuration constants
const LOCATION_UPDATE_INTERVAL = 2 * 60 * 1000; // 2 minutes in milliseconds
const LOW_BATTERY_THRESHOLD = 15; // 15% battery level
const MIN_ACCURACY = 100; // Minimum accuracy in meters (we accept locations up to 100m accuracy)

// Internal state
let trackingInterval = null;
let isTracking = false;
let isPaused = false;
let lastUpdateTime = null;

/**
 * Request location permissions from the user
 * @returns {Promise<boolean>} True if permission granted, false otherwise
 */
export const requestLocationPermission = async () => {
  if (!navigator.geolocation) {
    throw new Error('Geolocation is not supported by this browser');
  }

  try {
    // Check if we have permissions already
    if (navigator.permissions) {
      const result = await navigator.permissions.query({ name: 'geolocation' });
      if (result.state === 'granted') {
        return true;
      } else if (result.state === 'denied') {
        throw new Error('Location permission denied. Please enable location access in browser settings.');
      }
    }

    // Request permission by trying to get current position
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        () => resolve(true),
        (error) => {
          if (error.code === error.PERMISSION_DENIED) {
            reject(new Error('Location permission denied. Please enable location access.'));
          } else {
            reject(new Error(`Failed to get location permission: ${error.message}`));
          }
        },
        { timeout: 10000 }
      );
    });
  } catch (error) {
    throw new Error(`Permission request failed: ${error.message}`);
  }
};

/**
 * Get current battery level
 * @returns {Promise<number>} Battery level percentage (0-100)
 */
export const getBatteryLevel = async () => {
  if ('getBattery' in navigator) {
    try {
      const battery = await navigator.getBattery();
      return Math.round(battery.level * 100);
    } catch (error) {
      console.warn('Battery API not available:', error);
      return null;
    }
  }
  return null;
};

/**
 * Get current position
 * @returns {Promise<{latitude: number, longitude: number, accuracy: number}>}
 */
export const getCurrentPosition = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation is not supported'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracy: position.coords.accuracy,
        });
      },
      (error) => {
        let errorMessage;
        // GeolocationPositionError codes: 1 = PERMISSION_DENIED, 2 = POSITION_UNAVAILABLE, 3 = TIMEOUT
        switch (error.code) {
          case 1: // PERMISSION_DENIED
            errorMessage = 'Location permission denied';
            break;
          case 2: // POSITION_UNAVAILABLE
            errorMessage = 'Location information unavailable';
            break;
          case 3: // TIMEOUT
            errorMessage = 'Location request timed out';
            break;
          default:
            errorMessage = `Unknown error: ${error.message}`;
        }
        reject(new Error(errorMessage));
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 30000,
      }
    );
  });
};

/**
 * Send location update to backend
 * @param {number} latitude
 * @param {number} longitude
 * @param {number} accuracy
 * @param {number|null} batteryLevel
 * @returns {Promise<Object>}
 */
export const sendLocationUpdate = async (latitude, longitude, accuracy, batteryLevel = null) => {
  const user = getCurrentUser();
  const headers = {
    ...getAuthHeaders(),
    'X-Technician-Id': user.id,
  };

  const body = {
    latitude,
    longitude,
    accuracy,
  };

  if (batteryLevel !== null) {
    body.batteryLevel = batteryLevel;
  }

  const response = await fetch(`${API_BASE_URL}/api/technicians/me/location`, {
    method: 'POST',
    headers,
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to update location');
  }

  lastUpdateTime = new Date();
  return response.json();
};

/**
 * Check if battery is too low to continue tracking
 * @param {number|null} batteryLevel
 * @returns {boolean}
 */
export const isBatteryTooLow = (batteryLevel) => {
  if (batteryLevel === null) {
    return false; // If we can't determine battery level, continue tracking
  }
  return batteryLevel < LOW_BATTERY_THRESHOLD;
};

/**
 * Notify user about low battery
 */
export const notifyLowBattery = () => {
  try {
    if (typeof window !== 'undefined' && 'Notification' in window && window.Notification && window.Notification.permission === 'granted') {
      new window.Notification('Location Tracking Paused', {
        body: `Battery level is below ${LOW_BATTERY_THRESHOLD}%. Location tracking has been paused to save battery.`,
        icon: '/icon.png',
      });
    }
  } catch (error) {
    // Silently fail if notifications are not supported
    console.debug('Notification not available:', error);
  }
};

/**
 * Perform a single location update
 * @returns {Promise<Object>}
 */
export const performLocationUpdate = async () => {
  try {
    // Get current position
    const position = await getCurrentPosition();

    // Check if accuracy is acceptable
    if (position.accuracy > MIN_ACCURACY) {
      console.warn(`Location accuracy too low: ${position.accuracy}m`);
      // Still send the update, but log the warning
    }

    // Get battery level
    const batteryLevel = await getBatteryLevel();

    // Check if battery is too low
    if (isBatteryTooLow(batteryLevel)) {
      await pauseLocationTracking();
      notifyLowBattery();
      throw new Error(`Battery level too low (${batteryLevel}%). Tracking paused.`);
    }

    // Send location update to backend
    const result = await sendLocationUpdate(
      position.latitude,
      position.longitude,
      position.accuracy,
      batteryLevel
    );

    return {
      success: true,
      position,
      batteryLevel,
      result,
    };
  } catch (error) {
    console.error('Location update failed:', error);
    throw error;
  }
};

/**
 * Start location tracking
 * @returns {Promise<void>}
 */
export const startLocationTracking = async () => {
  // Request permission first
  await requestLocationPermission();

  if (isTracking) {
    console.warn('Location tracking is already running');
    return;
  }

  isTracking = true;
  isPaused = false;

  // Perform initial update
  try {
    await performLocationUpdate();
  } catch (error) {
    console.error('Initial location update failed:', error);
  }

  // Set up interval for periodic updates
  trackingInterval = setInterval(async () => {
    if (!isPaused) {
      try {
        await performLocationUpdate();
      } catch (error) {
        console.error('Periodic location update failed:', error);
      }
    }
  }, LOCATION_UPDATE_INTERVAL);
};

/**
 * Stop location tracking
 */
export const stopLocationTracking = () => {
  if (trackingInterval) {
    clearInterval(trackingInterval);
    trackingInterval = null;
  }
  isTracking = false;
  isPaused = false;
  lastUpdateTime = null;
};

/**
 * Pause location tracking (keeps interval running but skips updates)
 * @returns {Promise<void>}
 */
export const pauseLocationTracking = async () => {
  if (!isTracking) {
    throw new Error('Location tracking is not active');
  }
  isPaused = true;
};

/**
 * Resume location tracking
 * @returns {Promise<void>}
 */
export const resumeLocationTracking = async () => {
  if (!isTracking) {
    throw new Error('Location tracking is not active');
  }
  
  isPaused = false;

  // Perform immediate update on resume
  try {
    await performLocationUpdate();
  } catch (error) {
    console.error('Resume location update failed:', error);
  }
};

/**
 * Get tracking status
 * @returns {{isTracking: boolean, isPaused: boolean, lastUpdateTime: Date|null}}
 */
export const getTrackingStatus = () => {
  return {
    isTracking,
    isPaused,
    lastUpdateTime,
  };
};

/**
 * Configure tracking interval (for testing purposes)
 */
export const setTrackingInterval = () => {
  if (isTracking) {
    throw new Error('Cannot change interval while tracking is active');
  }
  // This function is primarily for testing
  // In production, use LOCATION_UPDATE_INTERVAL constant
};
