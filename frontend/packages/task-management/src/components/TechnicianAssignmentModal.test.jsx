import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TechnicianAssignmentModal from './TechnicianAssignmentModal';
import * as taskApi from '../services/taskApi';

// Mock the taskApi module
vi.mock('../services/taskApi', () => ({
  getTechnicians: vi.fn(),
  assignTask: vi.fn(),
}));

describe('TechnicianAssignmentModal', () => {
  const mockTask = {
    id: 1,
    title: 'Test Task',
  };

  const mockTechnicians = [
    { id: 1, name: 'John Doe', email: 'john@example.com', workload: 3, status: 'ACTIVE', role: 'TECHNICIAN' },
    { id: 2, name: 'Jane Smith', email: 'jane@example.com', workload: 7, status: 'ACTIVE', role: 'TECHNICIAN' },
    { id: 3, name: 'Bob Wilson', email: 'bob@example.com', workload: 12, status: 'ACTIVE', role: 'TECHNICIAN' },
  ];

  let mockOnClose;
  let mockOnAssignmentComplete;

  beforeEach(() => {
    mockOnClose = vi.fn();
    mockOnAssignmentComplete = vi.fn();
    vi.clearAllMocks();
    taskApi.getTechnicians.mockResolvedValue(mockTechnicians);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Component Rendering', () => {
    it('does not render when isOpen is false', () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={false}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('renders when isOpen is true', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      expect(screen.getByRole('dialog')).toBeInTheDocument();
      expect(screen.getByText('Assign Task')).toBeInTheDocument();
    });

    it('displays loading state initially', async () => {
      taskApi.getTechnicians.mockImplementation(() => new Promise(() => {}));

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      expect(screen.getByText('Loading technicians...')).toBeInTheDocument();
    });

    it('displays technician list after loading', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
        expect(screen.getByText('Bob Wilson')).toBeInTheDocument();
      });
    });

    it('sorts technicians by workload (least loaded first)', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        const listItems = screen.getAllByRole('option');
        expect(listItems).toHaveLength(3);
        expect(listItems[0]).toHaveTextContent('John Doe');
        expect(listItems[1]).toHaveTextContent('Jane Smith');
        expect(listItems[2]).toHaveTextContent('Bob Wilson');
      });
    });

    it('displays workload count for each technician', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('3')).toBeInTheDocument();
        expect(screen.getByText('7')).toBeInTheDocument();
        expect(screen.getByText('12')).toBeInTheDocument();
      });
    });

    it('displays warning icon for technicians at capacity (>=10 tasks)', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        const listItems = screen.getAllByRole('option');
        // Bob Wilson (12 tasks) should have warning
        expect(listItems[2]).toHaveTextContent('At capacity');
      });
    });

    it('displays empty state when no technicians available', async () => {
      taskApi.getTechnicians.mockResolvedValue([]);

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('No technicians available for assignment')).toBeInTheDocument();
      });
    });

    it('displays error message when loading fails', async () => {
      taskApi.getTechnicians.mockRejectedValue(new Error('Network error'));

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Network error')).toBeInTheDocument();
      });
    });
  });

  describe('Technician Selection', () => {
    it('highlights selected technician', async () => {
      const user = userEvent.setup();

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      expect(johnDoeItem).toHaveClass('selected');
    });

    it('enables Continue button when technician is selected', async () => {
      const user = userEvent.setup();

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      expect(continueButton).toBeDisabled();

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      expect(continueButton).not.toBeDisabled();
    });

    it('allows selecting technician using keyboard', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      johnDoeItem.focus();
      fireEvent.keyDown(johnDoeItem, { key: 'Enter' });

      expect(johnDoeItem).toHaveClass('selected');
    });

    it('allows selecting technician using space key', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      johnDoeItem.focus();
      fireEvent.keyDown(johnDoeItem, { key: ' ' });

      expect(johnDoeItem).toHaveClass('selected');
    });
  });

  describe('Confirmation View', () => {
    it('shows confirmation view when Continue is clicked', async () => {
      const user = userEvent.setup();

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      expect(screen.getByRole('heading', { name: 'Confirm Assignment' })).toBeInTheDocument();
      expect(screen.getByText('Are you sure you want to assign this task?')).toBeInTheDocument();
      expect(screen.getByText('Test Task')).toBeInTheDocument();
    });

    it('displays workload warning in confirmation for at-capacity technicians', async () => {
      const user = userEvent.setup();

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('Bob Wilson')).toBeInTheDocument();
      });

      const bobWilsonItem = screen.getByText('Bob Wilson').closest('[role="option"]');
      await user.click(bobWilsonItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      expect(screen.getByText(/This technician is at or exceeds capacity/)).toBeInTheDocument();
    });

    it('allows going back from confirmation', async () => {
      const user = userEvent.setup();

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const backButton = screen.getByRole('button', { name: /Back/i });
      await user.click(backButton);

      expect(screen.getByText('Assign Task')).toBeInTheDocument();
      expect(screen.getByRole('listbox')).toBeInTheDocument();
    });
  });

  describe('Assignment Process', () => {
    it('calls assignTask with correct parameters', async () => {
      const user = userEvent.setup();
      taskApi.assignTask.mockResolvedValue({ assignmentId: 1 });

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Assignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(taskApi.assignTask).toHaveBeenCalledWith(1, 1);
      });
    });

    it('shows loading state during assignment', async () => {
      const user = userEvent.setup();
      let resolveAssign;
      taskApi.assignTask.mockImplementation(() => new Promise((resolve) => {
        resolveAssign = resolve;
      }));

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Assignment/i });
      await user.click(confirmButton);

      expect(screen.getByRole('button', { name: /Assigning.../i })).toBeDisabled();

      resolveAssign({ assignmentId: 1 });
    });

    it('displays success message after assignment', async () => {
      const user = userEvent.setup();
      taskApi.assignTask.mockResolvedValue({ assignmentId: 1 });

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Assignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(screen.getByText('Task successfully assigned to John Doe')).toBeInTheDocument();
      });
    });

    it('displays error message when assignment fails', async () => {
      const user = userEvent.setup();
      taskApi.assignTask.mockRejectedValue(new Error('Assignment failed'));

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Assignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(screen.getByText('Assignment failed')).toBeInTheDocument();
      });
    });

    it('goes back to selection view when assignment fails', async () => {
      const user = userEvent.setup();
      taskApi.assignTask.mockRejectedValue(new Error('Assignment failed'));

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Assignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        expect(screen.getByText('Assign Task')).toBeInTheDocument();
        expect(screen.getByRole('listbox')).toBeInTheDocument();
      });
    });
  });

  describe('Modal Interactions', () => {
    it('calls onClose when Cancel button is clicked', async () => {
      const user = userEvent.setup();

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const cancelButton = screen.getByRole('button', { name: /Cancel/i });
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('calls onClose when close button is clicked', async () => {
      const user = userEvent.setup();

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const closeButton = screen.getByLabelText('Close modal');
      await user.click(closeButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('calls onClose when clicking overlay', async () => {
      const user = userEvent.setup();

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const overlay = screen.getByRole('dialog');
      await user.click(overlay);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('calls onClose when Escape key is pressed', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      fireEvent.keyDown(document, { key: 'Escape' });

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('does not close modal while assignment is in progress', async () => {
      const user = userEvent.setup();
      taskApi.assignTask.mockImplementation(() => new Promise(() => {}));

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Assignment/i });
      await user.click(confirmButton);

      fireEvent.keyDown(document, { key: 'Escape' });

      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    it('has proper dialog role and aria attributes', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      const dialog = screen.getByRole('dialog');
      expect(dialog).toHaveAttribute('aria-modal', 'true');
      expect(dialog).toHaveAttribute('aria-labelledby', 'modal-title');
    });

    it('has proper listbox role for technician list', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        const listbox = screen.getByRole('listbox');
        expect(listbox).toHaveAttribute('aria-label', 'Available technicians');
      });
    });

    it('has proper option role for technician items', async () => {
      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        const options = screen.getAllByRole('option');
        expect(options).toHaveLength(3);
      });
    });

    it('uses role="alert" for error messages', async () => {
      taskApi.getTechnicians.mockRejectedValue(new Error('Test error'));

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        const error = screen.getByRole('alert');
        expect(error).toHaveTextContent('Test error');
      });
    });

    it('uses role="status" for success messages', async () => {
      const user = userEvent.setup();
      taskApi.assignTask.mockResolvedValue({ assignmentId: 1 });

      render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      const confirmButton = screen.getByRole('button', { name: /Confirm Assignment/i });
      await user.click(confirmButton);

      await waitFor(() => {
        const success = screen.getByRole('status');
        expect(success).toHaveTextContent('Task successfully assigned to John Doe');
      });
    });
  });

  describe('State Reset', () => {
    it('resets state when modal is reopened', async () => {
      const user = userEvent.setup();
      const { rerender } = render(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument();
      });

      // Select a technician
      const johnDoeItem = screen.getByText('John Doe').closest('[role="option"]');
      await user.click(johnDoeItem);

      // Go to confirmation
      const continueButton = screen.getByRole('button', { name: /Continue/i });
      await user.click(continueButton);

      // Close modal
      rerender(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={false}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      // Reopen modal
      rerender(
        <TechnicianAssignmentModal
          task={mockTask}
          isOpen={true}
          onClose={mockOnClose}
          onAssignmentComplete={mockOnAssignmentComplete}
        />
      );

      // Should be back to selection view, not confirmation
      await waitFor(() => {
        expect(screen.getByText('Assign Task')).toBeInTheDocument();
        expect(screen.getByRole('listbox')).toBeInTheDocument();
      });
    });
  });
});
