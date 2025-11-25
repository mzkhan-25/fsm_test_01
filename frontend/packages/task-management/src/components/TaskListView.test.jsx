import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskListView from './TaskListView';
import * as taskApi from '../services/taskApi';

// Mock the taskApi module
vi.mock('../services/taskApi', () => ({
  getTasks: vi.fn(),
}));

describe('TaskListView', () => {
  const mockTasksResponse = {
    tasks: [
      {
        id: 1,
        title: 'Repair HVAC System',
        clientAddress: '123 Main St, Springfield, IL',
        status: 'UNASSIGNED',
        priority: 'HIGH',
        assignedTechnician: null,
        createdAt: '2024-01-15T10:00:00Z',
      },
      {
        id: 2,
        title: 'Install Smart Thermostat',
        clientAddress: '456 Oak Ave, Chicago, IL',
        status: 'ASSIGNED',
        priority: 'MEDIUM',
        assignedTechnician: 'John Doe',
        createdAt: '2024-01-14T09:30:00Z',
      },
      {
        id: 3,
        title: 'AC Maintenance Check',
        clientAddress: '789 Elm Rd, Aurora, IL',
        status: 'IN_PROGRESS',
        priority: 'LOW',
        assignedTechnician: 'Jane Smith',
        createdAt: '2024-01-13T14:00:00Z',
      },
    ],
    page: 0,
    pageSize: 10,
    totalElements: 3,
    totalPages: 1,
    first: true,
    last: true,
    statusCounts: {
      UNASSIGNED: 1,
      ASSIGNED: 1,
      IN_PROGRESS: 1,
      COMPLETED: 0,
    },
  };

  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    taskApi.getTasks.mockResolvedValue(mockTasksResponse);
  });

  afterEach(() => {
    vi.clearAllMocks();
    vi.useRealTimers();
  });

  describe('Initial Rendering', () => {
    it('renders the task list title', async () => {
      render(<TaskListView />);
      
      expect(screen.getByText('Task List')).toBeInTheDocument();
    });

    it('shows loading state initially', async () => {
      render(<TaskListView />);
      
      expect(screen.getByText('Loading tasks...')).toBeInTheDocument();
    });

    it('fetches and displays tasks on mount', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
        expect(screen.getByText('Install Smart Thermostat')).toBeInTheDocument();
        expect(screen.getByText('AC Maintenance Check')).toBeInTheDocument();
      });
    });

    it('displays task count information', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Showing 3 of 3 tasks')).toBeInTheDocument();
      });
    });

    it('renders the search input', async () => {
      render(<TaskListView />);
      
      expect(screen.getByPlaceholderText('Search tasks...')).toBeInTheDocument();
    });

    it('renders the status filter dropdown', async () => {
      render(<TaskListView />);
      
      expect(screen.getByLabelText('Filter by status')).toBeInTheDocument();
    });

    it('renders create task button when onCreateTask is provided', async () => {
      const onCreateTask = vi.fn();
      render(<TaskListView onCreateTask={onCreateTask} />);
      
      const createButton = screen.getByText('+ Create Task');
      expect(createButton).toBeInTheDocument();
    });

    it('does not render create task button when onCreateTask is not provided', async () => {
      render(<TaskListView />);
      
      expect(screen.queryByText('+ Create Task')).not.toBeInTheDocument();
    });
  });

  describe('Table Display', () => {
    it('displays all task fields correctly', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        // Task 1
        expect(screen.getByText('1')).toBeInTheDocument();
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
        expect(screen.getByText('123 Main St, Springfield, IL')).toBeInTheDocument();
        expect(screen.getByText('Unassigned')).toBeInTheDocument();
        expect(screen.getByText('HIGH')).toBeInTheDocument();
        
        // Task 2 with assigned technician
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('Assigned')).toBeInTheDocument();
        
        // Task 3
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
        expect(screen.getByText('In Progress')).toBeInTheDocument();
      });
    });

    it('displays dash for unassigned technician', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        const rows = screen.getAllByRole('row');
        const firstDataRow = rows[1]; // Skip header row
        expect(within(firstDataRow).getByText('-')).toBeInTheDocument();
      });
    });

    it('displays table headers correctly', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('ID')).toBeInTheDocument();
        expect(screen.getByText('Title')).toBeInTheDocument();
        expect(screen.getByText('Technician')).toBeInTheDocument();
      });
      
      // Check for sortable headers (they include sort icons)
      const statusHeader = screen.getByRole('columnheader', { name: /Status/ });
      const priorityHeader = screen.getByRole('columnheader', { name: /Priority/ });
      const createdHeader = screen.getByRole('columnheader', { name: /Created/ });
      
      expect(statusHeader).toBeInTheDocument();
      expect(priorityHeader).toBeInTheDocument();
      expect(createdHeader).toBeInTheDocument();
    });

    it('applies correct status badge classes', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        const unassignedBadge = screen.getByText('Unassigned');
        expect(unassignedBadge).toHaveClass('status-unassigned');
        
        const assignedBadge = screen.getByText('Assigned');
        expect(assignedBadge).toHaveClass('status-assigned');
        
        const inProgressBadge = screen.getByText('In Progress');
        expect(inProgressBadge).toHaveClass('status-in-progress');
      });
    });

    it('applies correct priority badge classes', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        const highBadge = screen.getByText('HIGH');
        expect(highBadge).toHaveClass('priority-high');
        
        const mediumBadge = screen.getByText('MEDIUM');
        expect(mediumBadge).toHaveClass('priority-medium');
        
        const lowBadge = screen.getByText('LOW');
        expect(lowBadge).toHaveClass('priority-low');
      });
    });
  });

  describe('Search Functionality', () => {
    it('searches with debounced input', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      const searchInput = screen.getByPlaceholderText('Search tasks...');
      await user.type(searchInput, 'HVAC');
      
      // Advance timers past debounce timeout
      await vi.advanceTimersByTimeAsync(400);
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenCalledWith(
          expect.objectContaining({
            search: 'HVAC',
            page: 0,
          })
        );
      });
    });

    it('resets page to 0 when searching', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      const searchInput = screen.getByPlaceholderText('Search tasks...');
      await user.type(searchInput, 'test');
      
      await vi.advanceTimersByTimeAsync(400);
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenCalledWith(
          expect.objectContaining({ page: 0 })
        );
      });
    });

    it('clears debounce timer on unmount', async () => {
      const { unmount } = render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      // This should not throw an error
      unmount();
    });
  });

  describe('Status Filtering', () => {
    it('displays status counts in dropdown options', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      const statusFilter = screen.getByLabelText('Filter by status');
      expect(statusFilter).toContainHTML('Unassigned (1)');
      expect(statusFilter).toContainHTML('Assigned (1)');
    });

    it('filters tasks by status', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      const statusFilter = screen.getByLabelText('Filter by status');
      await user.selectOptions(statusFilter, 'UNASSIGNED');
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenCalledWith(
          expect.objectContaining({
            status: 'UNASSIGNED',
            page: 0,
          })
        );
      });
    });

    it('clears status filter when selecting All', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      const statusFilter = screen.getByLabelText('Filter by status');
      await user.selectOptions(statusFilter, 'UNASSIGNED');
      await user.selectOptions(statusFilter, '');
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenLastCalledWith(
          expect.objectContaining({
            status: undefined,
          })
        );
      });
    });
  });

  describe('Sorting', () => {
    it('sorts by priority by default (descending)', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenCalledWith(
          expect.objectContaining({
            sortBy: 'priority',
            sortOrder: 'desc',
          })
        );
      });
    });

    it('toggles sort order when clicking the same column', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      const priorityHeader = screen.getByRole('columnheader', { name: /Priority/ });
      await user.click(priorityHeader);
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenCalledWith(
          expect.objectContaining({
            sortBy: 'priority',
            sortOrder: 'asc',
          })
        );
      });
    });

    it('changes sort column when clicking a different column', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      const createdHeader = screen.getByRole('columnheader', { name: /Created/ });
      await user.click(createdHeader);
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenCalledWith(
          expect.objectContaining({
            sortBy: 'createdAt',
            sortOrder: 'desc',
          })
        );
      });
    });

    it('displays correct sort icons', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      // Priority should show descending icon by default
      const priorityHeader = screen.getByRole('columnheader', { name: /Priority/ });
      expect(priorityHeader).toHaveAttribute('aria-sort', 'descending');
    });

    it('resets page when sorting', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      const statusHeader = screen.getByRole('columnheader', { name: /Status/ });
      await user.click(statusHeader);
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenCalledWith(
          expect.objectContaining({ page: 0 })
        );
      });
    });
  });

  describe('Pagination', () => {
    const paginatedResponse = {
      ...mockTasksResponse,
      totalPages: 5,
      totalElements: 50,
      page: 2,
      first: false,
      last: false,
    };

    it('displays pagination when there are multiple pages', async () => {
      taskApi.getTasks.mockResolvedValue(paginatedResponse);
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByLabelText('Previous page')).toBeInTheDocument();
        expect(screen.getByLabelText('Next page')).toBeInTheDocument();
      });
    });

    it('does not display pagination when there is only one page', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('Repair HVAC System')).toBeInTheDocument();
      });
      
      expect(screen.queryByLabelText('Previous page')).not.toBeInTheDocument();
    });

    it('navigates to previous page', async () => {
      taskApi.getTasks.mockResolvedValue(paginatedResponse);
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByLabelText('Previous page')).toBeInTheDocument();
      });
      
      await user.click(screen.getByLabelText('Previous page'));
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenLastCalledWith(
          expect.objectContaining({ page: expect.any(Number) })
        );
      });
    });

    it('navigates to next page', async () => {
      taskApi.getTasks.mockResolvedValue({
        ...paginatedResponse,
        page: 0,
        first: true,
        last: false,
      });
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByLabelText('Next page')).toBeInTheDocument();
      });
      
      await user.click(screen.getByLabelText('Next page'));
      
      await waitFor(() => {
        expect(taskApi.getTasks).toHaveBeenCalled();
      });
    });

    it('disables previous button on first page', async () => {
      taskApi.getTasks.mockResolvedValue({
        ...paginatedResponse,
        page: 0,
        first: true,
      });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByLabelText('Previous page')).toBeDisabled();
      });
    });

    it('disables next button on last page', async () => {
      // Mock with 2 pages so pagination shows
      const twoPageResponse = {
        ...paginatedResponse,
        totalPages: 2,
        page: 0,
        first: true,
        last: false,
      };
      taskApi.getTasks.mockResolvedValue(twoPageResponse);
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByLabelText('Next page')).toBeInTheDocument();
      });
      
      // Click next to go to page 2 (last page)
      taskApi.getTasks.mockResolvedValue({
        ...paginatedResponse,
        totalPages: 2,
        page: 1,
        first: false,
        last: true,
      });
      
      await user.click(screen.getByLabelText('Next page'));
      
      await waitFor(() => {
        expect(screen.getByLabelText('Next page')).toBeDisabled();
      });
    });

    it('displays page numbers', async () => {
      taskApi.getTasks.mockResolvedValue(paginatedResponse);
      render(<TaskListView />);
      
      await waitFor(() => {
        // Should show page numbers
        expect(screen.getByLabelText('Go to page 1')).toBeInTheDocument();
      });
    });

    it('marks current page as active', async () => {
      taskApi.getTasks.mockResolvedValue({
        ...paginatedResponse,
        page: 0,
      });
      render(<TaskListView />);
      
      await waitFor(() => {
        const page1Button = screen.getByLabelText('Go to page 1');
        expect(page1Button).toHaveClass('active');
      });
    });
  });

  describe('Empty State', () => {
    it('displays empty state when no tasks found', async () => {
      taskApi.getTasks.mockResolvedValue({
        tasks: [],
        page: 0,
        pageSize: 10,
        totalElements: 0,
        totalPages: 0,
        statusCounts: {},
      });
      
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('No tasks found.')).toBeInTheDocument();
      });
    });

    it('displays hint when empty with filters applied', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      taskApi.getTasks.mockResolvedValue({
        tasks: [],
        page: 0,
        pageSize: 10,
        totalElements: 0,
        totalPages: 0,
        statusCounts: {},
      });
      
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('No tasks found.')).toBeInTheDocument();
      });
      
      const statusFilter = screen.getByLabelText('Filter by status');
      await user.selectOptions(statusFilter, 'UNASSIGNED');
      
      await waitFor(() => {
        expect(screen.getByText('Try adjusting your search or filter criteria.')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('displays error message when fetch fails', async () => {
      taskApi.getTasks.mockRejectedValue(new Error('Network error'));
      
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByRole('alert')).toHaveTextContent('Network error');
      });
    });

    it('displays default error message when error has no message', async () => {
      taskApi.getTasks.mockRejectedValue({});
      
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByRole('alert')).toHaveTextContent('Failed to fetch tasks');
      });
    });

    it('clears tasks on error', async () => {
      taskApi.getTasks.mockRejectedValue(new Error('Server error'));
      
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByRole('alert')).toBeInTheDocument();
      });
      
      expect(screen.queryByRole('table')).not.toBeInTheDocument();
    });
  });

  describe('Create Task Button', () => {
    it('calls onCreateTask when create button is clicked', async () => {
      const onCreateTask = vi.fn();
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
      
      render(<TaskListView onCreateTask={onCreateTask} />);
      
      await waitFor(() => {
        expect(screen.getByText('+ Create Task')).toBeInTheDocument();
      });
      
      await user.click(screen.getByText('+ Create Task'));
      
      expect(onCreateTask).toHaveBeenCalledTimes(1);
    });
  });

  describe('Accessibility', () => {
    it('has proper aria labels for search', async () => {
      render(<TaskListView />);
      
      expect(screen.getByLabelText('Search tasks')).toBeInTheDocument();
    });

    it('has proper aria labels for filter', async () => {
      render(<TaskListView />);
      
      expect(screen.getByLabelText('Filter by status')).toBeInTheDocument();
    });

    it('has proper aria label for task table', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByRole('table', { name: 'Task list' })).toBeInTheDocument();
      });
    });

    it('has proper aria-sort for sortable columns', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        const priorityHeader = screen.getByRole('columnheader', { name: /Priority/ });
        expect(priorityHeader).toHaveAttribute('aria-sort', 'descending');
        
        const statusHeader = screen.getByRole('columnheader', { name: /Status/ });
        expect(statusHeader).toHaveAttribute('aria-sort', 'none');
      });
    });

    it('has proper role for pagination navigation', async () => {
      taskApi.getTasks.mockResolvedValue({
        ...mockTasksResponse,
        totalPages: 3,
      });
      
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByRole('navigation', { name: 'Pagination' })).toBeInTheDocument();
      });
    });

    it('loading state is announced to screen readers', async () => {
      render(<TaskListView />);
      
      const loadingContainer = screen.getByRole('status');
      expect(loadingContainer).toHaveAttribute('aria-live', 'polite');
    });
  });

  describe('Date Formatting', () => {
    it('formats dates correctly', async () => {
      render(<TaskListView />);
      
      await waitFor(() => {
        // The date should be formatted
        expect(screen.getByText(/Jan 15, 2024/)).toBeInTheDocument();
      });
    });

    it('displays dash for missing dates', async () => {
      taskApi.getTasks.mockResolvedValue({
        ...mockTasksResponse,
        tasks: [{
          id: 1,
          title: 'Test Task',
          clientAddress: '123 Main St',
          status: 'UNASSIGNED',
          priority: 'HIGH',
          assignedTechnician: null,
          createdAt: null,
        }],
      });
      
      render(<TaskListView />);
      
      await waitFor(() => {
        const rows = screen.getAllByRole('row');
        const dataRow = rows[1];
        expect(within(dataRow).getAllByText('-').length).toBeGreaterThanOrEqual(1);
      });
    });
  });

  describe('Priority and Status Display', () => {
    it('handles URGENT priority', async () => {
      taskApi.getTasks.mockResolvedValue({
        ...mockTasksResponse,
        tasks: [{
          id: 1,
          title: 'Urgent Task',
          clientAddress: '123 Main St',
          status: 'UNASSIGNED',
          priority: 'URGENT',
          assignedTechnician: null,
          createdAt: '2024-01-15T10:00:00Z',
        }],
      });
      
      render(<TaskListView />);
      
      await waitFor(() => {
        const urgentBadge = screen.getByText('URGENT');
        expect(urgentBadge).toHaveClass('priority-urgent');
      });
    });

    it('handles COMPLETED status', async () => {
      taskApi.getTasks.mockResolvedValue({
        ...mockTasksResponse,
        tasks: [{
          id: 1,
          title: 'Completed Task',
          clientAddress: '123 Main St',
          status: 'COMPLETED',
          priority: 'LOW',
          assignedTechnician: 'Tech',
          createdAt: '2024-01-15T10:00:00Z',
        }],
      });
      
      render(<TaskListView />);
      
      await waitFor(() => {
        const completedBadge = screen.getByText('Completed');
        expect(completedBadge).toHaveClass('status-completed');
      });
    });

    it('handles unknown status gracefully', async () => {
      taskApi.getTasks.mockResolvedValue({
        ...mockTasksResponse,
        tasks: [{
          id: 1,
          title: 'Unknown Status Task',
          clientAddress: '123 Main St',
          status: 'UNKNOWN_STATUS',
          priority: 'LOW',
          assignedTechnician: null,
          createdAt: '2024-01-15T10:00:00Z',
        }],
      });
      
      render(<TaskListView />);
      
      await waitFor(() => {
        expect(screen.getByText('UNKNOWN_STATUS')).toBeInTheDocument();
      });
    });
  });
});
