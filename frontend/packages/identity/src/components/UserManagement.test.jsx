import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import UserManagement from './UserManagement';
import * as userApi from '../services/userApi';

// Mock the userApi module
vi.mock('../services/userApi');

describe('UserManagement', () => {
  const mockUsers = [
    {
      id: 1,
      name: 'Admin User',
      email: 'admin@example.com',
      phone: '+1234567890',
      role: 'ADMIN',
      status: 'ACTIVE',
    },
    {
      id: 2,
      name: 'Tech User',
      email: 'tech@example.com',
      phone: '+9876543210',
      role: 'TECHNICIAN',
      status: 'ACTIVE',
    },
    {
      id: 3,
      name: 'Inactive User',
      email: 'inactive@example.com',
      phone: null,
      role: 'DISPATCHER',
      status: 'INACTIVE',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    userApi.getAllUsers.mockResolvedValue(mockUsers);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Component Rendering', () => {
    it('renders the user management page', async () => {
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('User Management')).toBeInTheDocument();
      });
      expect(screen.getByRole('button', { name: 'Add User' })).toBeInTheDocument();
    });

    it('displays loading state initially', () => {
      render(<UserManagement />);
      expect(screen.getByText('Loading users...')).toBeInTheDocument();
    });

    it('displays user list after loading', async () => {
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });
      expect(screen.getByText('Tech User')).toBeInTheDocument();
      expect(screen.getByText('Inactive User')).toBeInTheDocument();
    });

    it('displays all table columns', async () => {
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Name')).toBeInTheDocument();
      });
      expect(screen.getByText('Email')).toBeInTheDocument();
      expect(screen.getByText('Phone')).toBeInTheDocument();
      expect(screen.getByText('Role')).toBeInTheDocument();
      expect(screen.getByText('Status')).toBeInTheDocument();
      expect(screen.getByText('Actions')).toBeInTheDocument();
    });

    it('displays user data in table', async () => {
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('admin@example.com')).toBeInTheDocument();
      });
      expect(screen.getByText('+1234567890')).toBeInTheDocument();
      expect(screen.getByText('tech@example.com')).toBeInTheDocument();
    });

    it('displays "-" for missing phone numbers', async () => {
      render(<UserManagement />);

      await waitFor(() => {
        const rows = screen.getAllByRole('row');
        const inactiveRow = rows.find(row => row.textContent.includes('Inactive User'));
        expect(within(inactiveRow).getByText('-')).toBeInTheDocument();
      });
    });

    it('displays status badges', async () => {
      render(<UserManagement />);

      await waitFor(() => {
        const statusBadges = screen.getAllByText(/ACTIVE|INACTIVE/);
        expect(statusBadges.length).toBeGreaterThan(0);
      });
    });

    it('shows "No users found" when list is empty', async () => {
      userApi.getAllUsers.mockResolvedValue([]);
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('No users found')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('displays error message when loading fails', async () => {
      userApi.getAllUsers.mockRejectedValue(new Error('Failed to load users'));
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load users')).toBeInTheDocument();
      });
    });

    it('displays error with custom message', async () => {
      userApi.getAllUsers.mockRejectedValue(new Error('Network error'));
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Network error')).toBeInTheDocument();
      });
    });
  });

  describe('Add User Functionality', () => {
    it('opens add user modal when clicking Add User button', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      const addButton = screen.getByRole('button', { name: 'Add User' });
      await user.click(addButton);

      expect(screen.getByRole('heading', { name: 'Add User' })).toBeInTheDocument();
      expect(screen.getByLabelText(/Name/)).toBeInTheDocument();
      expect(screen.getByLabelText(/Email/)).toBeInTheDocument();
    });

    it('creates user successfully', async () => {
      const user = userEvent.setup();
      const newUser = {
        id: 4,
        name: 'New User',
        email: 'new@example.com',
        phone: '+1111111111',
        role: 'TECHNICIAN',
        status: 'ACTIVE',
      };
      userApi.createUser.mockResolvedValue(newUser);
      userApi.getAllUsers.mockResolvedValueOnce(mockUsers).mockResolvedValueOnce([...mockUsers, newUser]);

      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Add User' }));

      await user.type(screen.getByLabelText(/Name/), 'New User');
      await user.type(screen.getByLabelText(/Email/), 'new@example.com');
      await user.type(screen.getByLabelText(/Phone/), '+1111111111');
      await user.type(screen.getByLabelText(/Password/), 'password123');

      await user.click(screen.getByRole('button', { name: 'Create User' }));

      await waitFor(() => {
        expect(userApi.createUser).toHaveBeenCalledWith({
          name: 'New User',
          email: 'new@example.com',
          phone: '+1111111111',
          password: 'password123',
          role: 'TECHNICIAN',
        });
      });

      await waitFor(() => {
        expect(screen.getByText('User created successfully')).toBeInTheDocument();
      });
    });

    it('validates required fields', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Add User' }));
      await user.click(screen.getByRole('button', { name: 'Create User' }));

      expect(await screen.findByText('Name is required')).toBeInTheDocument();
      expect(await screen.findByText('Email is required')).toBeInTheDocument();
      expect(await screen.findByText('Password is required')).toBeInTheDocument();
      expect(userApi.createUser).not.toHaveBeenCalled();
    });

    it('validates phone format', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Add User' }));
      await user.type(screen.getByLabelText(/Phone/), 'invalid-phone');
      await user.click(screen.getByRole('button', { name: 'Create User' }));

      expect(await screen.findByText(/Phone number must be in E.164 format/)).toBeInTheDocument();
    });

    it('closes modal on cancel', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Add User' }));
      
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: 'Add User' })).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Cancel' }));
      
      await waitFor(() => {
        expect(screen.queryByRole('heading', { name: 'Add User' })).not.toBeInTheDocument();
      });
    });

    it('displays error when creation fails', async () => {
      const user = userEvent.setup();
      userApi.createUser.mockRejectedValue(new Error('Email already exists'));

      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Add User' }));
      await user.type(screen.getByLabelText(/Name/), 'New User');
      await user.type(screen.getByLabelText(/Email/), 'existing@example.com');
      await user.type(screen.getByLabelText(/Password/), 'password123');
      await user.click(screen.getByRole('button', { name: 'Create User' }));

      await waitFor(() => {
        expect(screen.getByText('Email already exists')).toBeInTheDocument();
      });
    });
  });

  describe('Edit User Functionality', () => {
    it('opens edit modal with user data', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      const editButtons = screen.getAllByRole('button', { name: 'Edit' });
      await user.click(editButtons[0]);

      expect(screen.getByRole('heading', { name: 'Edit User' })).toBeInTheDocument();
      expect(screen.getByDisplayValue('Admin User')).toBeInTheDocument();
      expect(screen.getByDisplayValue('admin@example.com')).toBeInTheDocument();
    });

    it('updates user successfully', async () => {
      const user = userEvent.setup();
      const updatedUser = {
        ...mockUsers[0],
        name: 'Updated Admin',
      };
      userApi.updateUser.mockResolvedValue(updatedUser);

      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      const editButtons = screen.getAllByRole('button', { name: 'Edit' });
      await user.click(editButtons[0]);

      const nameInput = screen.getByDisplayValue('Admin User');
      await user.clear(nameInput);
      await user.type(nameInput, 'Updated Admin');

      await user.click(screen.getByRole('button', { name: 'Update User' }));

      await waitFor(() => {
        expect(userApi.updateUser).toHaveBeenCalledWith(1, {
          name: 'Updated Admin',
          email: 'admin@example.com',
          phone: '+1234567890',
          role: 'ADMIN',
        });
      });

      await waitFor(() => {
        expect(screen.getByText('User updated successfully')).toBeInTheDocument();
      });
    });

    it('does not require password for update', async () => {
      const user = userEvent.setup();
      const updatedUser = mockUsers[0];
      userApi.updateUser.mockResolvedValue(updatedUser);

      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      const editButtons = screen.getAllByRole('button', { name: 'Edit' });
      await user.click(editButtons[0]);

      await user.click(screen.getByRole('button', { name: 'Update User' }));

      await waitFor(() => {
        expect(userApi.updateUser).toHaveBeenCalled();
      });

      const callArgs = userApi.updateUser.mock.calls[0][1];
      expect(callArgs).not.toHaveProperty('password');
    });

    it('includes password in update if provided', async () => {
      const user = userEvent.setup();
      const updatedUser = mockUsers[0];
      userApi.updateUser.mockResolvedValue(updatedUser);

      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      const editButtons = screen.getAllByRole('button', { name: 'Edit' });
      await user.click(editButtons[0]);

      await user.type(screen.getByLabelText(/Password/), 'newpassword123');
      await user.click(screen.getByRole('button', { name: 'Update User' }));

      await waitFor(() => {
        expect(userApi.updateUser).toHaveBeenCalled();
      });

      const callArgs = userApi.updateUser.mock.calls[0][1];
      expect(callArgs).toHaveProperty('password', 'newpassword123');
    });
  });

  describe('Deactivate User Functionality', () => {
    it('shows confirmation dialog when clicking deactivate', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      const deactivateButtons = screen.getAllByRole('button', { name: 'Deactivate' });
      await user.click(deactivateButtons[0]);

      expect(screen.getByText('Confirm Deactivation')).toBeInTheDocument();
      expect(screen.getByText(/Are you sure you want to deactivate user/)).toBeInTheDocument();
    });

    it('deactivates user on confirmation', async () => {
      const user = userEvent.setup();
      userApi.deactivateUser.mockResolvedValue(true);

      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      const deactivateButtons = screen.getAllByRole('button', { name: 'Deactivate' });
      await user.click(deactivateButtons[0]);

      await waitFor(() => {
        expect(screen.getByRole('heading', { name: 'Confirm Deactivation' })).toBeInTheDocument();
      });

      const confirmButtons = screen.getAllByRole('button', { name: 'Deactivate' });
      const confirmButton = confirmButtons[confirmButtons.length - 1];
      await user.click(confirmButton);

      await waitFor(() => {
        expect(userApi.deactivateUser).toHaveBeenCalledWith(1);
      });

      await waitFor(() => {
        expect(screen.getByText(/deactivated successfully/)).toBeInTheDocument();
      });
    });

    it('closes dialog on cancel', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      const deactivateButtons = screen.getAllByRole('button', { name: 'Deactivate' });
      await user.click(deactivateButtons[0]);

      expect(screen.getByText('Confirm Deactivation')).toBeInTheDocument();

      await user.click(screen.getAllByRole('button', { name: 'Cancel' })[0]);

      await waitFor(() => {
        expect(screen.queryByText('Confirm Deactivation')).not.toBeInTheDocument();
      });
    });

    it('does not show deactivate button for inactive users', async () => {
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Inactive User')).toBeInTheDocument();
      });

      const rows = screen.getAllByRole('row');
      const inactiveRow = rows.find(row => row.textContent.includes('Inactive User'));
      
      expect(within(inactiveRow).queryByRole('button', { name: 'Deactivate' })).not.toBeInTheDocument();
    });

    it('displays error when deactivation fails', async () => {
      const user = userEvent.setup();
      userApi.deactivateUser.mockRejectedValue(new Error('Cannot deactivate user'));

      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      const deactivateButtons = screen.getAllByRole('button', { name: 'Deactivate' });
      await user.click(deactivateButtons[0]);

      await waitFor(() => {
        expect(screen.getByRole('heading', { name: 'Confirm Deactivation' })).toBeInTheDocument();
      });

      const confirmButtons = screen.getAllByRole('button', { name: 'Deactivate' });
      const confirmButton = confirmButtons[confirmButtons.length - 1];
      await user.click(confirmButton);

      await waitFor(() => {
        expect(screen.getByText('Cannot deactivate user')).toBeInTheDocument();
      });
    });
  });

  describe('Form Field Clearing', () => {
    it('clears field errors when user types', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Add User' }));
      await user.click(screen.getByRole('button', { name: 'Create User' }));

      expect(await screen.findByText('Name is required')).toBeInTheDocument();

      await user.type(screen.getByLabelText(/Name/), 'T');

      expect(screen.queryByText('Name is required')).not.toBeInTheDocument();
    });
  });

  describe('Role Selection', () => {
    it('allows selecting different roles', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Add User' }));

      const roleSelect = screen.getByLabelText(/Role/);
      expect(roleSelect).toHaveValue('TECHNICIAN');

      await user.selectOptions(roleSelect, 'ADMIN');
      expect(roleSelect).toHaveValue('ADMIN');

      await user.selectOptions(roleSelect, 'SUPERVISOR');
      expect(roleSelect).toHaveValue('SUPERVISOR');
    });
  });

  describe('Accessibility', () => {
    it('marks invalid fields with aria-invalid', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Add User' }));
      await user.click(screen.getByRole('button', { name: 'Create User' }));

      await waitFor(() => {
        expect(screen.getByLabelText(/Name/)).toHaveAttribute('aria-invalid', 'true');
        expect(screen.getByLabelText(/Email/)).toHaveAttribute('aria-invalid', 'true');
      });
    });

    it('provides aria-describedby for error messages', async () => {
      const user = userEvent.setup();
      render(<UserManagement />);

      await waitFor(() => {
        expect(screen.getByText('Admin User')).toBeInTheDocument();
      });

      await user.click(screen.getByRole('button', { name: 'Add User' }));
      await user.click(screen.getByRole('button', { name: 'Create User' }));

      await waitFor(() => {
        expect(screen.getByLabelText(/Name/)).toHaveAttribute('aria-describedby', 'name-error');
      });
    });

    it('uses role="alert" for error and success messages', async () => {
      userApi.getAllUsers.mockRejectedValue(new Error('Failed to load'));
      render(<UserManagement />);

      await waitFor(() => {
        const errorMessage = screen.getByText('Failed to load');
        expect(errorMessage).toHaveAttribute('role', 'alert');
      });
    });
  });
});
