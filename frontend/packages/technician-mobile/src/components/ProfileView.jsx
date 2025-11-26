import { useState, useEffect } from 'react';
import { getCurrentUser, clearAuth } from '../services/authService';
import {
  startLocationTracking,
  stopLocationTracking,
  pauseLocationTracking,
  resumeLocationTracking,
  getTrackingStatus,
} from '../services/locationService';
import './ProfileView.css';

const ProfileView = ({ onLogout }) => {
  const user = getCurrentUser();
  const [locationStatus, setLocationStatus] = useState(getTrackingStatus());
  const [locationError, setLocationError] = useState(null);
  const [isChangingStatus, setIsChangingStatus] = useState(false);

  useEffect(() => {
    // Update status on mount
    setLocationStatus(getTrackingStatus());

    // Poll for status updates every 5 seconds to show current state
    const intervalId = setInterval(() => {
      setLocationStatus(getTrackingStatus());
    }, 5000);

    return () => clearInterval(intervalId);
  }, []);

  const getInitials = (name) => {
    if (!name) return '?';
    return name
      .split(' ')
      .map((part) => part[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const handleLogout = () => {
    // Stop location tracking on logout
    stopLocationTracking();
    clearAuth();
    if (onLogout) {
      onLogout();
    }
  };

  const handleLocationToggle = async () => {
    setIsChangingStatus(true);
    setLocationError(null);

    try {
      if (!locationStatus.isTracking) {
        // Start tracking
        await startLocationTracking();
        setLocationStatus(getTrackingStatus());
      } else {
        // Stop tracking
        stopLocationTracking();
        setLocationStatus(getTrackingStatus());
      }
    } catch (error) {
      setLocationError(error.message);
    } finally {
      setIsChangingStatus(false);
    }
  };

  const handleLocationPauseResume = async () => {
    if (!locationStatus.isTracking) {
      return;
    }

    setIsChangingStatus(true);
    setLocationError(null);

    try {
      if (locationStatus.isPaused) {
        await resumeLocationTracking();
      } else {
        await pauseLocationTracking();
      }
      setLocationStatus(getTrackingStatus());
    } catch (error) {
      setLocationError(error.message);
    } finally {
      setIsChangingStatus(false);
    }
  };

  const getLocationStatusText = () => {
    if (!locationStatus.isTracking) {
      return 'Disabled';
    }
    if (locationStatus.isPaused) {
      return 'Paused';
    }
    return 'Active';
  };

  const getLastUpdateText = () => {
    if (!locationStatus.lastUpdateTime) {
      return 'Never';
    }
    const now = new Date();
    const diff = Math.floor((now - locationStatus.lastUpdateTime) / 1000);
    
    if (diff < 60) {
      return `${diff} seconds ago`;
    } else if (diff < 3600) {
      return `${Math.floor(diff / 60)} minutes ago`;
    } else {
      return locationStatus.lastUpdateTime.toLocaleTimeString();
    }
  };

  return (
    <div className="profile-view">
      <header className="profile-header">
        <div className="profile-avatar" aria-hidden="true">
          {getInitials(user.name)}
        </div>
        <h1 className="profile-name">{user.name || 'Technician'}</h1>
        <p className="profile-role">{user.role || 'Field Technician'}</p>
      </header>

      <section className="profile-section" aria-labelledby="account-info-title">
        <h2 id="account-info-title" className="section-title">Account Information</h2>
        <div className="profile-info-item">
          <span className="info-label">Email</span>
          <span className="info-value">{user.email || 'Not available'}</span>
        </div>
        <div className="profile-info-item">
          <span className="info-label">User ID</span>
          <span className="info-value">{user.id || 'Not available'}</span>
        </div>
        <div className="profile-info-item">
          <span className="info-label">Role</span>
          <span className="info-value">{user.role || 'Not available'}</span>
        </div>
      </section>

      <section className="profile-section" aria-labelledby="location-tracking-title">
        <h2 id="location-tracking-title" className="section-title">Location Tracking</h2>
        
        {locationError && (
          <div className="location-error" role="alert">
            {locationError}
          </div>
        )}

        <div className="profile-info-item">
          <span className="info-label">Status</span>
          <span className="info-value">{getLocationStatusText()}</span>
        </div>

        {locationStatus.isTracking && (
          <div className="profile-info-item">
            <span className="info-label">Last Update</span>
            <span className="info-value">{getLastUpdateText()}</span>
          </div>
        )}

        <div className="location-controls">
          <button
            className="location-button"
            onClick={handleLocationToggle}
            disabled={isChangingStatus}
            aria-label={locationStatus.isTracking ? 'Stop location tracking' : 'Start location tracking'}
          >
            {isChangingStatus
              ? 'Processing...'
              : locationStatus.isTracking
              ? 'Stop Tracking'
              : 'Start Tracking'}
          </button>

          {locationStatus.isTracking && (
            <button
              className="location-button secondary"
              onClick={handleLocationPauseResume}
              disabled={isChangingStatus}
              aria-label={locationStatus.isPaused ? 'Resume location tracking' : 'Pause location tracking'}
            >
              {isChangingStatus
                ? 'Processing...'
                : locationStatus.isPaused
                ? 'Resume'
                : 'Pause'}
            </button>
          )}
        </div>

        <p className="location-info-text">
          Location updates are sent every 2-5 minutes when tracking is active.
          Tracking automatically pauses when battery is below 15%.
        </p>
      </section>

      <section className="profile-section" aria-labelledby="app-settings-title">
        <h2 id="app-settings-title" className="section-title">App Settings</h2>
        <div className="profile-info-item">
          <span className="info-label">Notifications</span>
          <span className="info-value">Enabled</span>
        </div>
      </section>

      <button
        className="logout-button"
        onClick={handleLogout}
        aria-label="Logout from account"
      >
        Logout
      </button>

      <p className="version-info">
        FSM Technician Mobile v1.0.0
      </p>
    </div>
  );
};

export default ProfileView;
