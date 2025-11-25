import { useState, useEffect, useCallback } from 'react';
import { updateLocation } from '../services/taskService';
import './MapView.css';

const MapView = () => {
  const [locationEnabled, setLocationEnabled] = useState(false);
  const [locationError, setLocationError] = useState(() => {
    // Initialize with geolocation support status
    if (typeof navigator !== 'undefined' && !('geolocation' in navigator)) {
      return 'Geolocation is not supported';
    }
    return null;
  });

  const requestLocation = useCallback(() => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLocationEnabled(true);
          // Send initial location to backend
          updateLocation(position.coords.latitude, position.coords.longitude)
            .catch(() => {
              // Silently handle location update errors
            });
        },
        (error) => {
          setLocationError(error.message);
          setLocationEnabled(false);
        }
      );
    }
  }, []);

  useEffect(() => {
    if (!locationError) {
      requestLocation();
    }
  }, [locationError, requestLocation]);

  return (
    <div className="map-view">
      <header className="map-header">
        <h1 className="map-title">Map</h1>
      </header>

      <div className="map-container" role="region" aria-label="Map view">
        <div className="map-placeholder">
          <div className="map-placeholder-icon" aria-hidden="true">🗺️</div>
          <p className="map-placeholder-text">Map integration coming soon</p>
          <p className="map-placeholder-subtext">
            Navigate to task locations with integrated maps
          </p>
        </div>

        <div className={`location-status ${locationEnabled ? 'active' : 'inactive'}`}>
          {locationEnabled ? (
            <span>📍 Location tracking active</span>
          ) : locationError ? (
            <span>⚠️ {locationError}</span>
          ) : (
            <span>📍 Requesting location access...</span>
          )}
        </div>
      </div>
    </div>
  );
};

export default MapView;
