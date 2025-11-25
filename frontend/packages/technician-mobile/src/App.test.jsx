import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import App from './App';
import * as authService from './services/authService';
import * as taskService from './services/taskService';

vi.mock('./services/authService', () => ({
  isAuthenticated: vi.fn(),
  getCurrentUser: vi.fn(),
  clearAuth: vi.fn(),
  login: vi.fn(),
}));

vi.mock('./services/taskService', () => ({
  getAssignedTasks: vi.fn().mockResolvedValue([]),
  getTaskById: vi.fn(),
  updateTaskStatus: vi.fn(),
  updateLocation: vi.fn().mockResolvedValue({}),
}));

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should render login page when not authenticated', () => {
    authService.isAuthenticated.mockReturnValue(false);

    render(<App />);

    expect(screen.getByText('FSM Technician')).toBeInTheDocument();
    expect(screen.getByLabelText('Email Address')).toBeInTheDocument();
  });

  it('should render main app when authenticated', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John Doe',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('My Tasks')).toBeInTheDocument();
    });
  });

  it('should render app header when authenticated', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getAllByText('FSM Technician')[0]).toBeInTheDocument();
      expect(screen.getByText('Online')).toBeInTheDocument();
    });
  });

  it('should render navigation tabs when authenticated', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByLabelText('Tasks')).toBeInTheDocument();
      expect(screen.getByLabelText('Map')).toBeInTheDocument();
      expect(screen.getByLabelText('Profile')).toBeInTheDocument();
    });
  });

  it('should switch to map view when map tab is clicked', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('My Tasks')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByLabelText('Map'));

    await waitFor(() => {
      expect(screen.getByText('Map integration coming soon')).toBeInTheDocument();
    });
  });

  it('should switch to profile view when profile tab is clicked', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('My Tasks')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByLabelText('Profile'));

    await waitFor(() => {
      expect(screen.getByText('Account Information')).toBeInTheDocument();
    });
  });

  it('should logout and show login page', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<App />);

    // Navigate to profile
    fireEvent.click(screen.getByLabelText('Profile'));

    await waitFor(() => {
      expect(
        screen.getByRole('button', { name: 'Logout from account' })
      ).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: 'Logout from account' }));

    await waitFor(() => {
      expect(screen.getByLabelText('Email Address')).toBeInTheDocument();
    });
  });

  it('should login and show main app', async () => {
    authService.isAuthenticated.mockReturnValue(false);
    authService.login.mockResolvedValue({
      token: 'token',
      role: 'TECHNICIAN',
      name: 'John',
    });

    render(<App />);

    fireEvent.change(screen.getByLabelText('Email Address'), {
      target: { value: 'john@example.com' },
    });
    fireEvent.change(screen.getByLabelText('Password'), {
      target: { value: 'password' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Sign In' }));

    await waitFor(() => {
      expect(screen.getByText('My Tasks')).toBeInTheDocument();
    });
  });

  it('should show offline status when offline', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    // Mock navigator.onLine
    Object.defineProperty(window.navigator, 'onLine', {
      value: false,
      writable: true,
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('Offline')).toBeInTheDocument();
    });

    // Restore
    Object.defineProperty(window.navigator, 'onLine', {
      value: true,
      writable: true,
    });
  });

  it('should handle online event', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    Object.defineProperty(window.navigator, 'onLine', {
      value: false,
      writable: true,
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('Offline')).toBeInTheDocument();
    });

    // Simulate online event
    Object.defineProperty(window.navigator, 'onLine', {
      value: true,
      writable: true,
    });
    window.dispatchEvent(new Event('online'));

    await waitFor(() => {
      expect(screen.getByText('Online')).toBeInTheDocument();
    });
  });

  it('should handle offline event', async () => {
    authService.isAuthenticated.mockReturnValue(true);
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('Online')).toBeInTheDocument();
    });

    // Simulate offline event
    window.dispatchEvent(new Event('offline'));

    await waitFor(() => {
      expect(screen.getByText('Offline')).toBeInTheDocument();
    });
  });

  describe('Task Detail Navigation', () => {
    const mockTask = {
      id: '1',
      title: 'Fix HVAC System',
      description: 'Replace the air filter',
      clientAddress: '123 Main St',
      priority: 'HIGH',
      status: 'ASSIGNED',
      estimatedDuration: 60,
    };

    beforeEach(() => {
      authService.isAuthenticated.mockReturnValue(true);
      authService.getCurrentUser.mockReturnValue({
        id: '123',
        name: 'John',
        email: 'john@example.com',
        role: 'TECHNICIAN',
      });
      taskService.getAssignedTasks.mockResolvedValue([mockTask]);
      taskService.getTaskById.mockResolvedValue(mockTask);
    });

    it('should navigate to task detail when task card is clicked', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('Fix HVAC System'));

      await waitFor(() => {
        expect(screen.getByText('← Back')).toBeInTheDocument();
        expect(screen.getByText('Description')).toBeInTheDocument();
      });
    });

    it('should navigate back to task list when back button is clicked', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('Fix HVAC System'));

      await waitFor(() => {
        expect(screen.getByText('← Back')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByRole('button', { name: 'Go back to task list' }));

      await waitFor(() => {
        expect(screen.getByText('My Tasks')).toBeInTheDocument();
      });
    });

    it('should clear selected task when switching tabs', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('Fix HVAC System'));

      await waitFor(() => {
        expect(screen.getByText('← Back')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByLabelText('Map'));

      await waitFor(() => {
        expect(screen.getByText('Map integration coming soon')).toBeInTheDocument();
      });

      // Navigate back to tasks should show list, not detail
      fireEvent.click(screen.getByLabelText('Tasks'));

      await waitFor(() => {
        expect(screen.getByText('My Tasks')).toBeInTheDocument();
      });
    });

    it('should clear selected task on logout', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('Fix HVAC System'));

      await waitFor(() => {
        expect(screen.getByText('← Back')).toBeInTheDocument();
      });

      // Navigate to profile and logout
      await act(async () => {
        fireEvent.click(screen.getByLabelText('Profile'));
      });

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Logout from account' })).toBeInTheDocument();
      });

      fireEvent.click(screen.getByRole('button', { name: 'Logout from account' }));

      await waitFor(() => {
        expect(screen.getByLabelText('Email Address')).toBeInTheDocument();
      });
    });
  });
});
