/**
 * Tests for TechnicianMarker component
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import TechnicianMarker from './TechnicianMarker';
import '@testing-library/jest-dom';

// Mock react-leaflet components
vi.mock('react-leaflet', () => ({
  Marker: ({ children, position, eventHandlers }) => (
    <div 
      data-testid="marker"
      data-position={JSON.stringify(position)}
      onClick={eventHandlers?.click}
    >
      {children}
    </div>
  ),
  Popup: ({ children }) => (
    <div data-testid="popup">{children}</div>
  ),
}));

// Mock the markerUtils module
vi.mock('../utils/markerUtils', () => ({
  createTechnicianMarkerIcon: vi.fn(() => ({})),
}));

describe('TechnicianMarker', () => {
  const mockTechnician = {
    technicianId: 1,
    name: 'John Doe',
    status: 'available',
    latitude: 37.7749,
    longitude: -122.4194,
    accuracy: 10.5,
    timestamp: '2024-01-01T10:00:00',
    batteryLevel: 85,
  };

  const renderTechnicianMarker = (props) => {
    return render(<TechnicianMarker {...props} />);
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('rendering', () => {
    it('renders technician marker with all data', () => {
      renderTechnicianMarker({ technician: mockTechnician });
      
      expect(screen.getByTestId('marker')).toBeInTheDocument();
    });

    it('renders technician name in popup', () => {
      renderTechnicianMarker({ technician: mockTechnician });
      
      expect(screen.getByText('John Doe')).toBeInTheDocument();
    });

    it('renders technician ID when name is not provided', () => {
      const techWithoutName = { ...mockTechnician, name: null };
      renderTechnicianMarker({ technician: techWithoutName });
      
      expect(screen.getByText('Technician #1')).toBeInTheDocument();
    });

    it('renders status badge', () => {
      renderTechnicianMarker({ technician: mockTechnician });
      
      expect(screen.getByText('Available')).toBeInTheDocument();
    });

    it('renders technician ID', () => {
      renderTechnicianMarker({ technician: mockTechnician });
      
      expect(screen.getByText(/ID: 1/)).toBeInTheDocument();
    });

    it('renders last update time', () => {
      renderTechnicianMarker({ technician: mockTechnician });
      
      expect(screen.getByText(/Last Update:/)).toBeInTheDocument();
    });

    it('renders accuracy when provided', () => {
      renderTechnicianMarker({ technician: mockTechnician });
      
      expect(screen.getByText(/Accuracy:/)).toBeInTheDocument();
      expect(screen.getByText(/10\.5m/)).toBeInTheDocument();
    });

    it('renders battery level when provided', () => {
      renderTechnicianMarker({ technician: mockTechnician });
      
      expect(screen.getByText(/Battery:/)).toBeInTheDocument();
      expect(screen.getByText(/85%/)).toBeInTheDocument();
    });

    it('does not render accuracy when not provided', () => {
      const techWithoutAccuracy = { ...mockTechnician, accuracy: null };
      renderTechnicianMarker({ technician: techWithoutAccuracy });
      
      expect(screen.queryByText(/Accuracy:/)).not.toBeInTheDocument();
    });

    it('does not render battery when not provided', () => {
      const techWithoutBattery = { ...mockTechnician, batteryLevel: null };
      renderTechnicianMarker({ technician: techWithoutBattery });
      
      expect(screen.queryByText(/Battery:/)).not.toBeInTheDocument();
    });

    it('renders "N/A" when timestamp is null', () => {
      const techWithoutTimestamp = { ...mockTechnician, timestamp: null };
      renderTechnicianMarker({ technician: techWithoutTimestamp });
      
      expect(screen.getByText(/N\/A/)).toBeInTheDocument();
    });
  });

  describe('edge cases', () => {
    it('returns null when technician is null', () => {
      const { container } = renderTechnicianMarker({ technician: null });
      
      expect(container.firstChild).toBeNull();
    });

    it('returns null when technician is undefined', () => {
      const { container } = renderTechnicianMarker({ technician: undefined });
      
      expect(container.firstChild).toBeNull();
    });

    it('returns null when latitude is missing', () => {
      const techWithoutLat = { ...mockTechnician, latitude: null };
      const { container } = renderTechnicianMarker({ technician: techWithoutLat });
      
      expect(container.firstChild).toBeNull();
    });

    it('returns null when longitude is missing', () => {
      const techWithoutLng = { ...mockTechnician, longitude: null };
      const { container } = renderTechnicianMarker({ technician: techWithoutLng });
      
      expect(container.firstChild).toBeNull();
    });

    it('handles zero accuracy', () => {
      const techWithZeroAccuracy = { ...mockTechnician, accuracy: 0 };
      renderTechnicianMarker({ technician: techWithZeroAccuracy });
      
      expect(screen.getByText(/0\.0m/)).toBeInTheDocument();
    });

    it('handles zero battery level', () => {
      const techWithZeroBattery = { ...mockTechnician, batteryLevel: 0 };
      renderTechnicianMarker({ technician: techWithZeroBattery });
      
      expect(screen.getByText(/0%/)).toBeInTheDocument();
    });
  });

  describe('interactions', () => {
    it('calls onClick when marker is clicked', () => {
      const handleClick = vi.fn();
      renderTechnicianMarker({ technician: mockTechnician, onClick: handleClick });
      
      const marker = screen.getByTestId('marker');
      marker.click();
      
      expect(handleClick).toHaveBeenCalledWith(mockTechnician);
    });

    it('does not error when onClick is not provided', () => {
      renderTechnicianMarker({ technician: mockTechnician });
      
      const marker = screen.getByTestId('marker');
      
      expect(() => marker.click()).not.toThrow();
    });
  });

  describe('status variations', () => {
    it('renders "Busy" status correctly', () => {
      const busyTech = { ...mockTechnician, status: 'busy' };
      renderTechnicianMarker({ technician: busyTech });
      
      expect(screen.getByText('Busy')).toBeInTheDocument();
    });

    it('renders "Offline" status correctly', () => {
      const offlineTech = { ...mockTechnician, status: 'offline' };
      renderTechnicianMarker({ technician: offlineTech });
      
      expect(screen.getByText('Offline')).toBeInTheDocument();
    });

    it('renders "Unknown" for unknown status', () => {
      const unknownTech = { ...mockTechnician, status: 'unknown-status' };
      renderTechnicianMarker({ technician: unknownTech });
      
      expect(screen.getByText('Unknown')).toBeInTheDocument();
    });
  });
});
