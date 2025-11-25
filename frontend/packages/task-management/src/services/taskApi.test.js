import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { createTask, getAddressSuggestions, getTechnicians, assignTask } from './taskApi';

// Mock fetch
global.fetch = vi.fn();

describe('taskApi', () => {
  beforeEach(() => {
    localStorage.clear();
    fetch.mockReset();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('createTask', () => {
    const mockTaskData = {
      title: 'Test Task',
      description: 'Test description',
      clientAddress: '123 Main St',
      priority: 'HIGH',
      estimatedDuration: 60,
    };

    const mockTaskResponse = {
      id: 1,
      ...mockTaskData,
      status: 'UNASSIGNED',
      createdAt: '2024-01-01T00:00:00Z',
    };

    it('creates a task successfully', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTaskResponse,
      });

      const result = await createTask(mockTaskData);

      expect(result).toEqual(mockTaskResponse);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/tasks',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(mockTaskData),
        }
      );
    });

    it('includes auth token in headers when available', async () => {
      localStorage.setItem('token', 'test-token-123');
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTaskResponse,
      });

      await createTask(mockTaskData);

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/tasks',
        expect.objectContaining({
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token-123',
          },
        })
      );
    });

    it('throws error when API returns error response', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Validation failed' }),
      });

      await expect(createTask(mockTaskData)).rejects.toThrow('Validation failed');
    });

    it('throws default error message when API error has no message', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({}),
      });

      await expect(createTask(mockTaskData)).rejects.toThrow('Failed to create task');
    });

    it('handles network errors', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(createTask(mockTaskData)).rejects.toThrow('Network error');
    });
  });

  describe('getAddressSuggestions', () => {
    const mockSuggestions = [
      {
        formattedAddress: '123 Main St, Springfield, IL 62701',
        latitude: 39.7817,
        longitude: -89.6501,
        placeId: 'ChIJd8BlQ2BZwokRAFUEcm_qrcA',
      },
      {
        formattedAddress: '123 Main Ave, Chicago, IL 60601',
        latitude: 41.8781,
        longitude: -87.6298,
        placeId: 'ChIJGzE9DS0l1YcRoGqvK_o-5Pk',
      },
    ];

    it('fetches address suggestions successfully', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockSuggestions,
      });

      const result = await getAddressSuggestions('123 Main');

      expect(result).toEqual(mockSuggestions);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/tasks/address-suggestions?partialAddress=123%20Main',
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
    });

    it('includes auth token in headers when available', async () => {
      localStorage.setItem('token', 'test-token-456');
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockSuggestions,
      });

      await getAddressSuggestions('123 Main');

      expect(fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token-456',
          },
        })
      );
    });

    it('returns empty array when query is empty', async () => {
      const result = await getAddressSuggestions('');

      expect(result).toEqual([]);
      expect(fetch).not.toHaveBeenCalled();
    });

    it('returns empty array when query is null', async () => {
      const result = await getAddressSuggestions(null);

      expect(result).toEqual([]);
      expect(fetch).not.toHaveBeenCalled();
    });

    it('returns empty array when query is less than 3 characters', async () => {
      const result = await getAddressSuggestions('12');

      expect(result).toEqual([]);
      expect(fetch).not.toHaveBeenCalled();
    });

    it('fetches when query is exactly 3 characters', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockSuggestions,
      });

      await getAddressSuggestions('123');

      expect(fetch).toHaveBeenCalled();
    });

    it('throws error when API returns error response', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Rate limit exceeded' }),
      });

      await expect(getAddressSuggestions('123 Main')).rejects.toThrow('Rate limit exceeded');
    });

    it('throws default error message when API error has no message', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({}),
      });

      await expect(getAddressSuggestions('123 Main')).rejects.toThrow('Failed to fetch address suggestions');
    });

    it('handles network errors', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(getAddressSuggestions('123 Main')).rejects.toThrow('Network error');
    });

    it('properly encodes special characters in query', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      });

      await getAddressSuggestions('123 Main St & Oak Ave');

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('123%20Main%20St%20%26%20Oak%20Ave'),
        expect.any(Object)
      );
    });
  });

  describe('getTechnicians', () => {
    const mockUsers = [
      { id: 1, name: 'John Doe', email: 'john@example.com', role: 'TECHNICIAN', status: 'ACTIVE' },
      { id: 2, name: 'Jane Smith', email: 'jane@example.com', role: 'TECHNICIAN', status: 'ACTIVE' },
      { id: 3, name: 'Bob Admin', email: 'bob@example.com', role: 'ADMIN', status: 'ACTIVE' },
      { id: 4, name: 'Inactive Tech', email: 'inactive@example.com', role: 'TECHNICIAN', status: 'INACTIVE' },
    ];

    it('fetches technicians successfully and filters by role and status', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockUsers,
      });

      const result = await getTechnicians();

      expect(result).toHaveLength(2);
      expect(result[0].name).toBe('John Doe');
      expect(result[1].name).toBe('Jane Smith');
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/users',
        expect.objectContaining({
          method: 'GET',
        })
      );
    });

    it('includes auth token in headers when available', async () => {
      localStorage.setItem('token', 'test-token-789');
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      });

      await getTechnicians();

      expect(fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token-789',
          },
        })
      );
    });

    it('throws error when API returns error response', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Unauthorized' }),
      });

      await expect(getTechnicians()).rejects.toThrow('Unauthorized');
    });

    it('throws default error message when API error has no message', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({}),
      });

      await expect(getTechnicians()).rejects.toThrow('Failed to fetch technicians');
    });

    it('handles network errors', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(getTechnicians()).rejects.toThrow('Network error');
    });
  });

  describe('assignTask', () => {
    const mockAssignmentResponse = {
      assignmentId: 1,
      taskId: 100,
      technicianId: 1,
      taskStatus: 'ASSIGNED',
      technicianWorkload: 5,
    };

    it('assigns task successfully', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockAssignmentResponse,
      });

      const result = await assignTask(100, 1);

      expect(result).toEqual(mockAssignmentResponse);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/tasks/100/assign',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ technicianId: 1 }),
        }
      );
    });

    it('includes auth token in headers when available', async () => {
      localStorage.setItem('token', 'test-token-assign');
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockAssignmentResponse,
      });

      await assignTask(100, 1);

      expect(fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer test-token-assign',
          },
        })
      );
    });

    it('throws error when API returns error response', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Task already assigned' }),
      });

      await expect(assignTask(100, 1)).rejects.toThrow('Task already assigned');
    });

    it('throws default error message when API error has no message', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({}),
      });

      await expect(assignTask(100, 1)).rejects.toThrow('Failed to assign task');
    });

    it('handles network errors', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(assignTask(100, 1)).rejects.toThrow('Network error');
    });
  });
});
