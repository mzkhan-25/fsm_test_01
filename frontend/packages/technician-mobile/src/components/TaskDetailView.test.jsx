import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import TaskDetailView from './TaskDetailView';
import * as taskService from '../services/taskService';
import * as navigationUtils from '../utils/navigationUtils';

vi.mock('../services/taskService', () => ({
  getTaskById: vi.fn(),
  updateTaskStatus: vi.fn(),
  completeTask: vi.fn(),
}));

vi.mock('../utils/navigationUtils', () => ({
  openNavigation: vi.fn(),
  extractCoordinatesFromAddress: vi.fn(),
}));

describe('TaskDetailView', () => {
  const mockOnBack = vi.fn();
  const mockOnStatusUpdate = vi.fn();
  const mockTask = {
    id: '1',
    title: 'Fix HVAC System',
    description: 'Replace the air filter and check refrigerant levels',
    clientAddress: '123 Main St, City, State 12345',
    priority: 'HIGH',
    status: 'ASSIGNED',
    estimatedDuration: 90,
    specialNotes: 'Access code: 1234. Dog in backyard.',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    navigationUtils.openNavigation.mockReturnValue(true);
    navigationUtils.extractCoordinatesFromAddress.mockReturnValue(null);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Loading State', () => {
    it('should show loading message initially', () => {
      taskService.getTaskById.mockImplementation(() => new Promise(() => {}));

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      expect(screen.getByText('Loading task details...')).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('should show error message when fetch fails', async () => {
      taskService.getTaskById.mockRejectedValue(new Error('Failed to load task details'));

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load task details')).toBeInTheDocument();
      });
    });

    it('should show back button when error occurs', async () => {
      taskService.getTaskById.mockRejectedValue(new Error('Failed'));

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Go back to task list' })).toBeInTheDocument();
      });
    });

    it('should show default error message when error has no message', async () => {
      taskService.getTaskById.mockRejectedValue(new Error());

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Failed to load task details')).toBeInTheDocument();
      });
    });
  });

  describe('Task Not Found', () => {
    it('should show not found message when task is null', async () => {
      taskService.getTaskById.mockResolvedValue(null);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Task not found.')).toBeInTheDocument();
      });
    });
  });

  describe('Task Display', () => {
    it('should display task title', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });
    });

    it('should display task description', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Replace the air filter and check refrigerant levels')).toBeInTheDocument();
      });
    });

    it('should display client address', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('123 Main St, City, State 12345')).toBeInTheDocument();
      });
    });

    it('should display priority badge with correct class', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        const priorityBadge = screen.getByText('HIGH');
        expect(priorityBadge).toHaveClass('high');
      });
    });

    it('should display status badge with correct class', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        const statusBadge = screen.getByText('ASSIGNED');
        expect(statusBadge).toHaveClass('assigned');
      });
    });

    it('should display estimated duration', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('1h 30min')).toBeInTheDocument();
      });
    });

    it('should display special notes', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Access code: 1234. Dog in backyard.')).toBeInTheDocument();
      });
    });

    it('should not display special notes section when not provided', async () => {
      const taskWithoutNotes = { ...mockTask, specialNotes: null };
      taskService.getTaskById.mockResolvedValue(taskWithoutNotes);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
        expect(screen.queryByText('Special Notes')).not.toBeInTheDocument();
      });
    });

    it('should display fallback address when clientAddress not provided', async () => {
      const taskWithAddress = { ...mockTask, clientAddress: null, address: '456 Oak Ave' };
      taskService.getTaskById.mockResolvedValue(taskWithAddress);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('456 Oak Ave')).toBeInTheDocument();
      });
    });

    it('should display no description message when not provided', async () => {
      const taskNoDescription = { ...mockTask, description: null };
      taskService.getTaskById.mockResolvedValue(taskNoDescription);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('No description provided.')).toBeInTheDocument();
      });
    });

    it('should display no address message when not provided', async () => {
      const taskNoAddress = { ...mockTask, clientAddress: null, address: null };
      taskService.getTaskById.mockResolvedValue(taskNoAddress);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('No address provided.')).toBeInTheDocument();
      });
    });

    it('should display not specified for duration when not provided', async () => {
      const taskNoDuration = { ...mockTask, estimatedDuration: null };
      taskService.getTaskById.mockResolvedValue(taskNoDuration);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Not specified')).toBeInTheDocument();
      });
    });
  });

  describe('Priority Classes', () => {
    it('should apply medium priority class', async () => {
      taskService.getTaskById.mockResolvedValue({ ...mockTask, priority: 'MEDIUM' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('MEDIUM')).toHaveClass('medium');
      });
    });

    it('should apply low priority class', async () => {
      taskService.getTaskById.mockResolvedValue({ ...mockTask, priority: 'LOW' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('LOW')).toHaveClass('low');
      });
    });

    it('should handle unknown priority gracefully', async () => {
      taskService.getTaskById.mockResolvedValue({ ...mockTask, priority: 'UNKNOWN' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('UNKNOWN')).toBeInTheDocument();
      });
    });
  });

  describe('Status Classes', () => {
    it('should apply in-progress status class', async () => {
      taskService.getTaskById.mockResolvedValue({ ...mockTask, status: 'IN_PROGRESS' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('IN PROGRESS')).toHaveClass('in-progress');
      });
    });

    it('should apply completed status class', async () => {
      taskService.getTaskById.mockResolvedValue({ ...mockTask, status: 'COMPLETED' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('COMPLETED')).toHaveClass('completed');
      });
    });

    it('should handle unknown status gracefully', async () => {
      taskService.getTaskById.mockResolvedValue({ ...mockTask, status: 'PENDING' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('PENDING')).toBeInTheDocument();
      });
    });
  });

  describe('Duration Formatting', () => {
    it('should format duration less than 60 minutes', async () => {
      taskService.getTaskById.mockResolvedValue({ ...mockTask, estimatedDuration: 45 });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('45 min')).toBeInTheDocument();
      });
    });

    it('should format exact hours', async () => {
      taskService.getTaskById.mockResolvedValue({ ...mockTask, estimatedDuration: 120 });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('2h')).toBeInTheDocument();
      });
    });
  });

  describe('Back Button', () => {
    it('should render back button', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Go back to task list' })).toBeInTheDocument();
      });
    });

    it('should call onBack when back button is clicked', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByRole('button', { name: 'Go back to task list' }));

      expect(mockOnBack).toHaveBeenCalledTimes(1);
    });
  });

  describe('Action Buttons - ASSIGNED Status', () => {
    it('should show Start Navigation button for assigned task', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Start navigation to client address' })).toBeInTheDocument();
      });
    });

    it('should show Mark In Progress button for assigned task', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Mark task as in progress' })).toBeInTheDocument();
      });
    });

    it('should not show Mark Completed button for assigned task', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
        expect(screen.queryByRole('button', { name: 'Mark task as completed' })).not.toBeInTheDocument();
      });
    });
  });

  describe('Action Buttons - IN_PROGRESS Status', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };

    it('should show Start Navigation button for in progress task', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Start navigation to client address' })).toBeInTheDocument();
      });
    });

    it('should not show Mark In Progress button for in progress task', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
        expect(screen.queryByRole('button', { name: 'Mark task as in progress' })).not.toBeInTheDocument();
      });
    });

    it('should show Mark Completed button for in progress task', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Mark task as completed' })).toBeInTheDocument();
      });
    });
  });

  describe('Action Buttons - COMPLETED Status', () => {
    const completedTask = { ...mockTask, status: 'COMPLETED' };

    it('should not show any action buttons for completed task', async () => {
      taskService.getTaskById.mockResolvedValue(completedTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
        expect(screen.queryByRole('button', { name: 'Start navigation to client address' })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: 'Mark task as in progress' })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: 'Mark task as completed' })).not.toBeInTheDocument();
      });
    });
  });

  describe('Start Navigation', () => {
    it('should call openNavigation with address when clicked', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      navigationUtils.extractCoordinatesFromAddress.mockReturnValue(null);
      navigationUtils.openNavigation.mockReturnValue(true);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByRole('button', { name: 'Start navigation to client address' }));

      expect(navigationUtils.extractCoordinatesFromAddress).toHaveBeenCalledWith('123 Main St, City, State 12345');
      expect(navigationUtils.openNavigation).toHaveBeenCalledWith({
        address: '123 Main St, City, State 12345',
        latitude: undefined,
        longitude: undefined,
      });
    });

    it('should use fallback address for navigation', async () => {
      const taskWithFallback = { ...mockTask, clientAddress: null, address: '789 Elm St' };
      taskService.getTaskById.mockResolvedValue(taskWithFallback);
      navigationUtils.extractCoordinatesFromAddress.mockReturnValue(null);
      navigationUtils.openNavigation.mockReturnValue(true);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByRole('button', { name: 'Start navigation to client address' }));

      expect(navigationUtils.openNavigation).toHaveBeenCalledWith({
        address: '789 Elm St',
        latitude: undefined,
        longitude: undefined,
      });
    });

    it('should extract and use coordinates from address if available', async () => {
      const taskWithCoords = { ...mockTask, clientAddress: '123 Main St (40.7128, -74.0060)' };
      taskService.getTaskById.mockResolvedValue(taskWithCoords);
      navigationUtils.extractCoordinatesFromAddress.mockReturnValue({
        latitude: 40.7128,
        longitude: -74.0060,
      });
      navigationUtils.openNavigation.mockReturnValue(true);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByRole('button', { name: 'Start navigation to client address' }));

      expect(navigationUtils.extractCoordinatesFromAddress).toHaveBeenCalledWith('123 Main St (40.7128, -74.0060)');
      expect(navigationUtils.openNavigation).toHaveBeenCalledWith({
        address: '123 Main St (40.7128, -74.0060)',
        latitude: 40.7128,
        longitude: -74.0060,
      });
    });

    it('should show error when no address is available', async () => {
      const taskNoAddress = { ...mockTask, clientAddress: null, address: null };
      taskService.getTaskById.mockResolvedValue(taskNoAddress);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByRole('button', { name: 'Start navigation to client address' }));

      await waitFor(() => {
        expect(screen.getByText('Cannot start navigation: No address available')).toBeInTheDocument();
      });
      expect(navigationUtils.openNavigation).not.toHaveBeenCalled();
    });

    it('should show error when openNavigation fails', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      navigationUtils.extractCoordinatesFromAddress.mockReturnValue(null);
      navigationUtils.openNavigation.mockReturnValue(false);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByRole('button', { name: 'Start navigation to client address' }));

      await waitFor(() => {
        expect(screen.getByText('Unable to open map application. Please check if a map app is installed.')).toBeInTheDocument();
      });
    });
  });

  describe('Mark In Progress', () => {
    it('should call updateTaskStatus when clicked', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      taskService.updateTaskStatus.mockResolvedValue({ ...mockTask, status: 'IN_PROGRESS' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as in progress' }));
      });

      await waitFor(() => {
        expect(taskService.updateTaskStatus).toHaveBeenCalledWith('1', 'IN_PROGRESS');
      });
    });

    it('should update UI after marking in progress', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      taskService.updateTaskStatus.mockResolvedValue({ ...mockTask, status: 'IN_PROGRESS' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as in progress' }));
      });

      await waitFor(() => {
        expect(screen.getByText('IN PROGRESS')).toBeInTheDocument();
      });
    });

    it('should show success message after marking in progress', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      taskService.updateTaskStatus.mockResolvedValue({ ...mockTask, status: 'IN_PROGRESS' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as in progress' }));
      });

      await waitFor(() => {
        expect(screen.getByText('Task marked as in progress successfully!')).toBeInTheDocument();
      });
    });

    it('should call onStatusUpdate callback when provided', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      taskService.updateTaskStatus.mockResolvedValue({ ...mockTask, status: 'IN_PROGRESS' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} onStatusUpdate={mockOnStatusUpdate} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as in progress' }));
      });

      await waitFor(() => {
        expect(mockOnStatusUpdate).toHaveBeenCalledWith('1', 'IN_PROGRESS');
      });
    });

    it('should show error message when update fails', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      taskService.updateTaskStatus.mockRejectedValue(new Error('Update failed'));

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as in progress' }));
      });

      await waitFor(() => {
        expect(screen.getByText('Update failed')).toBeInTheDocument();
      });
    });

    it('should show default error message when update fails without message', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      taskService.updateTaskStatus.mockRejectedValue(new Error());

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as in progress' }));
      });

      await waitFor(() => {
        expect(screen.getByText('Failed to update task status')).toBeInTheDocument();
      });
    });

    it('should show updating state while request is in progress', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      let resolveUpdate;
      taskService.updateTaskStatus.mockImplementation(() => new Promise(resolve => {
        resolveUpdate = resolve;
      }));

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as in progress' }));
      });

      expect(screen.getByText('Updating...')).toBeInTheDocument();

      await act(async () => {
        resolveUpdate({ ...mockTask, status: 'IN_PROGRESS' });
      });
    });

    it('should clear error message before showing success', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);
      taskService.updateTaskStatus
        .mockRejectedValueOnce(new Error('First attempt failed'))
        .mockResolvedValueOnce({ ...mockTask, status: 'IN_PROGRESS' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      // First attempt - should fail
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as in progress' }));
      });

      await waitFor(() => {
        expect(screen.getByText('First attempt failed')).toBeInTheDocument();
      });

      // Second attempt - should succeed
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as in progress' }));
      });

      await waitFor(() => {
        expect(screen.queryByText('First attempt failed')).not.toBeInTheDocument();
        expect(screen.getByText('Task marked as in progress successfully!')).toBeInTheDocument();
      });
    });
  });

  describe('Mark Completed', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' };

    it('should open completion modal when button clicked', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as completed' }));
      });

      await waitFor(() => {
        expect(screen.getByText('Complete Task')).toBeInTheDocument();
      });
    });

    it('should call completeTask with work summary when modal submitted', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);
      taskService.completeTask.mockResolvedValue({ ...inProgressTask, status: 'COMPLETED' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      // Open modal
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as completed' }));
      });

      await waitFor(() => {
        expect(screen.getByText('Complete Task')).toBeInTheDocument();
      });

      // Fill in work summary
      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Replaced air filter and checked refrigerant levels successfully' } });

      // Submit form
      const form = textarea.closest('form');
      
      await act(async () => {
        fireEvent.submit(form);
      });

      await waitFor(() => {
        expect(taskService.completeTask).toHaveBeenCalledWith('1', 'Replaced air filter and checked refrigerant levels successfully');
      });
    });

    it('should update UI after completing task', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);
      taskService.completeTask.mockResolvedValue({ ...inProgressTask, status: 'COMPLETED' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      // Open modal
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as completed' }));
      });

      // Fill in work summary and submit
      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Work completed successfully' } });

      const form = textarea.closest('form');
      await act(async () => {
        fireEvent.submit(form);
      });

      await waitFor(() => {
        expect(screen.getByText('COMPLETED')).toBeInTheDocument();
      });
    });

    it('should show success message after completing task', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);
      taskService.completeTask.mockResolvedValue({ ...inProgressTask, status: 'COMPLETED' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      // Open modal
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as completed' }));
      });

      // Fill in work summary and submit
      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Work completed successfully' } });

      const form = textarea.closest('form');
      await act(async () => {
        fireEvent.submit(form);
      });

      await waitFor(() => {
        expect(screen.getByText('Task completed successfully!')).toBeInTheDocument();
      });
    });

    it('should call onStatusUpdate callback when provided', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);
      taskService.completeTask.mockResolvedValue({ ...inProgressTask, status: 'COMPLETED' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} onStatusUpdate={mockOnStatusUpdate} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      // Open modal
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as completed' }));
      });

      // Fill in work summary and submit
      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Work completed successfully' } });

      const form = textarea.closest('form');
      await act(async () => {
        fireEvent.submit(form);
      });

      await waitFor(() => {
        expect(mockOnStatusUpdate).toHaveBeenCalledWith('1', 'COMPLETED');
      });
    });

    it('should show error message when completion fails', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);
      taskService.completeTask.mockRejectedValue(new Error('Completion failed'));

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      // Open modal
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as completed' }));
      });

      // Fill in work summary and submit
      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Work completed successfully' } });

      const form = textarea.closest('form');
      await act(async () => {
        fireEvent.submit(form);
      });

      await waitFor(() => {
        expect(screen.getByText('Completion failed')).toBeInTheDocument();
      });
    });

    it('should close modal and navigate back after successful completion', async () => {
      vi.useFakeTimers();
      taskService.getTaskById.mockResolvedValue(inProgressTask);
      taskService.completeTask.mockResolvedValue({ ...inProgressTask, status: 'COMPLETED' });

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      // Open modal
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as completed' }));
      });

      // Fill in work summary and submit
      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Work completed successfully' } });

      const form = textarea.closest('form');
      await act(async () => {
        fireEvent.submit(form);
      });

      await waitFor(() => {
        expect(screen.getByText('Task completed successfully!')).toBeInTheDocument();
      });

      // Fast-forward time to trigger navigation
      await act(async () => {
        vi.advanceTimersByTime(2000);
      });

      expect(mockOnBack).toHaveBeenCalledTimes(1);

      vi.useRealTimers();
    });

    it('should close modal when completion fails', async () => {
      taskService.getTaskById.mockResolvedValue(inProgressTask);
      taskService.completeTask.mockRejectedValue(new Error('Completion failed'));

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      });

      // Open modal
      await act(async () => {
        fireEvent.click(screen.getByRole('button', { name: 'Mark task as completed' }));
      });

      expect(screen.getByText('Complete Task')).toBeInTheDocument();

      // Fill in work summary and submit
      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Work completed successfully' } });

      const form = textarea.closest('form');
      await act(async () => {
        fireEvent.submit(form);
      });

      await waitFor(() => {
        expect(screen.getByText('Completion failed')).toBeInTheDocument();
      });

      // Modal should be closed
      expect(screen.queryByText('Work Summary *')).not.toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels for action buttons', async () => {
      taskService.getTaskById.mockResolvedValue(mockTask);

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByLabelText('Go back to task list')).toBeInTheDocument();
        expect(screen.getByLabelText('Start navigation to client address')).toBeInTheDocument();
        expect(screen.getByLabelText('Mark task as in progress')).toBeInTheDocument();
      });
    });

    it('should have proper role for loading status', () => {
      taskService.getTaskById.mockImplementation(() => new Promise(() => {}));

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      expect(screen.getByRole('status')).toBeInTheDocument();
    });

    it('should have proper role for error message', async () => {
      taskService.getTaskById.mockRejectedValue(new Error('Failed'));

      render(<TaskDetailView taskId="1" onBack={mockOnBack} />);

      await waitFor(() => {
        expect(screen.getByRole('alert')).toBeInTheDocument();
      });
    });
  });
});
