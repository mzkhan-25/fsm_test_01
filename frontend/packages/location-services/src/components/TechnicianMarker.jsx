import { Marker, Popup } from 'react-leaflet';
import { getStatusColor, getStatusLabel } from '../services/technicianService';
import { createTechnicianMarkerIcon } from '../utils/markerUtils';
import './TechnicianMarker.css';

/**
 * TechnicianMarker component that displays a technician on the map
 * @param {Object} props - Component props
 * @param {Object} props.technician - Technician object with location data
 * @param {number} props.technician.technicianId - Technician ID
 * @param {string} props.technician.name - Technician name
 * @param {string} props.technician.status - Technician status (available, busy, offline)
 * @param {number} props.technician.latitude - Latitude
 * @param {number} props.technician.longitude - Longitude
 * @param {number} props.technician.accuracy - Location accuracy in meters
 * @param {string} props.technician.timestamp - Location timestamp
 * @param {number} props.technician.batteryLevel - Battery level percentage (0-100)
 * @param {function} props.onClick - Optional click handler
 */
const TechnicianMarker = ({ technician, onClick }) => {
  if (!technician || !technician.latitude || !technician.longitude) {
    return null;
  }

  const { technicianId, name, status, latitude, longitude, accuracy, timestamp, batteryLevel } = technician;
  const icon = createTechnicianMarkerIcon(status);
  const statusLabel = getStatusLabel(status);

  const handleClick = () => {
    if (onClick) {
      onClick(technician);
    }
  };

  // Format timestamp for display
  const formattedTime = timestamp ? new Date(timestamp).toLocaleString() : 'N/A';

  return (
    <Marker
      position={[latitude, longitude]}
      icon={icon}
      eventHandlers={{ click: handleClick }}
    >
      <Popup>
        <div className="technician-popup">
          <h3 className="technician-popup-title">{name || `Technician #${technicianId}`}</h3>
          <div className="technician-popup-meta">
            <p className="technician-popup-id">ID: {technicianId}</p>
            <span 
              className="technician-popup-status"
              style={{ 
                backgroundColor: getStatusColor(status),
                color: '#ffffff',
                padding: '2px 8px',
                borderRadius: '4px',
                fontSize: '0.85em',
                fontWeight: 'bold',
              }}
            >
              {statusLabel}
            </span>
          </div>
          <div className="technician-popup-details">
            <p className="technician-popup-time">
              <strong>Last Update:</strong> {formattedTime}
            </p>
            {accuracy !== null && accuracy !== undefined && (
              <p className="technician-popup-accuracy">
                <strong>Accuracy:</strong> {accuracy.toFixed(1)}m
              </p>
            )}
            {batteryLevel !== null && batteryLevel !== undefined && (
              <p className="technician-popup-battery">
                <strong>Battery:</strong> {batteryLevel}%
              </p>
            )}
          </div>
        </div>
      </Popup>
    </Marker>
  );
};

export default TechnicianMarker;
