import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  getAssignedTasks,
  getTaskById,
  updateTaskStatus,
  completeTask,
  updateLocation,
} from './taskService';

// Mock authService
vi.mock('./authService', () => ({
  getAuthHeaders: () => ({
    'Content-Type': 'application/json',
    Authorization: 'Bearer test-token',
  }),
}));

// Mock fetch
global.fetch = vi.fn();

describe('taskService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAssignedTasks', () => {
    it('should fetch assigned tasks successfully', async () => {
      const mockTasks = [
        { id: '1', title: 'Task 1', status: 'ASSIGNED' },
        { id: '2', title: 'Task 2', status: 'IN_PROGRESS' },
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ tasks: mockTasks }),
      });

      const result = await getAssignedTasks();

      expect(result).toEqual(mockTasks);
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/technicians/me/tasks'),
        expect.objectContaining({
          method: 'GET',
        })
      );
    });

    it('should fetch assigned tasks with status filter', async () => {
      const mockTasks = [
        { id: '1', title: 'Task 1', status: 'IN_PROGRESS' },
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ tasks: mockTasks }),
      });

      const result = await getAssignedTasks('in_progress');

      expect(result).toEqual(mockTasks);
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('status=in_progress'),
        expect.objectContaining({
          method: 'GET',
        })
      );
    });

    it('should not append status param when filter is all', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ tasks: [] }),
      });

      await getAssignedTasks('all');

      const fetchCall = global.fetch.mock.calls[0][0];
      expect(fetchCall).not.toContain('status=');
    });

    it('should throw error on failed fetch', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ message: 'Unauthorized' }),
      });

      await expect(getAssignedTasks()).rejects.toThrow('Unauthorized');
    });

    it('should throw default error message when no message provided', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({}),
      });

      await expect(getAssignedTasks()).rejects.toThrow(
        'Failed to fetch assigned tasks'
      );
    });

    it('should handle response without tasks wrapper', async () => {
      const mockTasks = [
        { id: '1', title: 'Task 1', status: 'ASSIGNED' },
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTasks),
      });

      const result = await getAssignedTasks();

      expect(result).toEqual(mockTasks);
    });
  });

  describe('getTaskById', () => {
    it('should fetch task by ID successfully', async () => {
      const mockTask = { id: '123', title: 'Test Task' };

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockTask),
      });

      const result = await getTaskById('123');

      expect(result).toEqual(mockTask);
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/tasks/123'),
        expect.objectContaining({
          method: 'GET',
        })
      );
    });

    it('should throw error on failed fetch', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ message: 'Task not found' }),
      });

      await expect(getTaskById('999')).rejects.toThrow('Task not found');
    });

    it('should throw default error message when no message provided', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({}),
      });

      await expect(getTaskById('999')).rejects.toThrow('Failed to fetch task');
    });
  });

  describe('updateTaskStatus', () => {
    it('should update task status successfully', async () => {
      const mockResponse = { id: '123', status: 'IN_PROGRESS' };

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      const result = await updateTaskStatus('123', 'IN_PROGRESS', 'Started work');

      expect(result).toEqual(mockResponse);
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/tasks/123/status'),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ status: 'IN_PROGRESS', notes: 'Started work' }),
        })
      );
    });

    it('should update status without notes', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ id: '123', status: 'IN_PROGRESS' }),
      });

      await updateTaskStatus('123', 'IN_PROGRESS');

      expect(global.fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          body: JSON.stringify({ status: 'IN_PROGRESS', notes: '' }),
        })
      );
    });

    it('should throw error on failed update', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ message: 'Update failed' }),
      });

      await expect(updateTaskStatus('123', 'COMPLETED')).rejects.toThrow(
        'Update failed'
      );
    });

    it('should throw default error message when no message provided', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({}),
      });

      await expect(updateTaskStatus('123', 'COMPLETED')).rejects.toThrow(
        'Failed to update task status'
      );
    });
  });

  describe('completeTask', () => {
    it('should complete task successfully', async () => {
      const mockResponse = { id: '123', status: 'COMPLETED' };

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      const result = await completeTask('123', 'Work completed successfully');

      expect(result).toEqual(mockResponse);
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/tasks/123/complete'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ completionSummary: 'Work completed successfully' }),
        })
      );
    });

    it('should throw error on failed completion', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ message: 'Completion failed' }),
      });

      await expect(completeTask('123', 'Summary')).rejects.toThrow(
        'Completion failed'
      );
    });

    it('should throw default error message when no message provided', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({}),
      });

      await expect(completeTask('123', 'Summary')).rejects.toThrow(
        'Failed to complete task'
      );
    });
  });

  describe('updateLocation', () => {
    it('should update location successfully', async () => {
      const mockResponse = { success: true };

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      const result = await updateLocation(37.7749, -122.4194);

      expect(result).toEqual(mockResponse);
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/technicians/location'),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ latitude: 37.7749, longitude: -122.4194 }),
        })
      );
    });

    it('should throw error on failed update', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ message: 'Location update failed' }),
      });

      await expect(updateLocation(0, 0)).rejects.toThrow(
        'Location update failed'
      );
    });

    it('should throw default error message when no message provided', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({}),
      });

      await expect(updateLocation(0, 0)).rejects.toThrow(
        'Failed to update location'
      );
    });
  });
});
