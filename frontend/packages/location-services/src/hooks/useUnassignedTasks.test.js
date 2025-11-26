import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useUnassignedTasks } from './useUnassignedTasks';
import * as taskService from '../services/taskService';
import * as geocodeService from '../services/geocodeService';

// Mock the services
vi.mock('../services/taskService', () => ({
  fetchUnassignedTasks: vi.fn(),
}));

vi.mock('../services/geocodeService', () => ({
  processTasksWithCoordinates: vi.fn(),
}));

describe('useUnassignedTasks', () => {
  const mockTasks = [
    { id: 1, title: 'Task 1', clientAddress: '123 Main St' },
    { id: 2, title: 'Task 2', clientAddress: '456 Oak Ave' },
  ];

  const mockProcessedTasks = [
    { id: 1, title: 'Task 1', clientAddress: '123 Main St', coordinates: { lat: 37.77, lng: -122.42 } },
    { id: 2, title: 'Task 2', clientAddress: '456 Oak Ave', coordinates: { lat: 37.78, lng: -122.41 } },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('starts with loading state', () => {
    taskService.fetchUnassignedTasks.mockReturnValue(new Promise(() => {}));
    geocodeService.processTasksWithCoordinates.mockReturnValue([]);

    const { result } = renderHook(() => useUnassignedTasks());

    expect(result.current.loading).toBe(true);
    expect(result.current.tasks).toEqual([]);
    expect(result.current.error).toBeNull();
  });

  it('fetches tasks on mount', async () => {
    taskService.fetchUnassignedTasks.mockResolvedValue({ tasks: mockTasks });
    geocodeService.processTasksWithCoordinates.mockReturnValue(mockProcessedTasks);

    const { result } = renderHook(() => useUnassignedTasks());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(taskService.fetchUnassignedTasks).toHaveBeenCalled();
    expect(result.current.tasks).toEqual(mockProcessedTasks);
  });

  it('processes tasks with coordinates', async () => {
    taskService.fetchUnassignedTasks.mockResolvedValue({ tasks: mockTasks });
    geocodeService.processTasksWithCoordinates.mockReturnValue(mockProcessedTasks);

    renderHook(() => useUnassignedTasks());

    await waitFor(() => {
      expect(geocodeService.processTasksWithCoordinates).toHaveBeenCalledWith(mockTasks);
    });
  });

  it('sets error state on fetch failure', async () => {
    taskService.fetchUnassignedTasks.mockRejectedValue(new Error('API Error'));
    geocodeService.processTasksWithCoordinates.mockReturnValue([]);

    const { result } = renderHook(() => useUnassignedTasks());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe('API Error');
    expect(result.current.tasks).toEqual([]);
  });

  it('sets lastUpdated after successful fetch', async () => {
    taskService.fetchUnassignedTasks.mockResolvedValue({ tasks: mockTasks });
    geocodeService.processTasksWithCoordinates.mockReturnValue(mockProcessedTasks);

    const { result } = renderHook(() => useUnassignedTasks());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.lastUpdated).toBeInstanceOf(Date);
  });

  it('provides refresh function', async () => {
    taskService.fetchUnassignedTasks.mockResolvedValue({ tasks: mockTasks });
    geocodeService.processTasksWithCoordinates.mockReturnValue(mockProcessedTasks);

    const { result } = renderHook(() => useUnassignedTasks());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(typeof result.current.refresh).toBe('function');
  });

  it('refresh function reloads tasks', async () => {
    taskService.fetchUnassignedTasks.mockResolvedValue({ tasks: mockTasks });
    geocodeService.processTasksWithCoordinates.mockReturnValue(mockProcessedTasks);

    const { result } = renderHook(() => useUnassignedTasks());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(taskService.fetchUnassignedTasks).toHaveBeenCalledTimes(1);

    await act(async () => {
      await result.current.refresh();
    });

    expect(taskService.fetchUnassignedTasks).toHaveBeenCalledTimes(2);
  });

  it('handles empty tasks response', async () => {
    taskService.fetchUnassignedTasks.mockResolvedValue({ tasks: [] });
    geocodeService.processTasksWithCoordinates.mockReturnValue([]);

    const { result } = renderHook(() => useUnassignedTasks());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.tasks).toEqual([]);
    expect(result.current.error).toBeNull();
  });

  it('handles undefined tasks in response', async () => {
    taskService.fetchUnassignedTasks.mockResolvedValue({});
    geocodeService.processTasksWithCoordinates.mockReturnValue([]);

    const { result } = renderHook(() => useUnassignedTasks());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(geocodeService.processTasksWithCoordinates).toHaveBeenCalledWith([]);
  });

  describe('auto-refresh', () => {
    beforeEach(() => {
      vi.useFakeTimers({ shouldAdvanceTime: true });
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('auto-refreshes when interval is set', async () => {
      taskService.fetchUnassignedTasks.mockResolvedValue({ tasks: mockTasks });
      geocodeService.processTasksWithCoordinates.mockReturnValue(mockProcessedTasks);

      renderHook(() => useUnassignedTasks({ refreshInterval: 5000 }));

      // Wait for initial load
      await vi.waitFor(() => {
        expect(taskService.fetchUnassignedTasks).toHaveBeenCalledTimes(1);
      });

      // Advance time to trigger refresh
      await act(async () => {
        vi.advanceTimersByTime(5000);
      });

      await vi.waitFor(() => {
        expect(taskService.fetchUnassignedTasks).toHaveBeenCalledTimes(2);
      });
    });

    it('does not auto-refresh when interval is 0', async () => {
      taskService.fetchUnassignedTasks.mockResolvedValue({ tasks: mockTasks });
      geocodeService.processTasksWithCoordinates.mockReturnValue(mockProcessedTasks);

      renderHook(() => useUnassignedTasks({ refreshInterval: 0 }));

      // Wait for initial load
      await vi.waitFor(() => {
        expect(taskService.fetchUnassignedTasks).toHaveBeenCalledTimes(1);
      });

      // Advance time
      await act(async () => {
        vi.advanceTimersByTime(10000);
      });

      // Should still be 1 (no auto-refresh)
      expect(taskService.fetchUnassignedTasks).toHaveBeenCalledTimes(1);
    });

    it('clears interval on unmount', async () => {
      taskService.fetchUnassignedTasks.mockResolvedValue({ tasks: mockTasks });
      geocodeService.processTasksWithCoordinates.mockReturnValue(mockProcessedTasks);

      const { unmount } = renderHook(() => useUnassignedTasks({ refreshInterval: 5000 }));

      // Wait for initial load
      await vi.waitFor(() => {
        expect(taskService.fetchUnassignedTasks).toHaveBeenCalledTimes(1);
      });

      unmount();

      // Advance time
      await act(async () => {
        vi.advanceTimersByTime(5000);
      });

      // Should still be 1 after unmount
      expect(taskService.fetchUnassignedTasks).toHaveBeenCalledTimes(1);
    });
  });
});
