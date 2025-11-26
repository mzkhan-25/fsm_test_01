import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { assignTaskToTechnician } from './assignmentService';

describe('assignmentService', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
  });

  describe('assignTaskToTechnician', () => {
    it('makes POST request to assign endpoint', async () => {
      const mockResponse = { id: 1, status: 'ASSIGNED', technicianId: 2 };
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      await assignTaskToTechnician(1, 2);

      expect(fetch).toHaveBeenCalledTimes(1);
      const [url, options] = fetch.mock.calls[0];
      expect(url).toContain('/tasks/1/assign');
      expect(options.method).toBe('POST');
    });

    it('sends technicianId in request body', async () => {
      const mockResponse = { id: 1, status: 'ASSIGNED', technicianId: 2 };
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      await assignTaskToTechnician(1, 2);

      const [, options] = fetch.mock.calls[0];
      const body = JSON.parse(options.body);
      expect(body).toEqual({ technicianId: 2 });
    });

    it('includes Content-Type header', async () => {
      const mockResponse = { id: 1, status: 'ASSIGNED' };
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      await assignTaskToTechnician(1, 2);

      const [, options] = fetch.mock.calls[0];
      expect(options.headers['Content-Type']).toBe('application/json');
    });

    it('includes Authorization header when token exists', async () => {
      localStorage.setItem('token', 'test-token');
      const mockResponse = { id: 1, status: 'ASSIGNED' };
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      await assignTaskToTechnician(1, 2);

      const [, options] = fetch.mock.calls[0];
      expect(options.headers['Authorization']).toBe('Bearer test-token');
    });

    it('does not include Authorization header when token does not exist', async () => {
      const mockResponse = { id: 1, status: 'ASSIGNED' };
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      await assignTaskToTechnician(1, 2);

      const [, options] = fetch.mock.calls[0];
      expect(options.headers['Authorization']).toBeUndefined();
    });

    it('returns response data on success', async () => {
      const mockResponse = { id: 1, status: 'ASSIGNED', technicianId: 2 };
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      const result = await assignTaskToTechnician(1, 2);

      expect(result).toEqual(mockResponse);
    });

    it('throws error with message from API on failure', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ message: 'Task not found' }),
      });

      await expect(assignTaskToTechnician(999, 2)).rejects.toThrow('Task not found');
    });

    it('throws default error message when API error has no message', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({}),
      });

      await expect(assignTaskToTechnician(1, 2)).rejects.toThrow('Failed to assign task');
    });

    it('throws error with status text when JSON parsing fails', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
        json: () => Promise.reject(new Error('Invalid JSON')),
      });

      await expect(assignTaskToTechnician(1, 2)).rejects.toThrow('500 Internal Server Error');
    });

    it('handles trailing slash in API_BASE_URL', async () => {
      // The service handles trailing slashes in the URL
      const mockResponse = { id: 1, status: 'ASSIGNED' };
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      await assignTaskToTechnician(1, 2);

      const [url] = fetch.mock.calls[0];
      expect(url).not.toContain('//tasks');
    });
  });
});
