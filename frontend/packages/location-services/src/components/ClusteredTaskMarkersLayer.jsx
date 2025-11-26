import { useState, useMemo, useCallback } from 'react';
import { useMap, useMapEvents, Marker, Popup } from 'react-leaflet';
import TaskMarker from './TaskMarker';
import {
  createTaskClusterIndex,
  getClustersForBounds,
  getClusterExpansionZoom,
  createClusterMarkerIcon,
  getClusterHighestPriority,
  isCluster,
  DEFAULT_CLUSTER_OPTIONS,
} from '../utils/clusterUtils';
import { getPriorityColor, getPriorityLabel, getPriorityTextColor } from '../services/taskService';
import './ClusteredTaskMarkersLayer.css';

/**
 * Gets initial bounds from map instance
 * @param {Object} map - Leaflet map instance
 * @returns {Object} Bounds object with west, south, east, north
 */
function getMapBoundsObject(map) {
  const mapBounds = map.getBounds();
  return {
    west: mapBounds.getWest(),
    south: mapBounds.getSouth(),
    east: mapBounds.getEast(),
    north: mapBounds.getNorth(),
  };
}

/**
 * ClusteredTaskMarkersLayer component that renders task markers with clustering support.
 * Clusters task markers at lower zoom levels and expands them when zoomed in.
 * Clusters are color-coded by the highest priority task in the cluster.
 * 
 * @param {Object} props - Component props
 * @param {Array} props.tasks - Array of task objects with coordinates
 * @param {function} props.onTaskClick - Optional handler called when a task marker is clicked
 * @param {function} props.onAssignTask - Optional handler for assigning a task
 * @param {function} props.onViewDetails - Optional handler for viewing task details
 * @param {Object} props.clusterOptions - Optional clustering configuration
 * @returns {JSX.Element|null} Rendered markers layer or null if no tasks
 */
const ClusteredTaskMarkersLayer = ({ 
  tasks = [], 
  onTaskClick, 
  onAssignTask, 
  onViewDetails,
  clusterOptions = DEFAULT_CLUSTER_OPTIONS,
}) => {
  const map = useMap();
  
  // Initialize state with current map bounds/zoom
  const [bounds, setBounds] = useState(() => getMapBoundsObject(map));
  const [zoom, setZoom] = useState(() => map.getZoom());

  // Create cluster index when tasks change
  const clusterIndex = useMemo(() => {
    if (!Array.isArray(tasks) || tasks.length === 0) {
      return null;
    }
    return createTaskClusterIndex(tasks, clusterOptions);
  }, [tasks, clusterOptions]);

  // Update bounds and zoom when map moves
  const updateBoundsAndZoom = useCallback(() => {
    setBounds(getMapBoundsObject(map));
    setZoom(map.getZoom());
  }, [map]);

  // Set up map event listeners
  useMapEvents({
    moveend: updateBoundsAndZoom,
    zoomend: updateBoundsAndZoom,
  });

  // Get clusters for current view
  const clusters = useMemo(() => {
    if (!clusterIndex || !bounds) {
      return [];
    }
    return getClustersForBounds(clusterIndex, bounds, zoom);
  }, [clusterIndex, bounds, zoom]);

  // Handle cluster click - zoom to expand
  const handleClusterClick = useCallback((clusterId, coordinates) => {
    if (!clusterIndex) return;
    
    const expansionZoom = getClusterExpansionZoom(clusterIndex, clusterId);
    map.flyTo([coordinates[1], coordinates[0]], Math.min(expansionZoom, map.getMaxZoom()));
  }, [clusterIndex, map]);

  if (!Array.isArray(tasks) || tasks.length === 0) {
    return null;
  }

  return (
    <>
      {clusters.map((feature) => {
        const [lng, lat] = feature.geometry.coordinates;
        
        if (isCluster(feature)) {
          // Render cluster marker
          const { cluster_id, point_count } = feature.properties;
          const priority = getClusterHighestPriority(feature);
          const icon = createClusterMarkerIcon(point_count, priority);
          
          return (
            <Marker
              key={`cluster-${cluster_id}`}
              position={[lat, lng]}
              icon={icon}
              eventHandlers={{
                click: () => handleClusterClick(cluster_id, feature.geometry.coordinates),
              }}
            >
              <Popup>
                <div className="cluster-popup">
                  <h3 className="cluster-popup-title">Task Cluster</h3>
                  <p className="cluster-popup-count">
                    {point_count} task{point_count !== 1 ? 's' : ''} in this area
                  </p>
                  <div className="cluster-popup-priority">
                    <span>Highest Priority:</span>
                    <span 
                      className="cluster-priority-badge"
                      style={{ 
                        backgroundColor: getPriorityColor(priority),
                        color: getPriorityTextColor(priority),
                      }}
                    >
                      {getPriorityLabel(priority)}
                    </span>
                  </div>
                  <p className="cluster-popup-hint">
                    Click to zoom in
                  </p>
                </div>
              </Popup>
            </Marker>
          );
        } else {
          // Render individual task marker
          const task = feature.properties.task;
          return (
            <TaskMarker
              key={`task-${task.id}`}
              task={task}
              onClick={onTaskClick}
              onAssignTask={onAssignTask}
              onViewDetails={onViewDetails}
            />
          );
        }
      })}
    </>
  );
};

export default ClusteredTaskMarkersLayer;
