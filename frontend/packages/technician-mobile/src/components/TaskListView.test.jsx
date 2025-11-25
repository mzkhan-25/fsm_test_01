import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
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
});
