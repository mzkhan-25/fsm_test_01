import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ProfileView from './ProfileView';
import * as authService from '../services/authService';
import * as locationService from '../services/locationService';

vi.mock('../services/authService', () => ({
  getCurrentUser: vi.fn(),
  clearAuth: vi.fn(),
}));

vi.mock('../services/locationService', () => ({
  getTrackingStatus: vi.fn(),
  startLocationTracking: vi.fn(),
  stopLocationTracking: vi.fn(),
  pauseLocationTracking: vi.fn(),
  resumeLocationTracking: vi.fn(),
  requestLocationPermission: vi.fn(),
}));

describe('ProfileView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    
    // Default location tracking status
    locationService.getTrackingStatus.mockReturnValue({
      isTracking: false,
      isPaused: false,
      lastUpdateTime: null,
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
    vi.clearAllTimers();
    vi.useRealTimers();
  });

  it('should render user profile', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John Doe',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getAllByText('TECHNICIAN')).toHaveLength(2); // Header and info section
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
    expect(screen.getByText('123')).toBeInTheDocument();
  });

  it('should display user initials in avatar', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John Doe',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('JD')).toBeInTheDocument();
  });

  it('should display single initial for single name', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('J')).toBeInTheDocument();
  });

  it('should display ? for missing name', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: null,
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('?')).toBeInTheDocument();
  });

  it('should display fallback values for missing data', () => {
    authService.getCurrentUser.mockReturnValue({
      id: null,
      name: null,
      email: null,
      role: null,
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('Technician')).toBeInTheDocument();
    expect(screen.getByText('Field Technician')).toBeInTheDocument();
    expect(screen.getAllByText('Not available')).toHaveLength(3);
  });

  it('should render account information section', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('Account Information')).toBeInTheDocument();
    expect(screen.getByText('Email')).toBeInTheDocument();
    expect(screen.getByText('User ID')).toBeInTheDocument();
    expect(screen.getByText('Role')).toBeInTheDocument();
  });

  it('should render location tracking section', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('Location Tracking')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Disabled')).toBeInTheDocument();
  });

  it('should render app settings section', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('App Settings')).toBeInTheDocument();
    expect(screen.getByText('Notifications')).toBeInTheDocument();
  });

  it('should render logout button', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(
      screen.getByRole('button', { name: 'Logout from account' })
    ).toBeInTheDocument();
  });

  it('should call clearAuth, stopLocationTracking, and onLogout when logout clicked', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    const mockOnLogout = vi.fn();
    render(<ProfileView onLogout={mockOnLogout} />);

    fireEvent.click(screen.getByRole('button', { name: 'Logout from account' }));

    expect(locationService.stopLocationTracking).toHaveBeenCalled();
    expect(authService.clearAuth).toHaveBeenCalled();
    expect(mockOnLogout).toHaveBeenCalled();
  });

  it('should handle logout without onLogout callback', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView />);

    fireEvent.click(screen.getByRole('button', { name: 'Logout from account' }));

    expect(locationService.stopLocationTracking).toHaveBeenCalled();
    expect(authService.clearAuth).toHaveBeenCalled();
  });

  it('should display version info', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('FSM Technician Mobile v1.0.0')).toBeInTheDocument();
  });

  it('should limit initials to 2 characters', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John Doe Smith',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('JD')).toBeInTheDocument();
  });

  describe('Location Tracking', () => {
    beforeEach(() => {
      authService.getCurrentUser.mockReturnValue({
        id: '123',
        name: 'John Doe',
        email: 'john@example.com',
        role: 'TECHNICIAN',
      });
      // Use real timers for these tests to avoid timeout issues
      vi.useRealTimers();
    });

    afterEach(() => {
      // Restore fake timers after each test
      vi.useFakeTimers();
    });

    it('should show disabled status when tracking is not active', () => {
      locationService.getTrackingStatus.mockReturnValue({
        isTracking: false,
        isPaused: false,
        lastUpdateTime: null,
      });

      render(<ProfileView onLogout={() => {}} />);

      expect(screen.getByText('Disabled')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Start location tracking' })).toBeInTheDocument();
    });

    it('should show active status when tracking is active', () => {
      locationService.getTrackingStatus.mockReturnValue({
        isTracking: true,
        isPaused: false,
        lastUpdateTime: new Date(),
      });

      render(<ProfileView onLogout={() => {}} />);

      expect(screen.getByText('Active')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Stop location tracking' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Pause location tracking' })).toBeInTheDocument();
    });

    it('should show paused status when tracking is paused', () => {
      locationService.getTrackingStatus.mockReturnValue({
        isTracking: true,
        isPaused: true,
        lastUpdateTime: new Date(),
      });

      render(<ProfileView onLogout={() => {}} />);

      expect(screen.getByText('Paused')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Resume location tracking' })).toBeInTheDocument();
    });

    it('should start tracking when start button is clicked', async () => {
      locationService.startLocationTracking.mockResolvedValue();
      
      render(<ProfileView onLogout={() => {}} />);

      const startButton = screen.getByRole('button', { name: 'Start location tracking' });
      fireEvent.click(startButton);

      await waitFor(() => {
        expect(locationService.startLocationTracking).toHaveBeenCalled();
      });
    });

    it('should stop tracking when stop button is clicked', async () => {
      locationService.stopLocationTracking.mockImplementation(() => {});
      locationService.getTrackingStatus.mockReturnValue({
        isTracking: true,
        isPaused: false,
        lastUpdateTime: new Date(),
      });

      render(<ProfileView onLogout={() => {}} />);

      const stopButton = screen.getByRole('button', { name: 'Stop location tracking' });
      fireEvent.click(stopButton);

      await waitFor(() => {
        expect(locationService.stopLocationTracking).toHaveBeenCalled();
      });
    });

    it('should pause tracking when pause button is clicked', async () => {
      locationService.pauseLocationTracking.mockResolvedValue();
      locationService.getTrackingStatus.mockReturnValue({
        isTracking: true,
        isPaused: false,
        lastUpdateTime: new Date(),
      });

      render(<ProfileView onLogout={() => {}} />);

      const pauseButton = screen.getByRole('button', { name: 'Pause location tracking' });
      fireEvent.click(pauseButton);

      await waitFor(() => {
        expect(locationService.pauseLocationTracking).toHaveBeenCalled();
      });
    });

    it('should resume tracking when resume button is clicked', async () => {
      locationService.resumeLocationTracking.mockResolvedValue();
      locationService.getTrackingStatus.mockReturnValue({
        isTracking: true,
        isPaused: true,
        lastUpdateTime: new Date(),
      });

      render(<ProfileView onLogout={() => {}} />);

      const resumeButton = screen.getByRole('button', { name: 'Resume location tracking' });
      fireEvent.click(resumeButton);

      await waitFor(() => {
        expect(locationService.resumeLocationTracking).toHaveBeenCalled();
      });
    });

    it('should display error when start tracking fails', async () => {
      locationService.startLocationTracking.mockRejectedValue(
        new Error('Location permission denied')
      );

      render(<ProfileView onLogout={() => {}} />);

      const startButton = screen.getByRole('button', { name: 'Start location tracking' });
      fireEvent.click(startButton);

      await waitFor(() => {
        expect(screen.getByText('Location permission denied')).toBeInTheDocument();
      });
    });

    it('should disable buttons while operation is in progress', async () => {
      locationService.startLocationTracking.mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100))
      );

      render(<ProfileView onLogout={() => {}} />);

      const startButton = screen.getByRole('button', { name: 'Start location tracking' });
      fireEvent.click(startButton);

      expect(screen.getByText('Processing...')).toBeInTheDocument();
      expect(startButton).toBeDisabled();
    });

    it('should display last update time when tracking is active', () => {
      const lastUpdate = new Date(Date.now() - 30 * 1000); // 30 seconds ago
      locationService.getTrackingStatus.mockReturnValue({
        isTracking: true,
        isPaused: false,
        lastUpdateTime: lastUpdate,
      });

      render(<ProfileView onLogout={() => {}} />);

      expect(screen.getByText('Last Update')).toBeInTheDocument();
      expect(screen.getByText('30 seconds ago')).toBeInTheDocument();
    });

    it('should display last update time in minutes', () => {
      const lastUpdate = new Date(Date.now() - 5 * 60 * 1000); // 5 minutes ago
      locationService.getTrackingStatus.mockReturnValue({
        isTracking: true,
        isPaused: false,
        lastUpdateTime: lastUpdate,
      });

      render(<ProfileView onLogout={() => {}} />);

      expect(screen.getByText('5 minutes ago')).toBeInTheDocument();
    });

    it('should update tracking status periodically', () => {
      // Use fake timers for this specific test
      vi.useFakeTimers();
      
      locationService.getTrackingStatus.mockReturnValue({
        isTracking: false,
        isPaused: false,
        lastUpdateTime: null,
      });

      render(<ProfileView onLogout={() => {}} />);

      // Clear the calls made during mount
      const initialCalls = locationService.getTrackingStatus.mock.calls.length;

      // Advance timers by 2 seconds (polling interval)
      vi.advanceTimersByTime(2000);

      // Should have called at least once more
      expect(locationService.getTrackingStatus.mock.calls.length).toBeGreaterThan(initialCalls);
      
      vi.useRealTimers();
    });

    it('should display information text about tracking', () => {
      render(<ProfileView onLogout={() => {}} />);

      expect(
        screen.getByText(/Location updates are sent every 2-5 minutes/)
      ).toBeInTheDocument();
      expect(
        screen.getByText(/automatically pauses when battery is below 15%/)
      ).toBeInTheDocument();
    });
  });
});
