import Supercluster from 'supercluster';
import L from 'leaflet';
import { TaskPriority, getPriorityColor } from '../services/taskService';

/**
 * Default clustering options
 */
export const DEFAULT_CLUSTER_OPTIONS = {
  radius: 60,
  maxZoom: 16,
  minPoints: 2,
};

/**
 * Priority ranking for determining highest priority in a cluster
 * Lower number = higher priority
 */
const PRIORITY_RANK = {
  [TaskPriority.HIGH]: 1,
  [TaskPriority.MEDIUM]: 2,
  [TaskPriority.LOW]: 3,
};

/**
 * Creates a Supercluster index from task data
 * @param {Array} tasks - Array of task objects with coordinates
 * @param {Object} options - Supercluster options
 * @returns {Supercluster} Configured supercluster instance
 */
export function createTaskClusterIndex(tasks, options = DEFAULT_CLUSTER_OPTIONS) {
  const index = new Supercluster({
    ...options,
    map: (props) => ({
      priority: props.priority,
      priorityRank: PRIORITY_RANK[props.priority] || 4,
    }),
    reduce: (accumulated, props) => {
      // Keep track of highest priority (lowest rank number)
      if (props.priorityRank < accumulated.priorityRank) {
        accumulated.priority = props.priority;
        accumulated.priorityRank = props.priorityRank;
      }
    },
  });

  // Convert tasks to GeoJSON features
  const points = tasks
    .filter(task => task && task.coordinates && 
            typeof task.coordinates.lat === 'number' && 
            typeof task.coordinates.lng === 'number')
    .map(task => ({
      type: 'Feature',
      properties: {
        id: task.id,
        title: task.title,
        description: task.description,
        clientAddress: task.clientAddress,
        priority: task.priority,
        estimatedDuration: task.estimatedDuration,
        task: task, // Store full task for individual markers
      },
      geometry: {
        type: 'Point',
        coordinates: [task.coordinates.lng, task.coordinates.lat], // GeoJSON uses [lng, lat]
      },
    }));

  index.load(points);
  return index;
}

/**
 * Gets clusters and individual points for a given bounds and zoom level
 * @param {Supercluster} index - Supercluster index
 * @param {Object} bounds - Map bounds {west, south, east, north}
 * @param {number} zoom - Current zoom level
 * @returns {Array} Array of clusters and points
 */
export function getClustersForBounds(index, bounds, zoom) {
  if (!index) {
    return [];
  }
  
  return index.getClusters(
    [bounds.west, bounds.south, bounds.east, bounds.north],
    Math.floor(zoom)
  );
}

/**
 * Gets the expansion zoom level for a cluster
 * @param {Supercluster} index - Supercluster index
 * @param {number} clusterId - Cluster ID
 * @returns {number} Zoom level at which cluster expands
 */
export function getClusterExpansionZoom(index, clusterId) {
  if (!index) {
    return 0;
  }
  return index.getClusterExpansionZoom(clusterId);
}

/**
 * Gets the leaves (individual points) within a cluster
 * @param {Supercluster} index - Supercluster index
 * @param {number} clusterId - Cluster ID
 * @param {number} limit - Maximum number of leaves to return
 * @param {number} offset - Offset for pagination
 * @returns {Array} Array of point features
 */
export function getClusterLeaves(index, clusterId, limit = Infinity, offset = 0) {
  if (!index) {
    return [];
  }
  return index.getLeaves(clusterId, limit, offset);
}

/**
 * Determines the highest priority in a cluster
 * @param {Object} cluster - Cluster feature from supercluster
 * @returns {string} Highest priority level
 */
export function getClusterHighestPriority(cluster) {
  if (!cluster || !cluster.properties) {
    return TaskPriority.LOW;
  }
  
  // For clusters, the reduced property contains highest priority
  return cluster.properties.priority || TaskPriority.LOW;
}

/**
 * Creates a cluster marker icon with count and priority color
 * @param {number} count - Number of tasks in cluster
 * @param {string} priority - Highest priority in cluster
 * @returns {L.DivIcon} Leaflet DivIcon
 */
export function createClusterMarkerIcon(count, priority) {
  const color = getPriorityColor(priority);
  const size = getClusterSize(count);
  
  return L.divIcon({
    className: 'cluster-marker-icon',
    html: `
      <div class="cluster-marker" style="
        background-color: ${color};
        width: ${size}px;
        height: ${size}px;
      ">
        <span class="cluster-count">${count}</span>
      </div>
    `,
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
  });
}

/**
 * Determines cluster marker size based on task count
 * @param {number} count - Number of tasks in cluster
 * @returns {number} Size in pixels
 */
export function getClusterSize(count) {
  if (count < 10) {
    return 36;
  }
  if (count < 50) {
    return 44;
  }
  if (count < 100) {
    return 52;
  }
  return 60;
}

/**
 * Checks if a feature is a cluster
 * @param {Object} feature - GeoJSON feature from supercluster
 * @returns {boolean} True if feature is a cluster
 */
export function isCluster(feature) {
  return !!(feature && feature.properties && feature.properties.cluster === true);
}
