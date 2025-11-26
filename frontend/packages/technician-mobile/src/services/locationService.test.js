import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  requestLocationPermission,
  getBatteryLevel,
  getCurrentPosition,
  sendLocationUpdate,
  isBatteryTooLow,
  notifyLowBattery,
  performLocationUpdate,
  startLocationTracking,
  stopLocationTracking,
  pauseLocationTracking,
  resumeLocationTracking,
  getTrackingStatus,
} from './locationService';

// Mock the authService
vi.mock('./authService', () => ({
  getAuthHeaders: () => ({
    'Content-Type': 'application/json',
    Authorization: 'Bearer test-token',
  }),
  getCurrentUser: () => ({
    id: '123',
    name: 'Test User',
    email: 'test@example.com',
    role: 'technician',
  }),
}));

describe('locationService', () => {
  let mockGeolocation;
  let mockFetch;
  let mockBattery;
  let mockNotification;

  beforeEach(() => {
    // Reset state
    stopLocationTracking();

    // Mock geolocation
    mockGeolocation = {
      getCurrentPosition: vi.fn(),
      watchPosition: vi.fn(),
      clearWatch: vi.fn(),
    };
    global.navigator.geolocation = mockGeolocation;

    // Mock permissions API
    global.navigator.permissions = {
      query: vi.fn(),
    };

    // Mock battery API
    mockBattery = {
      level: 0.5,
      charging: false,
      addEventListener: vi.fn(),
    };
    global.navigator.getBattery = vi.fn().mockResolvedValue(mockBattery);

    // Mock fetch
    mockFetch = vi.fn();
    global.fetch = mockFetch;

    // Mock Notification
    mockNotification = vi.fn();
    global.Notification = mockNotification;
    mockNotification.permission = 'granted';

    // Mock timers
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.clearAllTimers();
    stopLocationTracking();
  });

  describe('requestLocationPermission', () => {
    it('should return true if permission is already granted', async () => {
      navigator.permissions.query.mockResolvedValue({ state: 'granted' });

      const result = await requestLocationPermission();

      expect(result).toBe(true);
      expect(navigator.permissions.query).toHaveBeenCalledWith({ name: 'geolocation' });
    });

    it('should throw error if permission is denied', async () => {
      navigator.permissions.query.mockResolvedValue({ state: 'denied' });

      await expect(requestLocationPermission()).rejects.toThrow('Location permission denied');
    });

    it('should request permission if state is prompt', async () => {
      navigator.permissions.query.mockResolvedValue({ state: 'prompt' });
      mockGeolocation.getCurrentPosition.mockImplementation((success) => {
        success({ coords: { latitude: 0, longitude: 0 } });
      });

      const result = await requestLocationPermission();

      expect(result).toBe(true);
      expect(mockGeolocation.getCurrentPosition).toHaveBeenCalled();
    });

    it('should throw error if geolocation is not supported', async () => {
      global.navigator.geolocation = undefined;

      await expect(requestLocationPermission()).rejects.toThrow('Geolocation is not supported');
    });

    it('should handle getCurrentPosition error', async () => {
      navigator.permissions.query.mockResolvedValue({ state: 'prompt' });
      mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
        error({ code: 1, message: 'Permission denied' });
      });

      await expect(requestLocationPermission()).rejects.toThrow('Failed to get location permission');
    });
  });

  describe('getBatteryLevel', () => {
    it('should return battery level as percentage', async () => {
      mockBattery.level = 0.75;

      const level = await getBatteryLevel();

      expect(level).toBe(75);
    });

    it('should return null if battery API is not available', async () => {
      global.navigator.getBattery = undefined;

      const level = await getBatteryLevel();

      expect(level).toBe(null);
    });

    it('should return null if battery API throws error', async () => {
      global.navigator.getBattery = vi.fn().mockRejectedValue(new Error('Not supported'));

      const level = await getBatteryLevel();

      expect(level).toBe(null);
    });

    it('should round battery level to nearest integer', async () => {
      mockBattery.level = 0.856;

      const level = await getBatteryLevel();

      expect(level).toBe(86);
    });
  });

  describe('getCurrentPosition', () => {
    it('should return current position', async () => {
      const mockPosition = {
        coords: {
          latitude: 40.7128,
          longitude: -74.006,
          accuracy: 10,
        },
      };

      mockGeolocation.getCurrentPosition.mockImplementation((success) => {
        success(mockPosition);
      });

      const position = await getCurrentPosition();

      expect(position).toEqual({
        latitude: 40.7128,
        longitude: -74.006,
        accuracy: 10,
      });
    });

    it('should reject if geolocation is not supported', async () => {
      global.navigator.geolocation = undefined;

      await expect(getCurrentPosition()).rejects.toThrow('Geolocation is not supported');
    });

    it('should handle PERMISSION_DENIED error', async () => {
      mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
        error({ code: 1, message: 'Permission denied' });
      });

      await expect(getCurrentPosition()).rejects.toThrow('Location permission denied');
    });

    it('should handle POSITION_UNAVAILABLE error', async () => {
      mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
        error({ code: 2, message: 'Position unavailable' });
      });

      await expect(getCurrentPosition()).rejects.toThrow('Location information unavailable');
    });

    it('should handle TIMEOUT error', async () => {
      mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
        error({ code: 3, message: 'Timeout' });
      });

      await expect(getCurrentPosition()).rejects.toThrow('Location request timed out');
    });

    it('should handle unknown error', async () => {
      mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
        error({ code: 99, message: 'Unknown error' });
      });

      await expect(getCurrentPosition()).rejects.toThrow('Unknown error');
    });
  });

  describe('sendLocationUpdate', () => {
    it('should send location update to backend', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({ success: true, locationId: '456' }),
      });

      const result = await sendLocationUpdate(40.7128, -74.006, 10, 75);

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/technicians/me/location',
        expect.objectContaining({
          method: 'POST',
          headers: expect.objectContaining({
            'X-Technician-Id': '123',
            Authorization: 'Bearer test-token',
          }),
          body: JSON.stringify({
            latitude: 40.7128,
            longitude: -74.006,
            accuracy: 10,
            batteryLevel: 75,
          }),
        })
      );
      expect(result).toEqual({ success: true, locationId: '456' });
    });

    it('should send location update without battery level', async () => {
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({ success: true }),
      });

      await sendLocationUpdate(40.7128, -74.006, 10);

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/technicians/me/location',
        expect.objectContaining({
          body: JSON.stringify({
            latitude: 40.7128,
            longitude: -74.006,
            accuracy: 10,
          }),
        })
      );
    });

    it('should throw error if request fails', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        json: async () => ({ message: 'Rate limit exceeded' }),
      });

      await expect(sendLocationUpdate(40.7128, -74.006, 10)).rejects.toThrow(
        'Rate limit exceeded'
      );
    });

    it('should throw generic error if no error message in response', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        json: async () => ({}),
      });

      await expect(sendLocationUpdate(40.7128, -74.006, 10)).rejects.toThrow(
        'Failed to update location'
      );
    });
  });

  describe('isBatteryTooLow', () => {
    it('should return true if battery is below threshold', () => {
      expect(isBatteryTooLow(10)).toBe(true);
      expect(isBatteryTooLow(14)).toBe(true);
    });

    it('should return false if battery is at or above threshold', () => {
      expect(isBatteryTooLow(15)).toBe(false);
      expect(isBatteryTooLow(50)).toBe(false);
      expect(isBatteryTooLow(100)).toBe(false);
    });

    it('should return false if battery level is null', () => {
      expect(isBatteryTooLow(null)).toBe(false);
    });
  });

  describe('notifyLowBattery', () => {
    it('should create notification if permission is granted', () => {
      mockNotification.permission = 'granted';

      notifyLowBattery();

      expect(mockNotification).toHaveBeenCalledWith(
        'Location Tracking Paused',
        expect.objectContaining({
          body: expect.stringContaining('Battery level is below 15%'),
        })
      );
    });

    it('should not create notification if permission is not granted', () => {
      mockNotification.permission = 'denied';

      notifyLowBattery();

      expect(mockNotification).not.toHaveBeenCalled();
    });

    it('should handle missing Notification API gracefully', () => {
      const originalNotification = global.Notification;
      global.Notification = undefined;

      // Call the function - it should not throw
      notifyLowBattery();

      // Restore
      global.Notification = originalNotification;
    });
  });

  describe('performLocationUpdate', () => {
    beforeEach(() => {
      navigator.permissions.query.mockResolvedValue({ state: 'granted' });

      mockGeolocation.getCurrentPosition.mockImplementation((success) => {
        success({
          coords: {
            latitude: 40.7128,
            longitude: -74.006,
            accuracy: 10,
          },
        });
      });

      mockBattery.level = 0.5;

      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({ success: true }),
      });
    });

    it('should perform complete location update', async () => {
      const result = await performLocationUpdate();

      expect(result.success).toBe(true);
      expect(result.position).toEqual({
        latitude: 40.7128,
        longitude: -74.006,
        accuracy: 10,
      });
      expect(result.batteryLevel).toBe(50);
    });

    it('should log warning for low accuracy but still send update', async () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
      mockGeolocation.getCurrentPosition.mockImplementation((success) => {
        success({
          coords: {
            latitude: 40.7128,
            longitude: -74.006,
            accuracy: 150, // Low accuracy
          },
        });
      });

      const result = await performLocationUpdate();

      expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining('accuracy too low'));
      expect(result.success).toBe(true);

      consoleSpy.mockRestore();
    });

    it('should pause tracking if battery is too low', async () => {
      mockBattery.level = 0.5; // Start with good battery
      
      // Start tracking first
      await startLocationTracking();
      
      // Now set battery low for the next update
      mockBattery.level = 0.1; // 10% - below threshold
      mockNotification.permission = 'granted';
      
      // Try to perform update with low battery
      await expect(performLocationUpdate()).rejects.toThrow('Battery level too low');

      const status = getTrackingStatus();
      expect(status.isPaused).toBe(true);
    });

    it('should throw error if getCurrentPosition fails', async () => {
      mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
        error({ code: 2, message: 'Position unavailable' });
      });

      await expect(performLocationUpdate()).rejects.toThrow('Location information unavailable');
    });

    it('should throw error if sendLocationUpdate fails', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        json: async () => ({ message: 'Server error' }),
      });

      await expect(performLocationUpdate()).rejects.toThrow('Server error');
    });
  });

  describe('startLocationTracking', () => {
    beforeEach(() => {
      navigator.permissions.query.mockResolvedValue({ state: 'granted' });

      mockGeolocation.getCurrentPosition.mockImplementation((success) => {
        success({
          coords: {
            latitude: 40.7128,
            longitude: -74.006,
            accuracy: 10,
          },
        });
      });

      mockBattery.level = 0.5;

      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({ success: true }),
      });
    });

    it('should start location tracking', async () => {
      await startLocationTracking();

      const status = getTrackingStatus();
      expect(status.isTracking).toBe(true);
      expect(status.isPaused).toBe(false);
    });

    it('should perform initial location update', async () => {
      await startLocationTracking();

      expect(mockFetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/technicians/me/location',
        expect.any(Object)
      );
    });

    it('should not start if already tracking', async () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

      await startLocationTracking();
      await startLocationTracking();

      expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining('already running'));

      consoleSpy.mockRestore();
    });

    it('should continue even if initial update fails', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Error' }),
      });

      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      await startLocationTracking();

      const status = getTrackingStatus();
      expect(status.isTracking).toBe(true);
      expect(consoleSpy).toHaveBeenCalledWith(
        expect.stringContaining('Initial location update failed'),
        expect.any(Error)
      );

      consoleSpy.mockRestore();
    });

    it('should set up periodic updates', async () => {
      await startLocationTracking();

      // Clear the initial fetch call
      mockFetch.mockClear();

      // Advance time by 2 minutes
      await vi.advanceTimersByTimeAsync(2 * 60 * 1000);

      expect(mockFetch).toHaveBeenCalled();
    });

    it('should not update when paused', async () => {
      await startLocationTracking();
      await pauseLocationTracking();

      mockFetch.mockClear();

      // Advance time by 2 minutes
      await vi.advanceTimersByTimeAsync(2 * 60 * 1000);

      expect(mockFetch).not.toHaveBeenCalled();
    });
  });

  describe('stopLocationTracking', () => {
    beforeEach(async () => {
      navigator.permissions.query.mockResolvedValue({ state: 'granted' });

      mockGeolocation.getCurrentPosition.mockImplementation((success) => {
        success({
          coords: {
            latitude: 40.7128,
            longitude: -74.006,
            accuracy: 10,
          },
        });
      });

      mockBattery.level = 0.5;

      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({ success: true }),
      });
    });

    it('should stop location tracking', async () => {
      await startLocationTracking();
      stopLocationTracking();

      const status = getTrackingStatus();
      expect(status.isTracking).toBe(false);
      expect(status.isPaused).toBe(false);
    });

    it('should clear interval', async () => {
      await startLocationTracking();

      mockFetch.mockClear();

      stopLocationTracking();

      // Advance time - should not trigger any updates
      await vi.advanceTimersByTimeAsync(2 * 60 * 1000);

      expect(mockFetch).not.toHaveBeenCalled();
    });

    it('should reset lastUpdateTime', async () => {
      await startLocationTracking();

      const statusBefore = getTrackingStatus();
      expect(statusBefore.lastUpdateTime).not.toBeNull();

      stopLocationTracking();

      const statusAfter = getTrackingStatus();
      expect(statusAfter.lastUpdateTime).toBeNull();
    });
  });

  describe('pauseLocationTracking', () => {
    beforeEach(async () => {
      navigator.permissions.query.mockResolvedValue({ state: 'granted' });

      mockGeolocation.getCurrentPosition.mockImplementation((success) => {
        success({
          coords: {
            latitude: 40.7128,
            longitude: -74.006,
            accuracy: 10,
          },
        });
      });

      mockBattery.level = 0.5;

      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({ success: true }),
      });
    });

    it('should pause location tracking', async () => {
      await startLocationTracking();
      await pauseLocationTracking();

      const status = getTrackingStatus();
      expect(status.isTracking).toBe(true);
      expect(status.isPaused).toBe(true);
    });

    it('should throw error if not tracking', async () => {
      await expect(pauseLocationTracking()).rejects.toThrow('Location tracking is not active');
    });
  });

  describe('resumeLocationTracking', () => {
    beforeEach(async () => {
      navigator.permissions.query.mockResolvedValue({ state: 'granted' });

      mockGeolocation.getCurrentPosition.mockImplementation((success) => {
        success({
          coords: {
            latitude: 40.7128,
            longitude: -74.006,
            accuracy: 10,
          },
        });
      });

      mockBattery.level = 0.5;

      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({ success: true }),
      });
    });

    it('should resume location tracking', async () => {
      await startLocationTracking();
      await pauseLocationTracking();

      mockFetch.mockClear();

      await resumeLocationTracking();

      const status = getTrackingStatus();
      expect(status.isTracking).toBe(true);
      expect(status.isPaused).toBe(false);

      // Should have performed immediate update
      expect(mockFetch).toHaveBeenCalled();
    });

    it('should throw error if not tracking', async () => {
      await expect(resumeLocationTracking()).rejects.toThrow('Location tracking is not active');
    });

    it('should handle error during resume update', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      await startLocationTracking();
      await pauseLocationTracking();

      mockFetch.mockResolvedValue({
        ok: false,
        json: async () => ({ message: 'Error' }),
      });

      await resumeLocationTracking();

      expect(consoleSpy).toHaveBeenCalledWith(
        expect.stringContaining('Resume location update failed'),
        expect.any(Error)
      );

      consoleSpy.mockRestore();
    });
  });

  describe('getTrackingStatus', () => {
    it('should return default status when not tracking', () => {
      const status = getTrackingStatus();

      expect(status.isTracking).toBe(false);
      expect(status.isPaused).toBe(false);
      expect(status.lastUpdateTime).toBeNull();
    });

    it('should return correct status when tracking', async () => {
      navigator.permissions.query.mockResolvedValue({ state: 'granted' });

      mockGeolocation.getCurrentPosition.mockImplementation((success) => {
        success({
          coords: {
            latitude: 40.7128,
            longitude: -74.006,
            accuracy: 10,
          },
        });
      });

      mockBattery.level = 0.5;

      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => ({ success: true }),
      });

      await startLocationTracking();

      const status = getTrackingStatus();

      expect(status.isTracking).toBe(true);
      expect(status.isPaused).toBe(false);
      expect(status.lastUpdateTime).toBeInstanceOf(Date);
    });
  });
});
