import { describe, it, expect } from 'vitest';
import {
  degreesToRadians,
  calculateHaversineDistance,
  calculateTaskToTechnicianDistance,
  getTechniciansWithDistanceFromTask,
  formatDistance,
  getNearbyTechnicians,
  DEFAULT_NEARBY_RADIUS_KM,
} from './distanceUtils';

describe('distanceUtils', () => {
  describe('degreesToRadians', () => {
    it('converts 0 degrees to 0 radians', () => {
      expect(degreesToRadians(0)).toBe(0);
    });

    it('converts 180 degrees to PI radians', () => {
      expect(degreesToRadians(180)).toBeCloseTo(Math.PI, 10);
    });

    it('converts 90 degrees to PI/2 radians', () => {
      expect(degreesToRadians(90)).toBeCloseTo(Math.PI / 2, 10);
    });

    it('converts 360 degrees to 2*PI radians', () => {
      expect(degreesToRadians(360)).toBeCloseTo(2 * Math.PI, 10);
    });

    it('converts negative degrees correctly', () => {
      expect(degreesToRadians(-90)).toBeCloseTo(-Math.PI / 2, 10);
    });
  });

  describe('calculateHaversineDistance', () => {
    it('returns 0 for identical coordinates', () => {
      const distance = calculateHaversineDistance(37.7749, -122.4194, 37.7749, -122.4194);
      expect(distance).toBe(0);
    });

    it('calculates distance between San Francisco and Los Angeles correctly in km', () => {
      // SF: 37.7749, -122.4194
      // LA: 34.0522, -118.2437
      // Actual distance is approximately 559 km
      const distance = calculateHaversineDistance(37.7749, -122.4194, 34.0522, -118.2437, 'km');
      expect(distance).toBeGreaterThan(550);
      expect(distance).toBeLessThan(570);
    });

    it('calculates distance in miles when specified', () => {
      // SF to LA should be approximately 347 miles
      const distance = calculateHaversineDistance(37.7749, -122.4194, 34.0522, -118.2437, 'miles');
      expect(distance).toBeGreaterThan(340);
      expect(distance).toBeLessThan(360);
    });

    it('calculates short distances correctly', () => {
      // Two points about 1 km apart
      const distance = calculateHaversineDistance(37.7749, -122.4194, 37.7839, -122.4094, 'km');
      expect(distance).toBeGreaterThan(0.5);
      expect(distance).toBeLessThan(2);
    });

    it('defaults to km when unit is not specified', () => {
      const distanceDefault = calculateHaversineDistance(37.7749, -122.4194, 34.0522, -118.2437);
      const distanceKm = calculateHaversineDistance(37.7749, -122.4194, 34.0522, -118.2437, 'km');
      expect(distanceDefault).toBe(distanceKm);
    });
  });

  describe('calculateTaskToTechnicianDistance', () => {
    const mockTask = {
      id: 1,
      title: 'Test Task',
      coordinates: {
        lat: 37.7749,
        lng: -122.4194,
      },
    };

    const mockTechnician = {
      technicianId: 1,
      name: 'John Doe',
      latitude: 37.7849,
      longitude: -122.4094,
    };

    it('calculates distance between task and technician', () => {
      const distance = calculateTaskToTechnicianDistance(mockTask, mockTechnician);
      expect(distance).toBeGreaterThan(0);
      expect(distance).toBeLessThan(5); // Should be within a few km
    });

    it('returns null if task is null', () => {
      expect(calculateTaskToTechnicianDistance(null, mockTechnician)).toBeNull();
    });

    it('returns null if task has no coordinates', () => {
      expect(calculateTaskToTechnicianDistance({ id: 1 }, mockTechnician)).toBeNull();
    });

    it('returns null if task coordinates are missing lat', () => {
      const task = { coordinates: { lng: -122.4194 } };
      expect(calculateTaskToTechnicianDistance(task, mockTechnician)).toBeNull();
    });

    it('returns null if task coordinates are missing lng', () => {
      const task = { coordinates: { lat: 37.7749 } };
      expect(calculateTaskToTechnicianDistance(task, mockTechnician)).toBeNull();
    });

    it('returns null if technician is null', () => {
      expect(calculateTaskToTechnicianDistance(mockTask, null)).toBeNull();
    });

    it('returns null if technician latitude is null', () => {
      const tech = { ...mockTechnician, latitude: null };
      expect(calculateTaskToTechnicianDistance(mockTask, tech)).toBeNull();
    });

    it('returns null if technician longitude is null', () => {
      const tech = { ...mockTechnician, longitude: null };
      expect(calculateTaskToTechnicianDistance(mockTask, tech)).toBeNull();
    });

    it('returns null if technician latitude is undefined', () => {
      const tech = { technicianId: 1, longitude: -122.4094 };
      expect(calculateTaskToTechnicianDistance(mockTask, tech)).toBeNull();
    });

    it('calculates distance in miles when specified', () => {
      const distanceKm = calculateTaskToTechnicianDistance(mockTask, mockTechnician, 'km');
      const distanceMiles = calculateTaskToTechnicianDistance(mockTask, mockTechnician, 'miles');
      expect(distanceMiles).toBeLessThan(distanceKm);
    });
  });

  describe('getTechniciansWithDistanceFromTask', () => {
    const mockTask = {
      id: 1,
      title: 'Test Task',
      coordinates: {
        lat: 37.7749,
        lng: -122.4194,
      },
    };

    const mockTechnicians = [
      { technicianId: 1, name: 'Far Tech', latitude: 34.0522, longitude: -118.2437 }, // LA - far
      { technicianId: 2, name: 'Close Tech', latitude: 37.7849, longitude: -122.4094 }, // Close
      { technicianId: 3, name: 'Medium Tech', latitude: 37.8749, longitude: -122.2594 }, // Medium
    ];

    it('returns technicians sorted by distance (nearest first)', () => {
      const result = getTechniciansWithDistanceFromTask(mockTask, mockTechnicians);
      expect(result).toHaveLength(3);
      expect(result[0].name).toBe('Close Tech');
      expect(result[2].name).toBe('Far Tech');
    });

    it('adds distance property to each technician', () => {
      const result = getTechniciansWithDistanceFromTask(mockTask, mockTechnicians);
      result.forEach(tech => {
        expect(tech).toHaveProperty('distance');
        expect(typeof tech.distance).toBe('number');
      });
    });

    it('adds distanceUnit property to each technician', () => {
      const result = getTechniciansWithDistanceFromTask(mockTask, mockTechnicians, 'km');
      result.forEach(tech => {
        expect(tech.distanceUnit).toBe('km');
      });
    });

    it('returns empty array if task is null', () => {
      expect(getTechniciansWithDistanceFromTask(null, mockTechnicians)).toEqual([]);
    });

    it('returns empty array if technicians is not an array', () => {
      expect(getTechniciansWithDistanceFromTask(mockTask, null)).toEqual([]);
      expect(getTechniciansWithDistanceFromTask(mockTask, 'invalid')).toEqual([]);
    });

    it('filters out technicians with invalid coordinates', () => {
      const techniciansWithInvalid = [
        ...mockTechnicians,
        { technicianId: 4, name: 'Invalid Tech', latitude: null, longitude: -122.4094 },
      ];
      const result = getTechniciansWithDistanceFromTask(mockTask, techniciansWithInvalid);
      expect(result).toHaveLength(3);
      expect(result.find(t => t.technicianId === 4)).toBeUndefined();
    });

    it('preserves original technician properties', () => {
      const result = getTechniciansWithDistanceFromTask(mockTask, mockTechnicians);
      expect(result[0]).toHaveProperty('technicianId');
      expect(result[0]).toHaveProperty('name');
      expect(result[0]).toHaveProperty('latitude');
      expect(result[0]).toHaveProperty('longitude');
    });
  });

  describe('formatDistance', () => {
    it('formats distance with default 1 decimal place', () => {
      expect(formatDistance(5.678, 'km')).toBe('5.7 km');
    });

    it('formats distance with specified decimal places', () => {
      expect(formatDistance(5.6789, 'km', 2)).toBe('5.68 km');
    });

    it('formats distance in miles', () => {
      expect(formatDistance(10.5, 'miles')).toBe('10.5 miles');
    });

    it('returns N/A for null distance', () => {
      expect(formatDistance(null, 'km')).toBe('N/A');
    });

    it('returns N/A for undefined distance', () => {
      expect(formatDistance(undefined, 'km')).toBe('N/A');
    });

    it('handles zero distance', () => {
      expect(formatDistance(0, 'km')).toBe('0.0 km');
    });

    it('rounds correctly', () => {
      expect(formatDistance(1.96, 'km', 1)).toBe('2.0 km');
    });
  });

  describe('getNearbyTechnicians', () => {
    const mockTask = {
      id: 1,
      title: 'Test Task',
      coordinates: {
        lat: 37.7749,
        lng: -122.4194,
      },
    };

    const mockTechnicians = [
      { technicianId: 1, name: 'Far Tech', latitude: 34.0522, longitude: -118.2437 }, // LA - ~559 km
      { technicianId: 2, name: 'Close Tech', latitude: 37.7849, longitude: -122.4094 }, // ~1.5 km
      { technicianId: 3, name: 'Medium Tech', latitude: 37.8749, longitude: -122.2594 }, // ~18 km
    ];

    it('filters technicians within default radius', () => {
      const result = getNearbyTechnicians(mockTask, mockTechnicians);
      // Only Close Tech should be within default 10km
      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('Close Tech');
    });

    it('filters technicians within custom radius', () => {
      const result = getNearbyTechnicians(mockTask, mockTechnicians, 20);
      // Close Tech and Medium Tech within 20km
      expect(result).toHaveLength(2);
    });

    it('returns all technicians if radius is very large', () => {
      const result = getNearbyTechnicians(mockTask, mockTechnicians, 1000);
      expect(result).toHaveLength(3);
    });

    it('returns empty array if no technicians within radius', () => {
      const result = getNearbyTechnicians(mockTask, mockTechnicians, 0.1);
      expect(result).toHaveLength(0);
    });

    it('returns results sorted by distance', () => {
      const result = getNearbyTechnicians(mockTask, mockTechnicians, 1000);
      expect(result[0].distance).toBeLessThan(result[1].distance);
      expect(result[1].distance).toBeLessThan(result[2].distance);
    });

    it('exports DEFAULT_NEARBY_RADIUS_KM constant', () => {
      expect(DEFAULT_NEARBY_RADIUS_KM).toBe(10);
    });
  });
});
