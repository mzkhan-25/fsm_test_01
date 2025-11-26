/**
 * Tests for technicianService
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import {
  fetchTechnicianLocations,
  TechnicianStatus,
  getStatusColor,
  getStatusLabel,
} from './technicianService';

describe('technicianService', () => {
  describe('fetchTechnicianLocations', () => {
    beforeEach(() => {
      global.fetch = vi.fn();
    });

    afterEach(() => {
      vi.restoreAllMocks();
    });

    it('fetches technician locations successfully', async () => {
      const mockTechnicians = [
        {
          technicianId: 1,
          name: 'John Doe',
          status: 'available',
          latitude: 37.7749,
          longitude: -122.4194,
          accuracy: 10.5,
          timestamp: '2024-01-01T10:00:00',
          batteryLevel: 85,
        },
        {
          technicianId: 2,
          name: 'Jane Smith',
          status: 'busy',
          latitude: 37.8049,
          longitude: -122.4294,
          accuracy: 15.2,
          timestamp: '2024-01-01T10:05:00',
          batteryLevel: 60,
        },
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTechnicians,
      });

      const result = await fetchTechnicianLocations();

      expect(global.fetch).toHaveBeenCalledWith('/api/technicians/locations');
      expect(result).toEqual(mockTechnicians);
    });

    it('returns array of technicians', async () => {
      const mockTechnicians = [
        { technicianId: 1, name: 'John Doe', status: 'available' },
      ];

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockTechnicians,
      });

      const result = await fetchTechnicianLocations();

      expect(result).toBeInstanceOf(Array);
      expect(result.length).toBe(1);
    });

    it('handles empty response', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      });

      const result = await fetchTechnicianLocations();

      expect(result).toEqual([]);
    });

    it('throws error on failed request', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
      });

      await expect(fetchTechnicianLocations()).rejects.toThrow(
        'Failed to fetch technician locations: 500 Internal Server Error'
      );
    });

    it('throws error on 404', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        statusText: 'Not Found',
      });

      await expect(fetchTechnicianLocations()).rejects.toThrow(
        'Failed to fetch technician locations: 404 Not Found'
      );
    });

    it('throws error on network failure', async () => {
      global.fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(fetchTechnicianLocations()).rejects.toThrow('Network error');
    });
  });

  describe('getStatusColor', () => {
    it('returns green for available status', () => {
      expect(getStatusColor(TechnicianStatus.AVAILABLE)).toBe('#28a745');
    });

    it('returns yellow for busy status', () => {
      expect(getStatusColor(TechnicianStatus.BUSY)).toBe('#ffc107');
    });

    it('returns gray for offline status', () => {
      expect(getStatusColor(TechnicianStatus.OFFLINE)).toBe('#6c757d');
    });

    it('returns gray for unknown status', () => {
      expect(getStatusColor('unknown')).toBe('#6c757d');
    });

    it('returns gray for null status', () => {
      expect(getStatusColor(null)).toBe('#6c757d');
    });

    it('returns gray for undefined status', () => {
      expect(getStatusColor(undefined)).toBe('#6c757d');
    });
  });

  describe('getStatusLabel', () => {
    it('returns "Available" for available status', () => {
      expect(getStatusLabel(TechnicianStatus.AVAILABLE)).toBe('Available');
    });

    it('returns "Busy" for busy status', () => {
      expect(getStatusLabel(TechnicianStatus.BUSY)).toBe('Busy');
    });

    it('returns "Offline" for offline status', () => {
      expect(getStatusLabel(TechnicianStatus.OFFLINE)).toBe('Offline');
    });

    it('returns "Unknown" for unknown status', () => {
      expect(getStatusLabel('unknown')).toBe('Unknown');
    });

    it('returns "Unknown" for null status', () => {
      expect(getStatusLabel(null)).toBe('Unknown');
    });

    it('returns "Unknown" for undefined status', () => {
      expect(getStatusLabel(undefined)).toBe('Unknown');
    });
  });

  describe('TechnicianStatus enum', () => {
    it('has correct status values', () => {
      expect(TechnicianStatus.AVAILABLE).toBe('available');
      expect(TechnicianStatus.BUSY).toBe('busy');
      expect(TechnicianStatus.OFFLINE).toBe('offline');
    });

    it('has all expected status keys', () => {
      const keys = Object.keys(TechnicianStatus);
      expect(keys).toContain('AVAILABLE');
      expect(keys).toContain('BUSY');
      expect(keys).toContain('OFFLINE');
      expect(keys.length).toBe(3);
    });
  });
});
