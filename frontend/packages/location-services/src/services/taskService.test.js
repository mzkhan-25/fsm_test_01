import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  TaskPriority,
  TaskStatus,
  fetchUnassignedTasks,
  getPriorityColor,
  getPriorityLabel,
  getPriorityTextColor,
} from './taskService';

describe('taskService', () => {
  describe('TaskPriority', () => {
    it('has HIGH priority', () => {
      expect(TaskPriority.HIGH).toBe('HIGH');
    });

    it('has MEDIUM priority', () => {
      expect(TaskPriority.MEDIUM).toBe('MEDIUM');
    });

    it('has LOW priority', () => {
      expect(TaskPriority.LOW).toBe('LOW');
    });
  });

  describe('TaskStatus', () => {
    it('has UNASSIGNED status', () => {
      expect(TaskStatus.UNASSIGNED).toBe('UNASSIGNED');
    });

    it('has ASSIGNED status', () => {
      expect(TaskStatus.ASSIGNED).toBe('ASSIGNED');
    });

    it('has IN_PROGRESS status', () => {
      expect(TaskStatus.IN_PROGRESS).toBe('IN_PROGRESS');
    });

    it('has COMPLETED status', () => {
      expect(TaskStatus.COMPLETED).toBe('COMPLETED');
    });
  });

  describe('getPriorityColor', () => {
    it('returns red for HIGH priority', () => {
      expect(getPriorityColor(TaskPriority.HIGH)).toBe('#dc3545');
    });

    it('returns orange for MEDIUM priority', () => {
      expect(getPriorityColor(TaskPriority.MEDIUM)).toBe('#fd7e14');
    });

    it('returns yellow for LOW priority', () => {
      expect(getPriorityColor(TaskPriority.LOW)).toBe('#ffc107');
    });

    it('returns gray for unknown priority', () => {
      expect(getPriorityColor('UNKNOWN')).toBe('#6c757d');
    });

    it('returns gray for undefined priority', () => {
      expect(getPriorityColor(undefined)).toBe('#6c757d');
    });

    it('returns gray for null priority', () => {
      expect(getPriorityColor(null)).toBe('#6c757d');
    });
  });

  describe('getPriorityTextColor', () => {
    it('returns black for LOW priority', () => {
      expect(getPriorityTextColor(TaskPriority.LOW)).toBe('#000000');
    });

    it('returns white for HIGH priority', () => {
      expect(getPriorityTextColor(TaskPriority.HIGH)).toBe('#ffffff');
    });

    it('returns white for MEDIUM priority', () => {
      expect(getPriorityTextColor(TaskPriority.MEDIUM)).toBe('#ffffff');
    });

    it('returns white for unknown priority', () => {
      expect(getPriorityTextColor('UNKNOWN')).toBe('#ffffff');
    });

    it('returns white for undefined priority', () => {
      expect(getPriorityTextColor(undefined)).toBe('#ffffff');
    });
  });

  describe('getPriorityLabel', () => {
    it('returns "High" for HIGH priority', () => {
      expect(getPriorityLabel(TaskPriority.HIGH)).toBe('High');
    });

    it('returns "Medium" for MEDIUM priority', () => {
      expect(getPriorityLabel(TaskPriority.MEDIUM)).toBe('Medium');
    });

    it('returns "Low" for LOW priority', () => {
      expect(getPriorityLabel(TaskPriority.LOW)).toBe('Low');
    });

    it('returns "Unknown" for undefined priority', () => {
      expect(getPriorityLabel(undefined)).toBe('Unknown');
    });

    it('returns "Unknown" for null priority', () => {
      expect(getPriorityLabel(null)).toBe('Unknown');
    });

    it('returns "Unknown" for invalid priority', () => {
      expect(getPriorityLabel('INVALID')).toBe('Unknown');
    });
  });

  describe('fetchUnassignedTasks', () => {
    let originalFetch;

    beforeEach(() => {
      originalFetch = global.fetch;
    });

    afterEach(() => {
      global.fetch = originalFetch;
    });

    it('fetches tasks with default pagination', async () => {
      const mockResponse = {
        tasks: [{ id: 1, title: 'Test Task' }],
        page: 0,
        pageSize: 100,
      };

      global.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      const result = await fetchUnassignedTasks();

      expect(global.fetch).toHaveBeenCalledTimes(1);
      const calledUrl = global.fetch.mock.calls[0][0];
      expect(calledUrl).toContain('status=UNASSIGNED');
      expect(calledUrl).toContain('page=0');
      expect(calledUrl).toContain('pageSize=100');
      expect(result).toEqual(mockResponse);
    });

    it('fetches tasks with custom pagination', async () => {
      const mockResponse = { tasks: [], page: 1, pageSize: 50 };

      global.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      await fetchUnassignedTasks({ page: 1, pageSize: 50 });

      const calledUrl = global.fetch.mock.calls[0][0];
      expect(calledUrl).toContain('page=1');
      expect(calledUrl).toContain('pageSize=50');
    });

    it('throws error on fetch failure', async () => {
      global.fetch = vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
      });

      await expect(fetchUnassignedTasks()).rejects.toThrow(
        'Failed to fetch tasks: 500 Internal Server Error'
      );
    });

    it('throws error on network failure', async () => {
      global.fetch = vi.fn().mockRejectedValue(new Error('Network error'));

      await expect(fetchUnassignedTasks()).rejects.toThrow('Network error');
    });
  });
});
