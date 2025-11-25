import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import ProfileView from './ProfileView';
import * as authService from '../services/authService';

vi.mock('../services/authService', () => ({
  getCurrentUser: vi.fn(),
  clearAuth: vi.fn(),
}));

describe('ProfileView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
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

  it('should render app settings section', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    render(<ProfileView onLogout={() => {}} />);

    expect(screen.getByText('App Settings')).toBeInTheDocument();
    expect(screen.getByText('Location Tracking')).toBeInTheDocument();
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

  it('should call clearAuth and onLogout when logout clicked', () => {
    authService.getCurrentUser.mockReturnValue({
      id: '123',
      name: 'John',
      email: 'john@example.com',
      role: 'TECHNICIAN',
    });

    const mockOnLogout = vi.fn();
    render(<ProfileView onLogout={mockOnLogout} />);

    fireEvent.click(screen.getByRole('button', { name: 'Logout from account' }));

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
});
