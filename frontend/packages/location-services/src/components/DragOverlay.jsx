import { useMemo } from 'react';
import { useMap, Polyline, Circle } from 'react-leaflet';
import './DragOverlay.css';

/**
 * DragOverlay component that provides visual feedback during drag-and-drop assignment
 * Shows a line from the dragged task to the nearest technician and highlights drop zones
 * 
 * @param {Object} props
 * @param {boolean} props.isDragging - Whether a drag operation is in progress
 * @param {Object} props.dragPosition - Current drag position {lat, lng}
 * @param {Object} props.nearbyTechnician - Technician currently under the drag cursor (for drop)
 * @param {Array} props.technicians - Array of all technicians for showing drop zones
 * @param {number} props.dropRadius - Radius for drop zone detection in pixels (default: 50)
 */
const DragOverlay = ({
  isDragging,
  dragPosition,
  nearbyTechnician,
  technicians = [],
  dropRadius = 50,
}) => {
  const map = useMap();

  // Calculate the approximate drop zone radius in meters based on zoom level
  const dropZoneRadiusMeters = useMemo(() => {
    if (!map || !isDragging) {
      return 100; // default
    }
    const center = map.getCenter();
    const centerPoint = map.latLngToContainerPoint(center);
    const offsetPoint = { x: centerPoint.x + dropRadius, y: centerPoint.y };
    const offsetLatLng = map.containerPointToLatLng(offsetPoint);
    
    // Calculate distance in meters
    return map.distance(center, offsetLatLng);
  }, [map, isDragging, dropRadius]);

  if (!isDragging || !dragPosition) {
    return null;
  }

  // Get positions for line drawing
  const linePositions = nearbyTechnician 
    ? [
        [dragPosition.lat, dragPosition.lng],
        [nearbyTechnician.latitude, nearbyTechnician.longitude]
      ]
    : null;

  return (
    <>
      {/* Drop zone circles around technicians */}
      {technicians.map((technician) => {
        if (technician.latitude === null || technician.latitude === undefined || 
            technician.longitude === null || technician.longitude === undefined) {
          return null;
        }

        const isActive = nearbyTechnician?.technicianId === technician.technicianId;
        
        return (
          <Circle
            key={`dropzone-${technician.technicianId}`}
            center={[technician.latitude, technician.longitude]}
            radius={dropZoneRadiusMeters}
            pathOptions={{
              color: isActive ? '#22c55e' : '#3b82f6',
              fillColor: isActive ? '#22c55e' : '#3b82f6',
              fillOpacity: isActive ? 0.3 : 0.1,
              weight: isActive ? 3 : 1,
              dashArray: isActive ? null : '5, 5',
            }}
          />
        );
      })}

      {/* Line from drag position to nearby technician */}
      {linePositions && (
        <Polyline
          positions={linePositions}
          pathOptions={{
            color: '#22c55e',
            weight: 3,
            opacity: 0.8,
            dashArray: '10, 10',
          }}
        />
      )}
    </>
  );
};

export default DragOverlay;
