import { MapContainer, TileLayer, ZoomControl, ScaleControl } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import './Map.css';

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
 */
const Map = ({ 
  center = { lat: 37.7749, lng: -122.4194 }, // Default: San Francisco
  zoom = 12,
  style,
  className = '',
  zoomControl = true,
  scaleControl = true,
  scrollWheelZoom = true
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
      </MapContainer>
    </div>
  );
};

export default Map;
