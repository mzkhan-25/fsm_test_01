import TechnicianMarker from './TechnicianMarker';

/**
 * TechnicianMarkersLayer component that displays multiple technician markers on the map
 * @param {Object} props - Component props
 * @param {Array} props.technicians - Array of technician objects with location data
 * @param {function} props.onTechnicianClick - Optional callback when a technician marker is clicked
 */
const TechnicianMarkersLayer = ({ technicians = [], onTechnicianClick }) => {
  if (!Array.isArray(technicians) || technicians.length === 0) {
    return null;
  }

  return (
    <>
      {technicians.map((technician) => (
        <TechnicianMarker
          key={`technician-${technician.technicianId}`}
          technician={technician}
          onClick={onTechnicianClick}
        />
      ))}
    </>
  );
};

export default TechnicianMarkersLayer;
