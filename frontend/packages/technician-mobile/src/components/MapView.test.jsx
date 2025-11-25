import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import MapView from './MapView';
import * as taskService from '../services/taskService';

vi.mock('../services/taskService', () => ({
  updateLocation: vi.fn(),
}));

describe('MapView', () => {
  const mockGeolocation = {
    getCurrentPosition: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    global.navigator.geolocation = mockGeolocation;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should render map header', () => {
    render(<MapView />);

    expect(screen.getByText('Map')).toBeInTheDocument();
  });

  it('should render map placeholder', () => {
    render(<MapView />);

    expect(screen.getByText('Map integration coming soon')).toBeInTheDocument();
    expect(
      screen.getByText('Navigate to task locations with integrated maps')
    ).toBeInTheDocument();
  });

  it('should show requesting location message initially when geolocation supported', () => {
    mockGeolocation.getCurrentPosition.mockImplementation(() => {});

    render(<MapView />);

    expect(
      screen.getByText('📍 Requesting location access...')
    ).toBeInTheDocument();
  });

  it('should show location active when geolocation succeeds', async () => {
    mockGeolocation.getCurrentPosition.mockImplementation((success) => {
      success({ coords: { latitude: 37.7749, longitude: -122.4194 } });
    });
    taskService.updateLocation.mockResolvedValue({ success: true });

    render(<MapView />);

    expect(screen.getByText('📍 Location tracking active')).toBeInTheDocument();
  });

  it('should call updateLocation when geolocation succeeds', () => {
    mockGeolocation.getCurrentPosition.mockImplementation((success) => {
      success({ coords: { latitude: 37.7749, longitude: -122.4194 } });
    });
    taskService.updateLocation.mockResolvedValue({ success: true });

    render(<MapView />);

    expect(taskService.updateLocation).toHaveBeenCalledWith(37.7749, -122.4194);
  });

  it('should handle location update failure silently', () => {
    mockGeolocation.getCurrentPosition.mockImplementation((success) => {
      success({ coords: { latitude: 37.7749, longitude: -122.4194 } });
    });
    taskService.updateLocation.mockRejectedValue(new Error('Update failed'));

    render(<MapView />);

    expect(screen.getByText('📍 Location tracking active')).toBeInTheDocument();
  });

  it('should show error when geolocation fails', () => {
    mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
      error({ message: 'User denied access' });
    });

    render(<MapView />);

    expect(screen.getByText('⚠️ User denied access')).toBeInTheDocument();
  });

  it('should show error when geolocation not supported', () => {
    delete global.navigator.geolocation;

    render(<MapView />);

    expect(
      screen.getByText('⚠️ Geolocation is not supported')
    ).toBeInTheDocument();
  });

  it('should render map view region', () => {
    render(<MapView />);

    expect(screen.getByRole('region', { name: 'Map view' })).toBeInTheDocument();
  });
});
