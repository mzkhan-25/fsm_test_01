import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginPage from './LoginPage';

// Mock window.location
delete window.location;
window.location = { href: '' };

// Mock fetch
global.fetch = vi.fn();

describe('LoginPage', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();
    // Reset fetch mock
    fetch.mockReset();
    // Reset window.location
    window.location.href = '';
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Component Rendering', () => {
    it('renders the login form', () => {
      render(<LoginPage />);
      
      expect(screen.getByText('Field Service Management')).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: 'Sign In' })).toBeInTheDocument();
      expect(screen.getByLabelText('Email Address')).toBeInTheDocument();
      expect(screen.getByLabelText('Password')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
    });

    it('renders input fields with correct attributes', () => {
      render(<LoginPage />);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      expect(emailInput).toHaveAttribute('type', 'email');
      expect(emailInput).toHaveAttribute('placeholder', 'Enter your email');
      expect(passwordInput).toHaveAttribute('type', 'password');
      expect(passwordInput).toHaveAttribute('placeholder', 'Enter your password');
    });
  });

  describe('Form Validation', () => {
    it('displays error when email is empty', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(await screen.findByText('Email is required')).toBeInTheDocument();
    });

    it('displays error when password is empty', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(await screen.findByText('Password is required')).toBeInTheDocument();
    });

    it('displays error when email format is invalid', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);
      
      const emailInput = screen.getByLabelText('Email Address');
      await user.type(emailInput, 'invalid-email');
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(await screen.findByText('Email must be valid')).toBeInTheDocument();
    });

    it('clears email error when user starts typing', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(await screen.findByText('Email is required')).toBeInTheDocument();
      
      const emailInput = screen.getByLabelText('Email Address');
      await user.type(emailInput, 't');
      
      expect(screen.queryByText('Email is required')).not.toBeInTheDocument();
    });

    it('clears password error when user starts typing', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(await screen.findByText('Password is required')).toBeInTheDocument();
      
      const passwordInput = screen.getByLabelText('Password');
      await user.type(passwordInput, 'p');
      
      expect(screen.queryByText('Password is required')).not.toBeInTheDocument();
    });

    it('does not submit form when validation fails', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(fetch).not.toHaveBeenCalled();
    });
  });

  describe('API Integration', () => {
    it('calls login API with correct data on successful validation', async () => {
      const user = userEvent.setup();
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          token: 'test-token',
          userId: 1,
          name: 'Test User',
          email: 'test@example.com',
          role: 'DISPATCHER',
        }),
      });

      render(<LoginPage />);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      await user.type(emailInput, 'test@example.com');
      await user.type(passwordInput, 'password123');
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/auth/login',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: 'test@example.com',
            password: 'password123',
            mobile: false,
          }),
        }
      );
    });

    it('stores JWT token in localStorage on successful login', async () => {
      const user = userEvent.setup();
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          token: 'test-token-123',
          userId: 1,
          name: 'Test User',
          email: 'test@example.com',
          role: 'DISPATCHER',
        }),
      });

      render(<LoginPage />);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      await user.type(emailInput, 'test@example.com');
      await user.type(passwordInput, 'password123');
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      await waitFor(() => {
        expect(localStorage.getItem('token')).toBe('test-token-123');
        expect(localStorage.getItem('userId')).toBe('1');
        expect(localStorage.getItem('userName')).toBe('Test User');
        expect(localStorage.getItem('userEmail')).toBe('test@example.com');
        expect(localStorage.getItem('userRole')).toBe('DISPATCHER');
      });
    });

    it('displays error message on login failure', async () => {
      const user = userEvent.setup();
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({
          message: 'Invalid credentials',
        }),
      });

      render(<LoginPage />);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      await user.type(emailInput, 'test@example.com');
      await user.type(passwordInput, 'wrongpassword');
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(await screen.findByText('Invalid credentials')).toBeInTheDocument();
    });

    it('displays generic error message when API error has no message', async () => {
      const user = userEvent.setup();
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({}),
      });

      render(<LoginPage />);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      await user.type(emailInput, 'test@example.com');
      await user.type(passwordInput, 'password123');
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(await screen.findByText('Invalid credentials')).toBeInTheDocument();
    });

    it('handles network errors gracefully', async () => {
      const user = userEvent.setup();
      fetch.mockRejectedValueOnce(new Error('Network error'));

      render(<LoginPage />);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      await user.type(emailInput, 'test@example.com');
      await user.type(passwordInput, 'password123');
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(await screen.findByText('Network error')).toBeInTheDocument();
    });
  });

  describe('Role-based Redirects', () => {
    const testRoleRedirect = async (role, expectedPath) => {
      const user = userEvent.setup();
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          token: 'test-token',
          userId: 1,
          name: 'Test User',
          email: 'test@example.com',
          role,
        }),
      });

      render(<LoginPage />);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      await user.type(emailInput, 'test@example.com');
      await user.type(passwordInput, 'password123');
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      await waitFor(() => {
        expect(window.location.href).toBe(expectedPath);
      });
    };

    it('redirects ADMIN to admin dashboard', async () => {
      await testRoleRedirect('ADMIN', '/admin/dashboard');
    });

    it('redirects DISPATCHER to dispatcher dashboard', async () => {
      await testRoleRedirect('DISPATCHER', '/dispatcher/dashboard');
    });

    it('redirects SUPERVISOR to supervisor dashboard', async () => {
      await testRoleRedirect('SUPERVISOR', '/supervisor/dashboard');
    });

    it('redirects TECHNICIAN to technician dashboard', async () => {
      await testRoleRedirect('TECHNICIAN', '/technician/dashboard');
    });

    it('redirects unknown role to default dashboard', async () => {
      await testRoleRedirect('UNKNOWN_ROLE', '/dashboard');
    });
  });

  describe('Loading State', () => {
    it('disables submit button while loading', async () => {
      const user = userEvent.setup();
      // Mock a delayed response
      fetch.mockImplementationOnce(() => 
        new Promise(resolve => setTimeout(() => resolve({
          ok: true,
          json: async () => ({
            token: 'test-token',
            userId: 1,
            name: 'Test User',
            email: 'test@example.com',
            role: 'DISPATCHER',
          }),
        }), 100))
      );

      render(<LoginPage />);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      await user.type(emailInput, 'test@example.com');
      await user.type(passwordInput, 'password123');
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      expect(submitButton).toBeDisabled();
      expect(submitButton).toHaveTextContent('Signing in...');
      
      await waitFor(() => {
        expect(submitButton).not.toBeDisabled();
      });
    });
  });

  describe('Accessibility', () => {
    it('marks invalid fields with aria-invalid', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      await waitFor(() => {
        expect(emailInput).toHaveAttribute('aria-invalid', 'true');
        expect(passwordInput).toHaveAttribute('aria-invalid', 'true');
      });
    });

    it('provides aria-describedby for error messages', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      const emailInput = screen.getByLabelText('Email Address');
      const passwordInput = screen.getByLabelText('Password');
      
      await waitFor(() => {
        expect(emailInput).toHaveAttribute('aria-describedby', 'email-error');
        expect(passwordInput).toHaveAttribute('aria-describedby', 'password-error');
      });
    });

    it('uses role="alert" for error messages', async () => {
      const user = userEvent.setup();
      render(<LoginPage />);
      
      const submitButton = screen.getByRole('button', { name: /sign in/i });
      await user.click(submitButton);
      
      const emailError = await screen.findByText('Email is required');
      const passwordError = await screen.findByText('Password is required');
      
      expect(emailError).toHaveAttribute('role', 'alert');
      expect(passwordError).toHaveAttribute('role', 'alert');
    });
  });
});
