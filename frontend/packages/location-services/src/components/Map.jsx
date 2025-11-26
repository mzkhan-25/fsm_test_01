import { MapContainer, TileLayer, ZoomControl, ScaleControl } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import './Map.css';
import ClusteredTaskMarkersLayer from './ClusteredTaskMarkersLayer';
import TechnicianMarkersLayer from './TechnicianMarkersLayer';

// Static OpenStreetMap configuration
const TILE_LAYER_URL = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';

/**
 * Map component that displays an OpenStreetMap using Leaflet
 * @param {Object} props - Component props
 * @param {Object} props.center - Map center coordinates as {lat: number, lng: number}. 
 *   Internally converted to Leaflet's [lat, lng] array format.
 * @param {number} props.zoom - Initial zoom level (1-20)
 * @param {React.CSSProperties} props.style - Custom styles for the map container
 * @param {string} props.className - Additional CSS classes
 * @param {boolean} props.zoomControl - Show zoom controls (default: true)
 * @param {boolean} props.scaleControl - Show scale control (default: true)
 * @param {boolean} props.scrollWheelZoom - Enable scroll wheel zoom (default: true)
 * @param {Array} props.tasks - Array of task objects with coordinates for markers
 * @param {function} props.onTaskClick - Callback when a task marker is clicked
 * @param {function} props.onAssignTask - Callback when assign button is clicked
 * @param {function} props.onViewDetails - Callback when view details button is clicked
 * @param {Array} props.technicians - Array of technician objects with location data
 * @param {function} props.onTechnicianClick - Callback when a technician marker is clicked
 */
const Map = ({ 
  center = { lat: 37.7749, lng: -122.4194 }, // Default: San Francisco
  zoom = 12,
  style,
  className = '',
  zoomControl = true,
  scaleControl = true,
  scrollWheelZoom = true,
  tasks = [],
  onTaskClick,
  onAssignTask,
  onViewDetails,
  technicians = [],
  onTechnicianClick,
}) => {
  return (
    <div className={`map-wrapper ${className}`} style={style}>
      <MapContainer
        center={[center.lat, center.lng]}
        zoom={zoom}
        scrollWheelZoom={scrollWheelZoom}
        zoomControl={false}
        className="leaflet-map-container"
      >
        <TileLayer
          attribution={ATTRIBUTION}
          url={TILE_LAYER_URL}
        />
        {zoomControl && <ZoomControl position="topright" />}
        {scaleControl && <ScaleControl position="bottomleft" />}
        <ClusteredTaskMarkersLayer 
          tasks={tasks} 
          onTaskClick={onTaskClick}
          onAssignTask={onAssignTask}
          onViewDetails={onViewDetails}
        />
        <TechnicianMarkersLayer 
          technicians={technicians}
          onTechnicianClick={onTechnicianClick}
        />
      </MapContainer>
    </div>
  );
};

export default Map;
