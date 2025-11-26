import { describe, it, expect } from 'vitest';
import {
  DEFAULT_CENTER,
  geocodeAddress,
  isValidCoordinates,
  processTasksWithCoordinates,
} from './geocodeService';

describe('geocodeService', () => {
  describe('DEFAULT_CENTER', () => {
    it('is San Francisco coordinates', () => {
      expect(DEFAULT_CENTER).toEqual({
        lat: 37.7749,
        lng: -122.4194,
      });
    });
  });

  describe('geocodeAddress', () => {
    it('returns coordinates for valid address', () => {
      const coords = geocodeAddress('123 Main St, Springfield, IL');
      
      expect(coords).toBeDefined();
      expect(coords.lat).toBeCloseTo(DEFAULT_CENTER.lat, 0);
      expect(coords.lng).toBeCloseTo(DEFAULT_CENTER.lng, 0);
    });

    it('returns null for empty string', () => {
      expect(geocodeAddress('')).toBeNull();
    });

    it('returns null for whitespace only', () => {
      expect(geocodeAddress('   ')).toBeNull();
    });

    it('returns null for null input', () => {
      expect(geocodeAddress(null)).toBeNull();
    });

    it('returns null for undefined input', () => {
      expect(geocodeAddress(undefined)).toBeNull();
    });

    it('returns null for non-string input', () => {
      expect(geocodeAddress(123)).toBeNull();
      expect(geocodeAddress({})).toBeNull();
      expect(geocodeAddress([])).toBeNull();
    });

    it('returns consistent coordinates for same address', () => {
      const address = '456 Oak Ave, Chicago, IL';
      const coords1 = geocodeAddress(address);
      const coords2 = geocodeAddress(address);
      
      expect(coords1).toEqual(coords2);
    });

    it('returns different coordinates for different addresses', () => {
      const coords1 = geocodeAddress('123 Main St, NYC');
      const coords2 = geocodeAddress('456 Broadway, LA');
      
      expect(coords1).not.toEqual(coords2);
    });
  });

  describe('isValidCoordinates', () => {
    it('returns true for valid coordinates', () => {
      expect(isValidCoordinates({ lat: 37.7749, lng: -122.4194 })).toBe(true);
    });

    it('returns true for coordinates at origin', () => {
      expect(isValidCoordinates({ lat: 0, lng: 0 })).toBe(true);
    });

    it('returns true for extreme valid coordinates', () => {
      expect(isValidCoordinates({ lat: 90, lng: 180 })).toBe(true);
      expect(isValidCoordinates({ lat: -90, lng: -180 })).toBe(true);
    });

    it('returns false for null', () => {
      expect(isValidCoordinates(null)).toBe(false);
    });

    it('returns false for undefined', () => {
      expect(isValidCoordinates(undefined)).toBe(false);
    });

    it('returns false for non-object', () => {
      expect(isValidCoordinates('string')).toBe(false);
      expect(isValidCoordinates(123)).toBe(false);
    });

    it('returns false for missing lat', () => {
      expect(isValidCoordinates({ lng: -122.4194 })).toBe(false);
    });

    it('returns false for missing lng', () => {
      expect(isValidCoordinates({ lat: 37.7749 })).toBe(false);
    });

    it('returns false for NaN lat', () => {
      expect(isValidCoordinates({ lat: NaN, lng: -122.4194 })).toBe(false);
    });

    it('returns false for NaN lng', () => {
      expect(isValidCoordinates({ lat: 37.7749, lng: NaN })).toBe(false);
    });

    it('returns false for lat out of range', () => {
      expect(isValidCoordinates({ lat: 91, lng: 0 })).toBe(false);
      expect(isValidCoordinates({ lat: -91, lng: 0 })).toBe(false);
    });

    it('returns false for lng out of range', () => {
      expect(isValidCoordinates({ lat: 0, lng: 181 })).toBe(false);
      expect(isValidCoordinates({ lat: 0, lng: -181 })).toBe(false);
    });
  });

  describe('processTasksWithCoordinates', () => {
    it('processes tasks with valid addresses', () => {
      const tasks = [
        { id: 1, title: 'Task 1', clientAddress: '123 Main St' },
        { id: 2, title: 'Task 2', clientAddress: '456 Oak Ave' },
      ];

      const result = processTasksWithCoordinates(tasks);

      expect(result).toHaveLength(2);
      expect(result[0].id).toBe(1);
      expect(result[0].coordinates).toBeDefined();
      expect(result[1].id).toBe(2);
      expect(result[1].coordinates).toBeDefined();
    });

    it('filters out tasks with empty addresses', () => {
      const tasks = [
        { id: 1, title: 'Task 1', clientAddress: '123 Main St' },
        { id: 2, title: 'Task 2', clientAddress: '' },
        { id: 3, title: 'Task 3', clientAddress: null },
      ];

      const result = processTasksWithCoordinates(tasks);

      expect(result).toHaveLength(1);
      expect(result[0].id).toBe(1);
    });

    it('returns empty array for empty input', () => {
      expect(processTasksWithCoordinates([])).toEqual([]);
    });

    it('returns empty array for null input', () => {
      expect(processTasksWithCoordinates(null)).toEqual([]);
    });

    it('returns empty array for undefined input', () => {
      expect(processTasksWithCoordinates(undefined)).toEqual([]);
    });

    it('returns empty array for non-array input', () => {
      expect(processTasksWithCoordinates('string')).toEqual([]);
      expect(processTasksWithCoordinates(123)).toEqual([]);
      expect(processTasksWithCoordinates({})).toEqual([]);
    });

    it('preserves original task properties', () => {
      const tasks = [
        { id: 1, title: 'Task 1', clientAddress: '123 Main St', priority: 'HIGH' },
      ];

      const result = processTasksWithCoordinates(tasks);

      expect(result[0].id).toBe(1);
      expect(result[0].title).toBe('Task 1');
      expect(result[0].clientAddress).toBe('123 Main St');
      expect(result[0].priority).toBe('HIGH');
      expect(result[0].coordinates).toBeDefined();
    });
  });
});
