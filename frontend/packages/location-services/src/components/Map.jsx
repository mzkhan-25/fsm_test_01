import { APIProvider, Map as GoogleMap } from '@vis.gl/react-google-maps';
import './Map.css';

/**
 * Map component that displays a Google Map with default settings
 * @param {Object} props - Component props
 * @param {Object} props.center - Map center coordinates {lat: number, lng: number}
 * @param {number} props.zoom - Initial zoom level (1-20)
 * @param {string} props.mapId - Google Maps Map ID for styling
 * @param {React.CSSProperties} props.style - Custom styles for the map container
 * @param {string} props.className - Additional CSS classes
 */
const Map = ({ 
  center = { lat: 37.7749, lng: -122.4194 }, // Default: San Francisco
  zoom = 12,
  mapId = 'location-services-map',
  style,
  className = ''
}) => {
  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;

  if (!apiKey) {
    return (
      <div className={`map-error ${className}`} style={style}>
        <div className="map-error-content">
          <h3>Map Configuration Error</h3>
          <p>Google Maps API key is not configured.</p>
          <p>Please set VITE_GOOGLE_MAPS_API_KEY in your environment variables.</p>
        </div>
      </div>
    );
  }

  return (
    <APIProvider apiKey={apiKey}>
      <div className={`map-wrapper ${className}`} style={style}>
        <GoogleMap
          defaultCenter={center}
          defaultZoom={zoom}
          mapId={mapId}
          gestureHandling="greedy"
          disableDefaultUI={false}
          zoomControl={true}
          mapTypeControl={false}
          scaleControl={true}
          streetViewControl={false}
          rotateControl={false}
          fullscreenControl={true}
        />
      </div>
    </APIProvider>
  );
};

export default Map;
