import { describe, it, expect } from 'vitest';
import L from 'leaflet';
import {
  DEFAULT_CLUSTER_OPTIONS,
  createTaskClusterIndex,
  getClustersForBounds,
  getClusterExpansionZoom,
  getClusterLeaves,
  getClusterHighestPriority,
  createClusterMarkerIcon,
  getClusterSize,
  isCluster,
} from './clusterUtils';
import { TaskPriority } from '../services/taskService';

describe('clusterUtils', () => {
  describe('DEFAULT_CLUSTER_OPTIONS', () => {
    it('has expected default values', () => {
      expect(DEFAULT_CLUSTER_OPTIONS.radius).toBe(60);
      expect(DEFAULT_CLUSTER_OPTIONS.maxZoom).toBe(16);
      expect(DEFAULT_CLUSTER_OPTIONS.minPoints).toBe(2);
    });
  });

  describe('createTaskClusterIndex', () => {
    const mockTasks = [
      {
        id: 1,
        title: 'Task 1',
        description: 'Desc 1',
        clientAddress: '123 Main St',
        priority: TaskPriority.HIGH,
        estimatedDuration: 60,
        coordinates: { lat: 37.7749, lng: -122.4194 },
      },
      {
        id: 2,
        title: 'Task 2',
        description: 'Desc 2',
        clientAddress: '456 Oak Ave',
        priority: TaskPriority.MEDIUM,
        estimatedDuration: 30,
        coordinates: { lat: 37.7750, lng: -122.4195 }, // Very close to task 1
      },
      {
        id: 3,
        title: 'Task 3',
        description: 'Desc 3',
        clientAddress: '789 Pine Rd',
        priority: TaskPriority.LOW,
        estimatedDuration: 45,
        coordinates: { lat: 37.8000, lng: -122.4000 }, // Far from tasks 1 and 2
      },
    ];

    it('creates a supercluster index from tasks', () => {
      const index = createTaskClusterIndex(mockTasks);
      expect(index).toBeDefined();
      expect(typeof index.getClusters).toBe('function');
    });

    it('uses custom options when provided', () => {
      const customOptions = { radius: 100, maxZoom: 14, minPoints: 3 };
      const index = createTaskClusterIndex(mockTasks, customOptions);
      expect(index).toBeDefined();
    });

    it('filters out tasks without coordinates', () => {
      const tasksWithMissing = [
        ...mockTasks,
        { id: 4, title: 'No coords', priority: TaskPriority.LOW },
        { id: 5, title: 'Null coords', priority: TaskPriority.LOW, coordinates: null },
      ];
      const index = createTaskClusterIndex(tasksWithMissing);
      
      // Should only process valid tasks
      const clusters = index.getClusters([-180, -90, 180, 90], 0);
      const totalPoints = clusters.reduce((sum, c) => {
        return sum + (c.properties.cluster ? c.properties.point_count : 1);
      }, 0);
      expect(totalPoints).toBe(3); // Only 3 valid tasks
    });

    it('filters out tasks with invalid coordinate types', () => {
      const tasksWithInvalid = [
        ...mockTasks,
        { id: 4, title: 'String coords', priority: TaskPriority.LOW, coordinates: { lat: 'invalid', lng: -122 } },
      ];
      const index = createTaskClusterIndex(tasksWithInvalid);
      
      const clusters = index.getClusters([-180, -90, 180, 90], 0);
      const totalPoints = clusters.reduce((sum, c) => {
        return sum + (c.properties.cluster ? c.properties.point_count : 1);
      }, 0);
      expect(totalPoints).toBe(3);
    });

    it('filters out null tasks', () => {
      const tasksWithNull = [...mockTasks, null];
      const index = createTaskClusterIndex(tasksWithNull);
      
      const clusters = index.getClusters([-180, -90, 180, 90], 0);
      const totalPoints = clusters.reduce((sum, c) => {
        return sum + (c.properties.cluster ? c.properties.point_count : 1);
      }, 0);
      expect(totalPoints).toBe(3);
    });

    it('stores task properties in GeoJSON features', () => {
      const index = createTaskClusterIndex([mockTasks[0]]);
      const clusters = index.getClusters([-180, -90, 180, 90], 20); // High zoom, no clustering
      
      expect(clusters[0].properties.id).toBe(1);
      expect(clusters[0].properties.title).toBe('Task 1');
      expect(clusters[0].properties.task).toEqual(mockTasks[0]);
    });
  });

  describe('getClustersForBounds', () => {
    const mockTasks = [
      { id: 1, priority: TaskPriority.HIGH, coordinates: { lat: 37.7749, lng: -122.4194 } },
      { id: 2, priority: TaskPriority.MEDIUM, coordinates: { lat: 37.7750, lng: -122.4195 } },
    ];

    it('returns clusters for given bounds and zoom', () => {
      const index = createTaskClusterIndex(mockTasks);
      const bounds = { west: -123, south: 37, east: -122, north: 38 };
      const clusters = getClustersForBounds(index, bounds, 10);
      
      expect(Array.isArray(clusters)).toBe(true);
    });

    it('returns empty array when index is null', () => {
      const bounds = { west: -123, south: 37, east: -122, north: 38 };
      const clusters = getClustersForBounds(null, bounds, 10);
      
      expect(clusters).toEqual([]);
    });

    it('returns individual points at high zoom levels', () => {
      const index = createTaskClusterIndex(mockTasks);
      const bounds = { west: -123, south: 37, east: -122, north: 38 };
      const clusters = getClustersForBounds(index, bounds, 20);
      
      // At high zoom, should be individual points
      expect(clusters.length).toBe(2);
      clusters.forEach(c => {
        expect(c.properties.cluster).toBeFalsy();
      });
    });
  });

  describe('getClusterExpansionZoom', () => {
    it('returns expansion zoom for a cluster', () => {
      const mockTasks = [
        { id: 1, priority: TaskPriority.HIGH, coordinates: { lat: 37.7749, lng: -122.4194 } },
        { id: 2, priority: TaskPriority.MEDIUM, coordinates: { lat: 37.7750, lng: -122.4195 } },
      ];
      const index = createTaskClusterIndex(mockTasks);
      const clusters = getClustersForBounds(
        index,
        { west: -123, south: 37, east: -122, north: 38 },
        1 // Low zoom to get clusters
      );
      
      // Find a cluster
      const cluster = clusters.find(c => c.properties.cluster);
      if (cluster) {
        const expansionZoom = getClusterExpansionZoom(index, cluster.id);
        expect(typeof expansionZoom).toBe('number');
        expect(expansionZoom).toBeGreaterThan(1);
      }
    });

    it('returns 0 when index is null', () => {
      const zoom = getClusterExpansionZoom(null, 123);
      expect(zoom).toBe(0);
    });
  });

  describe('getClusterLeaves', () => {
    const mockTasks = [
      { id: 1, priority: TaskPriority.HIGH, coordinates: { lat: 37.7749, lng: -122.4194 } },
      { id: 2, priority: TaskPriority.MEDIUM, coordinates: { lat: 37.7750, lng: -122.4195 } },
      { id: 3, priority: TaskPriority.LOW, coordinates: { lat: 37.7751, lng: -122.4196 } },
    ];

    it('returns leaves for a cluster', () => {
      const index = createTaskClusterIndex(mockTasks);
      const clusters = getClustersForBounds(
        index,
        { west: -123, south: 37, east: -122, north: 38 },
        1 // Low zoom to get clusters
      );
      
      const cluster = clusters.find(c => c.properties.cluster);
      if (cluster) {
        const leaves = getClusterLeaves(index, cluster.id);
        expect(Array.isArray(leaves)).toBe(true);
        expect(leaves.length).toBeGreaterThan(0);
      }
    });

    it('returns empty array when index is null', () => {
      const leaves = getClusterLeaves(null, 123);
      expect(leaves).toEqual([]);
    });

    it('respects limit parameter', () => {
      const index = createTaskClusterIndex(mockTasks);
      const clusters = getClustersForBounds(
        index,
        { west: -123, south: 37, east: -122, north: 38 },
        1
      );
      
      const cluster = clusters.find(c => c.properties.cluster);
      if (cluster) {
        const leaves = getClusterLeaves(index, cluster.id, 1);
        expect(leaves.length).toBeLessThanOrEqual(1);
      }
    });
  });

  describe('getClusterHighestPriority', () => {
    it('returns cluster priority from properties', () => {
      const cluster = {
        properties: {
          cluster: true,
          priority: TaskPriority.HIGH,
        },
      };
      expect(getClusterHighestPriority(cluster)).toBe(TaskPriority.HIGH);
    });

    it('returns LOW for cluster without priority', () => {
      const cluster = {
        properties: {
          cluster: true,
        },
      };
      expect(getClusterHighestPriority(cluster)).toBe(TaskPriority.LOW);
    });

    it('returns LOW for null cluster', () => {
      expect(getClusterHighestPriority(null)).toBe(TaskPriority.LOW);
    });

    it('returns LOW for cluster without properties', () => {
      expect(getClusterHighestPriority({})).toBe(TaskPriority.LOW);
    });
  });

  describe('createClusterMarkerIcon', () => {
    it('creates a Leaflet DivIcon', () => {
      const icon = createClusterMarkerIcon(5, TaskPriority.HIGH);
      expect(icon).toBeInstanceOf(L.DivIcon);
    });

    it('includes task count in HTML', () => {
      const icon = createClusterMarkerIcon(10, TaskPriority.MEDIUM);
      expect(icon.options.html).toContain('10');
    });

    it('uses correct size for small counts', () => {
      const icon = createClusterMarkerIcon(5, TaskPriority.LOW);
      expect(icon.options.iconSize).toEqual([36, 36]);
    });

    it('uses correct size for medium counts', () => {
      const icon = createClusterMarkerIcon(25, TaskPriority.LOW);
      expect(icon.options.iconSize).toEqual([44, 44]);
    });

    it('uses correct size for large counts', () => {
      const icon = createClusterMarkerIcon(75, TaskPriority.LOW);
      expect(icon.options.iconSize).toEqual([52, 52]);
    });

    it('uses correct size for very large counts', () => {
      const icon = createClusterMarkerIcon(150, TaskPriority.LOW);
      expect(icon.options.iconSize).toEqual([60, 60]);
    });

    it('applies correct priority color', () => {
      const icon = createClusterMarkerIcon(5, TaskPriority.HIGH);
      expect(icon.options.html).toContain('#dc3545'); // Red for HIGH
    });

    it('centers icon anchor', () => {
      const icon = createClusterMarkerIcon(5, TaskPriority.HIGH);
      const size = icon.options.iconSize[0];
      expect(icon.options.iconAnchor).toEqual([size / 2, size / 2]);
    });
  });

  describe('getClusterSize', () => {
    it('returns 36 for counts less than 10', () => {
      expect(getClusterSize(1)).toBe(36);
      expect(getClusterSize(5)).toBe(36);
      expect(getClusterSize(9)).toBe(36);
    });

    it('returns 44 for counts 10-49', () => {
      expect(getClusterSize(10)).toBe(44);
      expect(getClusterSize(25)).toBe(44);
      expect(getClusterSize(49)).toBe(44);
    });

    it('returns 52 for counts 50-99', () => {
      expect(getClusterSize(50)).toBe(52);
      expect(getClusterSize(75)).toBe(52);
      expect(getClusterSize(99)).toBe(52);
    });

    it('returns 60 for counts 100+', () => {
      expect(getClusterSize(100)).toBe(60);
      expect(getClusterSize(500)).toBe(60);
      expect(getClusterSize(1000)).toBe(60);
    });
  });

  describe('isCluster', () => {
    it('returns true for cluster features', () => {
      const cluster = {
        properties: {
          cluster: true,
          point_count: 5,
        },
      };
      expect(isCluster(cluster)).toBe(true);
    });

    it('returns false for individual point features', () => {
      const point = {
        properties: {
          id: 1,
          title: 'Task 1',
        },
      };
      expect(isCluster(point)).toBe(false);
    });

    it('returns false for null', () => {
      expect(isCluster(null)).toBe(false);
    });

    it('returns false for undefined', () => {
      expect(isCluster(undefined)).toBe(false);
    });

    it('returns false for feature without properties', () => {
      expect(isCluster({})).toBe(false);
    });

    it('returns false when cluster property is false', () => {
      const feature = {
        properties: {
          cluster: false,
        },
      };
      expect(isCluster(feature)).toBe(false);
    });
  });

  describe('Priority reduction in clusters', () => {
    it('keeps highest priority when clustering', () => {
      const mockTasks = [
        { id: 1, priority: TaskPriority.LOW, coordinates: { lat: 37.7749, lng: -122.4194 } },
        { id: 2, priority: TaskPriority.HIGH, coordinates: { lat: 37.7750, lng: -122.4195 } },
        { id: 3, priority: TaskPriority.MEDIUM, coordinates: { lat: 37.7751, lng: -122.4196 } },
      ];
      
      const index = createTaskClusterIndex(mockTasks);
      const clusters = getClustersForBounds(
        index,
        { west: -123, south: 37, east: -122, north: 38 },
        1 // Low zoom to force clustering
      );
      
      const cluster = clusters.find(c => c.properties.cluster);
      if (cluster) {
        // Should have HIGH priority as it's the highest among clustered tasks
        expect(cluster.properties.priority).toBe(TaskPriority.HIGH);
      }
    });
  });
});
