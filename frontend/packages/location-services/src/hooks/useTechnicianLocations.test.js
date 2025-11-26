/**
 * Tests for useTechnicianLocations hook
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useTechnicianLocations } from './useTechnicianLocations';
import * as technicianService from '../services/technicianService';

// Mock the service
vi.mock('../services/technicianService', () => ({
  fetchTechnicianLocations: vi.fn(),
}));

describe('useTechnicianLocations', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('initial load', () => {
    it('loads technicians on mount', async () => {
      const mockTechnicians = [
        {
          technicianId: 1,
          name: 'John Doe',
          status: 'available',
          latitude: 37.7749,
          longitude: -122.4194,
        },
      ];

      technicianService.fetchTechnicianLocations.mockResolvedValueOnce(mockTechnicians);

      const { result } = renderHook(() => useTechnicianLocations());

      expect(result.current.loading).toBe(true);
      expect(result.current.technicians).toEqual([]);
      expect(result.current.error).toBeNull();

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.technicians).toEqual(mockTechnicians);
      expect(result.current.error).toBeNull();
      expect(result.current.lastUpdated).toBeInstanceOf(Date);
    });

    it('handles empty technician list', async () => {
      technicianService.fetchTechnicianLocations.mockResolvedValueOnce([]);

      const { result } = renderHook(() => useTechnicianLocations());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.technicians).toEqual([]);
      expect(result.current.error).toBeNull();
    });

    it('handles API errors', async () => {
      const errorMessage = 'Failed to fetch technician locations: 500 Internal Server Error';
      technicianService.fetchTechnicianLocations.mockRejectedValueOnce(
        new Error(errorMessage)
      );

      const { result } = renderHook(() => useTechnicianLocations());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.technicians).toEqual([]);
      expect(result.current.error).toBe(errorMessage);
    });

    it('handles error without message', async () => {
      technicianService.fetchTechnicianLocations.mockRejectedValueOnce({});

      const { result } = renderHook(() => useTechnicianLocations());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.error).toBe('Failed to load technician locations');
    });
  });

  describe('refresh functionality', () => {
    it('refreshes technicians when refresh is called', async () => {
      const initialTechnicians = [
        { technicianId: 1, name: 'John Doe', status: 'available' },
      ];
      const updatedTechnicians = [
        { technicianId: 1, name: 'John Doe', status: 'busy' },
        { technicianId: 2, name: 'Jane Smith', status: 'available' },
      ];

      technicianService.fetchTechnicianLocations
        .mockResolvedValueOnce(initialTechnicians)
        .mockResolvedValueOnce(updatedTechnicians);

      const { result } = renderHook(() => useTechnicianLocations());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.technicians).toEqual(initialTechnicians);

      // Call refresh
      await act(async () => {
        result.current.refresh();
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.technicians).toEqual(updatedTechnicians);
      expect(technicianService.fetchTechnicianLocations).toHaveBeenCalledTimes(2);
    });

    it('clears error on successful refresh', async () => {
      technicianService.fetchTechnicianLocations
        .mockRejectedValueOnce(new Error('Initial error'))
        .mockResolvedValueOnce([{ technicianId: 1, name: 'John Doe' }]);

      const { result } = renderHook(() => useTechnicianLocations());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.error).toBeTruthy();

      await act(async () => {
        result.current.refresh();
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.error).toBeNull();
      expect(result.current.technicians).toHaveLength(1);
    });
  });

  describe('auto-refresh', () => {
    beforeEach(() => {
      vi.useFakeTimers({ shouldAdvanceTime: true });
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('auto-refreshes at specified interval', async () => {
      const mockTechnicians = [{ technicianId: 1, name: 'John Doe' }];
      technicianService.fetchTechnicianLocations.mockResolvedValue(mockTechnicians);

      const refreshInterval = 30000; // 30 seconds
      renderHook(() => useTechnicianLocations({ refreshInterval }));

      await vi.waitFor(() => {
        expect(technicianService.fetchTechnicianLocations).toHaveBeenCalledTimes(1);
      });

      // Advance time by refresh interval
      await act(async () => {
        vi.advanceTimersByTime(refreshInterval);
      });

      await vi.waitFor(() => {
        expect(technicianService.fetchTechnicianLocations).toHaveBeenCalledTimes(2);
      });

      // Advance time again
      await act(async () => {
        vi.advanceTimersByTime(refreshInterval);
      });

      await vi.waitFor(() => {
        expect(technicianService.fetchTechnicianLocations).toHaveBeenCalledTimes(3);
      });
    });

    it('does not auto-refresh when interval is 0', async () => {
      const mockTechnicians = [{ technicianId: 1, name: 'John Doe' }];
      technicianService.fetchTechnicianLocations.mockResolvedValue(mockTechnicians);

      renderHook(() => useTechnicianLocations({ refreshInterval: 0 }));

      await vi.waitFor(() => {
        expect(technicianService.fetchTechnicianLocations).toHaveBeenCalledTimes(1);
      });

      // Advance time
      await act(async () => {
        vi.advanceTimersByTime(60000);
      });

      // Should still only be called once (initial load)
      expect(technicianService.fetchTechnicianLocations).toHaveBeenCalledTimes(1);
    });

    it('clears interval on unmount', async () => {
      const mockTechnicians = [{ technicianId: 1, name: 'John Doe' }];
      technicianService.fetchTechnicianLocations.mockResolvedValue(mockTechnicians);

      const { unmount } = renderHook(() =>
        useTechnicianLocations({ refreshInterval: 30000 })
      );

      await vi.waitFor(() => {
        expect(technicianService.fetchTechnicianLocations).toHaveBeenCalledTimes(1);
      });

      unmount();

      // Advance time after unmount
      await act(async () => {
        vi.advanceTimersByTime(60000);
      });

      // Should not call again after unmount
      expect(technicianService.fetchTechnicianLocations).toHaveBeenCalledTimes(1);
    });
  });

  describe('lastUpdated timestamp', () => {
    it('updates lastUpdated on successful load', async () => {
      const mockTechnicians = [{ technicianId: 1, name: 'John Doe' }];
      technicianService.fetchTechnicianLocations.mockResolvedValue(mockTechnicians);

      const { result } = renderHook(() => useTechnicianLocations());

      expect(result.current.lastUpdated).toBeNull();

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.lastUpdated).toBeInstanceOf(Date);
    });

    it('does not update lastUpdated on error', async () => {
      technicianService.fetchTechnicianLocations.mockRejectedValue(
        new Error('API Error')
      );

      const { result } = renderHook(() => useTechnicianLocations());

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.lastUpdated).toBeNull();
    });
  });
});
