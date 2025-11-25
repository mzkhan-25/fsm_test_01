import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { createTask, getAddressSuggestions, getTasks } from './taskApi';

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

  describe('getTasks', () => {
    const mockTaskListResponse = {
      tasks: [
        {
          id: 1,
          title: 'Test Task',
          clientAddress: '123 Main St',
          priority: 'HIGH',
          status: 'UNASSIGNED',
          createdAt: '2024-01-01T00:00:00Z',
        },
      ],
      page: 0,
      pageSize: 10,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      statusCounts: {
        UNASSIGNED: 1,
        ASSIGNED: 0,
        IN_PROGRESS: 0,
        COMPLETED: 0,
      },
    };

    it('fetches tasks without parameters', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTaskListResponse,
      });

      const result = await getTasks();

      expect(result).toEqual(mockTaskListResponse);
      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/tasks',
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
    });

    it('fetches tasks with all parameters', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTaskListResponse,
      });

      await getTasks({
        status: 'UNASSIGNED',
        priority: 'HIGH',
        search: 'HVAC',
        sortBy: 'priority',
        sortOrder: 'desc',
        page: 0,
        pageSize: 10,
      });

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/tasks?status=UNASSIGNED&priority=HIGH&search=HVAC&sortBy=priority&sortOrder=desc&page=0&pageSize=10',
        expect.objectContaining({
          method: 'GET',
        })
      );
    });

    it('includes auth token in headers when available', async () => {
      localStorage.setItem('token', 'test-token-789');
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTaskListResponse,
      });

      await getTasks();

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

    it('only includes provided parameters in URL', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTaskListResponse,
      });

      await getTasks({
        status: 'ASSIGNED',
        page: 2,
      });

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/tasks?status=ASSIGNED&page=2',
        expect.any(Object)
      );
    });

    it('throws error when API returns error response', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Unauthorized' }),
      });

      await expect(getTasks()).rejects.toThrow('Unauthorized');
    });

    it('throws default error message when API error has no message', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({}),
      });

      await expect(getTasks()).rejects.toThrow('Failed to fetch tasks');
    });

    it('handles network errors', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(getTasks()).rejects.toThrow('Network error');
    });

    it('properly encodes special characters in search', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTaskListResponse,
      });

      await getTasks({ search: 'HVAC & AC' });

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('search=HVAC+%26+AC'),
        expect.any(Object)
      );
    });

    it('handles page 0 correctly', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTaskListResponse,
      });

      await getTasks({ page: 0 });

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/tasks?page=0',
        expect.any(Object)
      );
    });

    it('handles pageSize 0 correctly', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTaskListResponse,
      });

      await getTasks({ pageSize: 0 });

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8081/api/tasks?pageSize=0',
        expect.any(Object)
      );
    });
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
});
