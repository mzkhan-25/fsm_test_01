/**
 * Navigation utility functions for opening map applications on mobile devices.
 * Supports both iOS and Android platforms with graceful fallback.
 */

/**
 * Detects the user's platform (iOS, Android, or other)
 * @returns {string} 'ios', 'android', or 'other'
 */
export const detectPlatform = () => {
  const userAgent = navigator.userAgent || navigator.vendor || window.opera;
  
  if (/iPad|iPhone|iPod/.test(userAgent) && !window.MSStream) {
    return 'ios';
  }
  
  if (/android/i.test(userAgent)) {
    return 'android';
  }
  
  return 'other';
};

/**
 * Opens the device's native map application with the specified destination.
 * - On iOS: Opens Apple Maps
 * - On Android: Opens Google Maps
 * - On other platforms: Opens Google Maps in browser
 * 
 * @param {Object} options - Navigation options
 * @param {string} options.address - Destination address (required if coordinates not provided)
 * @param {number} options.latitude - Destination latitude (optional)
 * @param {number} options.longitude - Destination longitude (optional)
 * @returns {boolean} True if navigation was initiated successfully, false otherwise
 */
export const openNavigation = ({ address, latitude, longitude }) => {
  // Validate input - need either address or coordinates
  if (!address && (!latitude || !longitude)) {
    console.error('Navigation error: Either address or coordinates must be provided');
    return false;
  }

  const platform = detectPlatform();
  let navigationUrl;

  try {
    if (platform === 'ios') {
      // Apple Maps URL scheme
      if (latitude && longitude) {
        // Use coordinates if available
        navigationUrl = `maps://maps.apple.com/?daddr=${latitude},${longitude}`;
      } else {
        // Use address
        const encodedAddress = encodeURIComponent(address);
        navigationUrl = `maps://maps.apple.com/?daddr=${encodedAddress}`;
      }
    } else if (platform === 'android') {
      // Google Maps URL scheme for Android
      if (latitude && longitude) {
        navigationUrl = `google.navigation:q=${latitude},${longitude}`;
      } else {
        const encodedAddress = encodeURIComponent(address);
        navigationUrl = `google.navigation:q=${encodedAddress}`;
      }
    } else {
      // Fallback to Google Maps web for desktop/other platforms
      if (latitude && longitude) {
        navigationUrl = `https://www.google.com/maps/dir/?api=1&destination=${latitude},${longitude}`;
      } else {
        const encodedAddress = encodeURIComponent(address);
        navigationUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodedAddress}`;
      }
    }

    // Try to open the navigation URL
    window.open(navigationUrl, '_blank');
    return true;
  } catch (error) {
    console.error('Failed to open navigation:', error);
    
    // Fallback to Google Maps web if native app fails
    try {
      const encodedAddress = address ? encodeURIComponent(address) : `${latitude},${longitude}`;
      const fallbackUrl = `https://www.google.com/maps/dir/?api=1&destination=${encodedAddress}`;
      window.open(fallbackUrl, '_blank');
      return true;
    } catch (fallbackError) {
      console.error('Fallback navigation also failed:', fallbackError);
      return false;
    }
  }
};

/**
 * Extracts coordinates from an address string if it contains lat/long information.
 * Supports formats like:
 * - "Address (lat, lon)"
 * - "Address [lat, lon]"
 * - "Address {lat, lon}"
 * 
 * @param {string} address - Address string that might contain coordinates
 * @returns {Object|null} Object with latitude and longitude, or null if not found
 */
export const extractCoordinatesFromAddress = (address) => {
  if (!address || typeof address !== 'string') {
    return null;
  }

  // Try to match various coordinate patterns
  const patterns = [
    /\((-?\d+\.?\d*),\s*(-?\d+\.?\d*)\)/,  // (lat, lon)
    /\[(-?\d+\.?\d*),\s*(-?\d+\.?\d*)\]/,  // [lat, lon]
    /\{(-?\d+\.?\d*),\s*(-?\d+\.?\d*)\}/,  // {lat, lon}
  ];

  for (const pattern of patterns) {
    const match = address.match(pattern);
    if (match) {
      const latitude = parseFloat(match[1]);
      const longitude = parseFloat(match[2]);
      
      // Validate coordinate ranges
      if (latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180) {
        return { latitude, longitude };
      }
    }
  }

  return null;
};
