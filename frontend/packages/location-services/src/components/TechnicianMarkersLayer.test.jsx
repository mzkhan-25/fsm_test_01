/**
 * Tests for TechnicianMarkersLayer component
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MapContainer } from 'react-leaflet';
import TechnicianMarkersLayer from './TechnicianMarkersLayer';
import '@testing-library/jest-dom';

// Mock TechnicianMarker component
vi.mock('./TechnicianMarker', () => ({
  default: ({ technician, onClick }) => {
    const handleClick = () => onClick?.(technician);
    return (
      <div 
        data-testid={`technician-marker-${technician.technicianId}`}
        onClick={handleClick}
      >
        {technician.name}
      </div>
    );
  },
}));

describe('TechnicianMarkersLayer', () => {
  const mockTechnicians = [
    {
      technicianId: 1,
      name: 'John Doe',
      status: 'available',
      latitude: 37.7749,
      longitude: -122.4194,
    },
    {
      technicianId: 2,
      name: 'Jane Smith',
      status: 'busy',
      latitude: 37.8049,
      longitude: -122.4294,
    },
    {
      technicianId: 3,
      name: 'Bob Johnson',
      status: 'offline',
      latitude: 37.7549,
      longitude: -122.4094,
    },
  ];

  const renderWithMap = (component) => {
    return render(
      <MapContainer center={[37.7749, -122.4194]} zoom={13}>
        {component}
      </MapContainer>
    );
  };

  describe('rendering', () => {
    it('renders all technician markers', () => {
      renderWithMap(<TechnicianMarkersLayer technicians={mockTechnicians} />);
      
      expect(screen.getByTestId('technician-marker-1')).toBeInTheDocument();
      expect(screen.getByTestId('technician-marker-2')).toBeInTheDocument();
      expect(screen.getByTestId('technician-marker-3')).toBeInTheDocument();
    });

    it('renders correct number of markers', () => {
      renderWithMap(<TechnicianMarkersLayer technicians={mockTechnicians} />);
      
      const markers = screen.getAllByTestId(/technician-marker-/);
      expect(markers).toHaveLength(3);
    });

    it('renders technician names', () => {
      renderWithMap(<TechnicianMarkersLayer technicians={mockTechnicians} />);
      
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Bob Johnson')).toBeInTheDocument();
    });

    it('uses technicianId as key', () => {
      const { container } = renderWithMap(<TechnicianMarkersLayer technicians={mockTechnicians} />);
      
      const marker1 = container.querySelector('[data-testid="technician-marker-1"]');
      const marker2 = container.querySelector('[data-testid="technician-marker-2"]');
      
      expect(marker1).toBeInTheDocument();
      expect(marker2).toBeInTheDocument();
    });
  });

  describe('empty state', () => {
    it('returns null when technicians array is empty', () => {
      const { container } = renderWithMap(<TechnicianMarkersLayer technicians={[]} />);
      
      expect(container.querySelector('[data-testid^="technician-marker-"]')).not.toBeInTheDocument();
    });

    it('returns null when technicians is undefined', () => {
      const { container } = renderWithMap(<TechnicianMarkersLayer technicians={undefined} />);
      
      expect(container.querySelector('[data-testid^="technician-marker-"]')).not.toBeInTheDocument();
    });

    it('returns null when technicians is null', () => {
      const { container } = renderWithMap(<TechnicianMarkersLayer technicians={null} />);
      
      expect(container.querySelector('[data-testid^="technician-marker-"]')).not.toBeInTheDocument();
    });

    it('returns null when technicians is not an array', () => {
      const { container } = renderWithMap(<TechnicianMarkersLayer technicians="not-an-array" />);
      
      expect(container.querySelector('[data-testid^="technician-marker-"]')).not.toBeInTheDocument();
    });
  });

  describe('single technician', () => {
    it('renders single technician marker', () => {
      const singleTech = [mockTechnicians[0]];
      renderWithMap(<TechnicianMarkersLayer technicians={singleTech} />);
      
      expect(screen.getByTestId('technician-marker-1')).toBeInTheDocument();
      expect(screen.queryByTestId('technician-marker-2')).not.toBeInTheDocument();
    });
  });

  describe('interactions', () => {
    it('calls onTechnicianClick with correct technician when marker is clicked', () => {
      const handleClick = vi.fn();
      renderWithMap(
        <TechnicianMarkersLayer 
          technicians={mockTechnicians} 
          onTechnicianClick={handleClick}
        />
      );
      
      const marker = screen.getByTestId('technician-marker-1');
      marker.click();
      
      expect(handleClick).toHaveBeenCalledWith(mockTechnicians[0]);
    });

    it('handles multiple marker clicks', () => {
      const handleClick = vi.fn();
      renderWithMap(
        <TechnicianMarkersLayer 
          technicians={mockTechnicians} 
          onTechnicianClick={handleClick}
        />
      );
      
      const marker1 = screen.getByTestId('technician-marker-1');
      const marker2 = screen.getByTestId('technician-marker-2');
      
      marker1.click();
      marker2.click();
      
      expect(handleClick).toHaveBeenCalledTimes(2);
      expect(handleClick).toHaveBeenCalledWith(mockTechnicians[0]);
      expect(handleClick).toHaveBeenCalledWith(mockTechnicians[1]);
    });

    it('does not error when onTechnicianClick is not provided', () => {
      renderWithMap(<TechnicianMarkersLayer technicians={mockTechnicians} />);
      
      const marker = screen.getByTestId('technician-marker-1');
      
      expect(() => marker.click()).not.toThrow();
    });
  });

  describe('dynamic updates', () => {
    it('updates when technicians array changes', () => {
      const { rerender } = renderWithMap(<TechnicianMarkersLayer technicians={mockTechnicians} />);
      
      expect(screen.getAllByTestId(/technician-marker-/)).toHaveLength(3);
      
      const updatedTechnicians = mockTechnicians.slice(0, 2);
      rerender(
        <MapContainer center={[37.7749, -122.4194]} zoom={13}>
          <TechnicianMarkersLayer technicians={updatedTechnicians} />
        </MapContainer>
      );
      
      expect(screen.getAllByTestId(/technician-marker-/)).toHaveLength(2);
      expect(screen.queryByTestId('technician-marker-3')).not.toBeInTheDocument();
    });
  });
});
