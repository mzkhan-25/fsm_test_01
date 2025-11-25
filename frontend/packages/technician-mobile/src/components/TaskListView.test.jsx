import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import TaskListView from './TaskListView';
import * as taskService from '../services/taskService';

vi.mock('../services/taskService', () => ({
  getAssignedTasks: vi.fn(),
}));

describe('TaskListView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should show loading state initially', () => {
    taskService.getAssignedTasks.mockImplementation(
      () => new Promise(() => {})
    );

    render(<TaskListView />);

    expect(screen.getByText('Loading tasks...')).toBeInTheDocument();
  });

  it('should render task list header', async () => {
    taskService.getAssignedTasks.mockResolvedValue([]);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('My Tasks')).toBeInTheDocument();
      expect(
        screen.getByRole('button', { name: 'Refresh tasks' })
      ).toBeInTheDocument();
    });
  });

  it('should show no tasks message when list is empty', async () => {
    taskService.getAssignedTasks.mockResolvedValue([]);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('No tasks assigned to you.')).toBeInTheDocument();
    });
  });

  it('should render task cards', async () => {
    const mockTasks = [
      {
        id: '1',
        title: 'Fix HVAC System',
        address: '123 Main St',
        priority: 'HIGH',
        status: 'ASSIGNED',
      },
      {
        id: '2',
        title: 'Install Router',
        address: '456 Oak Ave',
        priority: 'LOW',
        status: 'IN_PROGRESS',
      },
    ];

    taskService.getAssignedTasks.mockResolvedValue(mockTasks);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('Fix HVAC System')).toBeInTheDocument();
      expect(screen.getByText('123 Main St')).toBeInTheDocument();
      expect(screen.getByText('HIGH')).toBeInTheDocument();
      expect(screen.getByText('Install Router')).toBeInTheDocument();
      expect(screen.getByText('456 Oak Ave')).toBeInTheDocument();
      expect(screen.getByText('LOW')).toBeInTheDocument();
    });
  });

  it('should apply correct priority class', async () => {
    const mockTasks = [
      {
        id: '1',
        title: 'High Priority Task',
        address: '123 St',
        priority: 'HIGH',
        status: 'ASSIGNED',
      },
      {
        id: '2',
        title: 'Medium Priority Task',
        address: '456 St',
        priority: 'MEDIUM',
        status: 'ASSIGNED',
      },
      {
        id: '3',
        title: 'Low Priority Task',
        address: '789 St',
        priority: 'LOW',
        status: 'ASSIGNED',
      },
    ];

    taskService.getAssignedTasks.mockResolvedValue(mockTasks);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('HIGH')).toHaveClass('high');
      expect(screen.getByText('MEDIUM')).toHaveClass('medium');
      expect(screen.getByText('LOW')).toHaveClass('low');
    });
  });

  it('should apply correct status class', async () => {
    const mockTasks = [
      {
        id: '1',
        title: 'In Progress Task',
        address: '123 St',
        priority: 'HIGH',
        status: 'IN_PROGRESS',
      },
      {
        id: '2',
        title: 'Completed Task',
        address: '456 St',
        priority: 'LOW',
        status: 'COMPLETED',
      },
    ];

    taskService.getAssignedTasks.mockResolvedValue(mockTasks);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('IN PROGRESS')).toHaveClass('in-progress');
      expect(screen.getByText('COMPLETED')).toHaveClass('completed');
    });
  });

  it('should show error message on fetch failure', async () => {
    taskService.getAssignedTasks.mockRejectedValue(
      new Error('Failed to load tasks')
    );

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load tasks')).toBeInTheDocument();
    });
  });

  it('should refresh tasks when refresh button is clicked', async () => {
    taskService.getAssignedTasks.mockResolvedValue([]);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('My Tasks')).toBeInTheDocument();
    });

    taskService.getAssignedTasks.mockResolvedValue([
      {
        id: '1',
        title: 'New Task',
        address: '123 St',
        priority: 'HIGH',
        status: 'ASSIGNED',
      },
    ]);

    fireEvent.click(screen.getByRole('button', { name: 'Refresh tasks' }));

    await waitFor(() => {
      expect(screen.getByText('New Task')).toBeInTheDocument();
    });

    expect(taskService.getAssignedTasks).toHaveBeenCalledTimes(2);
  });

  it('should format status with underscores replaced by spaces', async () => {
    const mockTasks = [
      {
        id: '1',
        title: 'Task',
        address: '123 St',
        priority: 'HIGH',
        status: 'IN_PROGRESS',
      },
    ];

    taskService.getAssignedTasks.mockResolvedValue(mockTasks);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('IN PROGRESS')).toBeInTheDocument();
    });
  });

  it('should render task list with role list', async () => {
    taskService.getAssignedTasks.mockResolvedValue([
      {
        id: '1',
        title: 'Task',
        address: '123 St',
        priority: 'HIGH',
        status: 'ASSIGNED',
      },
    ]);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByRole('list')).toBeInTheDocument();
    });
  });

  it('should handle unknown priority gracefully', async () => {
    const mockTasks = [
      {
        id: '1',
        title: 'Task',
        address: '123 St',
        priority: 'UNKNOWN',
        status: 'ASSIGNED',
      },
    ];

    taskService.getAssignedTasks.mockResolvedValue(mockTasks);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('UNKNOWN')).toBeInTheDocument();
    });
  });

  it('should handle unknown status gracefully', async () => {
    const mockTasks = [
      {
        id: '1',
        title: 'Task',
        address: '123 St',
        priority: 'HIGH',
        status: 'PENDING',
      },
    ];

    taskService.getAssignedTasks.mockResolvedValue(mockTasks);

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('PENDING')).toBeInTheDocument();
    });
  });

  it('should show default error message when error has no message', async () => {
    taskService.getAssignedTasks.mockRejectedValue(new Error());

    render(<TaskListView />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load tasks')).toBeInTheDocument();
    });
  });

  // New tests for status filter tabs
  describe('Status Filter Tabs', () => {
    it('should render all filter tabs', async () => {
      taskService.getAssignedTasks.mockResolvedValue([]);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByRole('tab', { name: 'All' })).toBeInTheDocument();
        expect(screen.getByRole('tab', { name: 'Assigned' })).toBeInTheDocument();
        expect(screen.getByRole('tab', { name: 'In Progress' })).toBeInTheDocument();
        expect(screen.getByRole('tab', { name: 'Completed' })).toBeInTheDocument();
      });
    });

    it('should have All filter active by default', async () => {
      taskService.getAssignedTasks.mockResolvedValue([]);

      render(<TaskListView />);

      await waitFor(() => {
        const allTab = screen.getByRole('tab', { name: 'All' });
        expect(allTab).toHaveClass('active');
        expect(allTab).toHaveAttribute('aria-selected', 'true');
      });
    });

    it('should change active filter when tab is clicked', async () => {
      taskService.getAssignedTasks.mockResolvedValue([]);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('My Tasks')).toBeInTheDocument();
      });

      const inProgressTab = screen.getByRole('tab', { name: 'In Progress' });
      
      await act(async () => {
        fireEvent.click(inProgressTab);
      });

      await waitFor(() => {
        expect(inProgressTab).toHaveClass('active');
        expect(inProgressTab).toHaveAttribute('aria-selected', 'true');
      });
    });

    it('should call getAssignedTasks with correct status when filter changes', async () => {
      taskService.getAssignedTasks.mockResolvedValue([]);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('My Tasks')).toBeInTheDocument();
      });

      await act(async () => {
        fireEvent.click(screen.getByRole('tab', { name: 'Assigned' }));
      });

      await waitFor(() => {
        expect(taskService.getAssignedTasks).toHaveBeenCalledWith('assigned');
      });
    });

    it('should have proper ARIA attributes for filter tabs', async () => {
      taskService.getAssignedTasks.mockResolvedValue([]);

      render(<TaskListView />);

      await waitFor(() => {
        const tablist = screen.getByRole('tablist');
        expect(tablist).toHaveAttribute('aria-label', 'Filter tasks by status');
        
        const tabs = screen.getAllByRole('tab');
        tabs.forEach(tab => {
          expect(tab).toHaveAttribute('aria-controls', 'task-list-panel');
        });
      });
    });
  });

  // New tests for estimated duration display
  describe('Estimated Duration', () => {
    it('should display estimated duration when provided', async () => {
      const mockTasks = [
        {
          id: '1',
          title: 'Task with duration',
          address: '123 St',
          priority: 'HIGH',
          status: 'ASSIGNED',
          estimatedDuration: 45,
        },
      ];

      taskService.getAssignedTasks.mockResolvedValue(mockTasks);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('45 min')).toBeInTheDocument();
      });
    });

    it('should format duration in hours when >= 60 minutes', async () => {
      const mockTasks = [
        {
          id: '1',
          title: 'Long Task',
          address: '123 St',
          priority: 'HIGH',
          status: 'ASSIGNED',
          estimatedDuration: 90,
        },
      ];

      taskService.getAssignedTasks.mockResolvedValue(mockTasks);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('1h 30min')).toBeInTheDocument();
      });
    });

    it('should format duration in whole hours when no remaining minutes', async () => {
      const mockTasks = [
        {
          id: '1',
          title: 'Exact Hour Task',
          address: '123 St',
          priority: 'HIGH',
          status: 'ASSIGNED',
          estimatedDuration: 120,
        },
      ];

      taskService.getAssignedTasks.mockResolvedValue(mockTasks);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('2h')).toBeInTheDocument();
      });
    });

    it('should not display duration when not provided', async () => {
      const mockTasks = [
        {
          id: '1',
          title: 'Task without duration',
          address: '123 St',
          priority: 'HIGH',
          status: 'ASSIGNED',
        },
      ];

      taskService.getAssignedTasks.mockResolvedValue(mockTasks);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('Task without duration')).toBeInTheDocument();
        expect(screen.queryByText(/min/)).not.toBeInTheDocument();
      });
    });
  });

  // New tests for clientAddress field
  describe('Client Address', () => {
    it('should display clientAddress when provided', async () => {
      const mockTasks = [
        {
          id: '1',
          title: 'Task',
          clientAddress: '789 Client Ave',
          priority: 'HIGH',
          status: 'ASSIGNED',
        },
      ];

      taskService.getAssignedTasks.mockResolvedValue(mockTasks);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('789 Client Ave')).toBeInTheDocument();
      });
    });

    it('should fallback to address field when clientAddress not provided', async () => {
      const mockTasks = [
        {
          id: '1',
          title: 'Task',
          address: '123 Fallback St',
          priority: 'HIGH',
          status: 'ASSIGNED',
        },
      ];

      taskService.getAssignedTasks.mockResolvedValue(mockTasks);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('123 Fallback St')).toBeInTheDocument();
      });
    });
  });

  // New tests for pull-to-refresh
  describe('Pull to Refresh', () => {
    it('should trigger refresh on pull down gesture', async () => {
      taskService.getAssignedTasks.mockResolvedValue([]);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('My Tasks')).toBeInTheDocument();
      });

      const container = document.querySelector('.task-list-view');
      
      // Mock scrollTop to 0 (at top of list)
      Object.defineProperty(container, 'scrollTop', {
        value: 0,
        writable: true,
      });

      // Simulate touch events for pull-to-refresh
      await act(async () => {
        fireEvent.touchStart(container, {
          touches: [{ clientY: 0 }],
        });
        fireEvent.touchMove(container, {
          touches: [{ clientY: 100 }],
        });
        fireEvent.touchEnd(container);
      });

      // Verify getAssignedTasks was called again (initial + refresh)
      await waitFor(() => {
        expect(taskService.getAssignedTasks).toHaveBeenCalledTimes(2);
      });
    });

    it('should show refreshing indicator during pull-to-refresh', async () => {
      let resolvePromise;
      taskService.getAssignedTasks.mockImplementation(() => {
        return new Promise((resolve) => {
          resolvePromise = resolve;
        });
      });

      render(<TaskListView />);

      // Wait for initial call
      await waitFor(() => {
        expect(taskService.getAssignedTasks).toHaveBeenCalled();
      });

      // Resolve initial fetch
      await act(async () => {
        resolvePromise([]);
      });

      await waitFor(() => {
        expect(screen.getByText('My Tasks')).toBeInTheDocument();
      });

      // Setup for refresh
      taskService.getAssignedTasks.mockImplementation(() => {
        return new Promise((resolve) => {
          resolvePromise = resolve;
        });
      });

      const container = document.querySelector('.task-list-view');
      Object.defineProperty(container, 'scrollTop', {
        value: 0,
        writable: true,
      });

      // Trigger pull-to-refresh
      await act(async () => {
        fireEvent.touchStart(container, {
          touches: [{ clientY: 0 }],
        });
        fireEvent.touchMove(container, {
          touches: [{ clientY: 100 }],
        });
        fireEvent.touchEnd(container);
      });

      // Check for refreshing indicator
      expect(screen.getByText('Refreshing...')).toBeInTheDocument();

      // Resolve refresh
      await act(async () => {
        resolvePromise([]);
      });
    });

    it('should not trigger refresh when not at top of list', async () => {
      taskService.getAssignedTasks.mockResolvedValue([
        { id: '1', title: 'Task', address: '123 St', priority: 'HIGH', status: 'ASSIGNED' },
      ]);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('My Tasks')).toBeInTheDocument();
      });

      const initialCallCount = taskService.getAssignedTasks.mock.calls.length;

      const container = document.querySelector('.task-list-view');
      
      // Mock scrollTop to non-zero (not at top)
      Object.defineProperty(container, 'scrollTop', {
        value: 100,
        writable: true,
      });

      await act(async () => {
        fireEvent.touchStart(container, {
          touches: [{ clientY: 0 }],
        });
        fireEvent.touchMove(container, {
          touches: [{ clientY: 100 }],
        });
        fireEvent.touchEnd(container);
      });

      // Should not have triggered additional fetch
      expect(taskService.getAssignedTasks).toHaveBeenCalledTimes(initialCallCount);
    });

    it('should not trigger refresh on small pull distance', async () => {
      taskService.getAssignedTasks.mockResolvedValue([]);

      render(<TaskListView />);

      await waitFor(() => {
        expect(screen.getByText('My Tasks')).toBeInTheDocument();
      });

      const initialCallCount = taskService.getAssignedTasks.mock.calls.length;

      const container = document.querySelector('.task-list-view');
      Object.defineProperty(container, 'scrollTop', {
        value: 0,
        writable: true,
      });

      // Small pull distance (less than threshold)
      await act(async () => {
        fireEvent.touchStart(container, {
          touches: [{ clientY: 0 }],
        });
        fireEvent.touchMove(container, {
          touches: [{ clientY: 30 }],
        });
        fireEvent.touchEnd(container);
      });

      // Should not have triggered additional fetch
      expect(taskService.getAssignedTasks).toHaveBeenCalledTimes(initialCallCount);
    });
  });
});
