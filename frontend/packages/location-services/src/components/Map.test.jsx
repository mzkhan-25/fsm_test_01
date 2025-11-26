import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import Map from './Map';

// Mock react-leaflet components
vi.mock('react-leaflet', () => ({
  MapContainer: ({ children, center, zoom, scrollWheelZoom, zoomControl, className }) => (
    <div 
      data-testid="map-container"
      data-center={JSON.stringify(center)}
      data-zoom={zoom}
      data-scroll-wheel-zoom={scrollWheelZoom}
      data-zoom-control={zoomControl}
      className={className}
    >
      {children}
    </div>
  ),
  TileLayer: ({ url, attribution }) => (
    <div 
      data-testid="tile-layer"
      data-url={url}
      data-attribution={attribution}
    >
      Mock TileLayer
    </div>
  ),
  ZoomControl: ({ position }) => (
    <div data-testid="zoom-control" data-position={position}>
      Mock ZoomControl
    </div>
  ),
  ScaleControl: ({ position }) => (
    <div data-testid="scale-control" data-position={position}>
      Mock ScaleControl
    </div>
  ),
}));

// Mock TaskMarkersLayer component
vi.mock('./TaskMarkersLayer', () => ({
  default: ({ tasks, onTaskClick, onAssignTask, onViewDetails }) => (
    <div 
      data-testid="task-markers-layer"
      data-tasks-count={tasks?.length || 0}
      onClick={() => onTaskClick && onTaskClick({ id: 1 })}
    >
      {tasks?.map(t => <span key={t.id} data-testid={`marker-${t.id}`}>{t.title}</span>)}
      {onAssignTask && <button onClick={() => onAssignTask({ id: 1 })}>Assign</button>}
      {onViewDetails && <button onClick={() => onViewDetails({ id: 1 })}>Details</button>}
    </div>
  ),
}));

describe('Map', () => {
  describe('Rendering', () => {
    it('renders the map with default props', () => {
      render(<Map />);
      
      const map = screen.getByTestId('map-container');
      expect(map).toBeInTheDocument();
    });

    it('renders with a wrapper div', () => {
      render(<Map />);
      
      const wrapper = document.querySelector('.map-wrapper');
      expect(wrapper).toBeInTheDocument();
    });

    it('applies custom className', () => {
      render(<Map className="custom-map-class" />);
      
      const wrapper = document.querySelector('.map-wrapper');
      expect(wrapper).toHaveClass('map-wrapper');
      expect(wrapper).toHaveClass('custom-map-class');
    });

    it('applies custom style', () => {
      const customStyle = { width: '500px', height: '300px' };
      render(<Map style={customStyle} />);
      
      const wrapper = document.querySelector('.map-wrapper');
      expect(wrapper).toHaveStyle(customStyle);
    });

    it('renders the TileLayer with OpenStreetMap URL', () => {
      render(<Map />);
      
      const tileLayer = screen.getByTestId('tile-layer');
      expect(tileLayer).toBeInTheDocument();
      expect(tileLayer.getAttribute('data-url')).toBe('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png');
    });

    it('renders TileLayer with OpenStreetMap attribution', () => {
      render(<Map />);
      
      const tileLayer = screen.getByTestId('tile-layer');
      expect(tileLayer.getAttribute('data-attribution')).toContain('OpenStreetMap');
    });
  });

  describe('Configuration', () => {
    it('uses default center when not provided', () => {
      render(<Map />);
      
      const map = screen.getByTestId('map-container');
      const center = JSON.parse(map.getAttribute('data-center'));
      expect(center).toEqual([37.7749, -122.4194]); // San Francisco as [lat, lng] array
    });

    it('uses custom center when provided', () => {
      const customCenter = { lat: 40.7128, lng: -74.0060 }; // New York
      render(<Map center={customCenter} />);
      
      const map = screen.getByTestId('map-container');
      const center = JSON.parse(map.getAttribute('data-center'));
      expect(center).toEqual([customCenter.lat, customCenter.lng]);
    });

    it('uses default zoom level when not provided', () => {
      render(<Map />);
      
      const map = screen.getByTestId('map-container');
      expect(map.getAttribute('data-zoom')).toBe('12');
    });

    it('uses custom zoom level when provided', () => {
      render(<Map zoom={15} />);
      
      const map = screen.getByTestId('map-container');
      expect(map.getAttribute('data-zoom')).toBe('15');
    });

    it('enables scroll wheel zoom by default', () => {
      render(<Map />);
      
      const map = screen.getByTestId('map-container');
      expect(map.getAttribute('data-scroll-wheel-zoom')).toBe('true');
    });

    it('can disable scroll wheel zoom', () => {
      render(<Map scrollWheelZoom={false} />);
      
      const map = screen.getByTestId('map-container');
      expect(map.getAttribute('data-scroll-wheel-zoom')).toBe('false');
    });

    it('applies leaflet-map-container class to MapContainer', () => {
      render(<Map />);
      
      const map = screen.getByTestId('map-container');
      expect(map).toHaveClass('leaflet-map-container');
    });
  });

  describe('Map Controls', () => {
    it('renders zoom control by default', () => {
      render(<Map />);
      
      const zoomControl = screen.getByTestId('zoom-control');
      expect(zoomControl).toBeInTheDocument();
    });

    it('positions zoom control on top right', () => {
      render(<Map />);
      
      const zoomControl = screen.getByTestId('zoom-control');
      expect(zoomControl.getAttribute('data-position')).toBe('topright');
    });

    it('can hide zoom control', () => {
      render(<Map zoomControl={false} />);
      
      const zoomControl = screen.queryByTestId('zoom-control');
      expect(zoomControl).not.toBeInTheDocument();
    });

    it('renders scale control by default', () => {
      render(<Map />);
      
      const scaleControl = screen.getByTestId('scale-control');
      expect(scaleControl).toBeInTheDocument();
    });

    it('positions scale control on bottom left', () => {
      render(<Map />);
      
      const scaleControl = screen.getByTestId('scale-control');
      expect(scaleControl.getAttribute('data-position')).toBe('bottomleft');
    });

    it('can hide scale control', () => {
      render(<Map scaleControl={false} />);
      
      const scaleControl = screen.queryByTestId('scale-control');
      expect(scaleControl).not.toBeInTheDocument();
    });

    it('disables MapContainer built-in zoom control', () => {
      render(<Map />);
      
      const map = screen.getByTestId('map-container');
      expect(map.getAttribute('data-zoom-control')).toBe('false');
    });
  });

  describe('Task Markers', () => {
    const mockTasks = [
      { id: 1, title: 'Task 1', coordinates: { lat: 37.77, lng: -122.42 } },
      { id: 2, title: 'Task 2', coordinates: { lat: 37.78, lng: -122.41 } },
    ];

    it('renders TaskMarkersLayer', () => {
      render(<Map />);
      
      const markersLayer = screen.getByTestId('task-markers-layer');
      expect(markersLayer).toBeInTheDocument();
    });

    it('passes tasks to TaskMarkersLayer', () => {
      render(<Map tasks={mockTasks} />);
      
      const markersLayer = screen.getByTestId('task-markers-layer');
      expect(markersLayer.getAttribute('data-tasks-count')).toBe('2');
    });

    it('renders empty tasks by default', () => {
      render(<Map />);
      
      const markersLayer = screen.getByTestId('task-markers-layer');
      expect(markersLayer.getAttribute('data-tasks-count')).toBe('0');
    });

    it('renders markers for each task', () => {
      render(<Map tasks={mockTasks} />);
      
      expect(screen.getByTestId('marker-1')).toBeInTheDocument();
      expect(screen.getByTestId('marker-2')).toBeInTheDocument();
    });

    it('passes onTaskClick to TaskMarkersLayer', () => {
      const handleClick = vi.fn();
      render(<Map tasks={mockTasks} onTaskClick={handleClick} />);
      
      const markersLayer = screen.getByTestId('task-markers-layer');
      markersLayer.click();
      
      expect(handleClick).toHaveBeenCalled();
    });
  });

  describe('Responsive Design', () => {
    it('renders map wrapper with full dimensions', () => {
      render(<Map />);
      
      const wrapper = document.querySelector('.map-wrapper');
      expect(wrapper).toHaveClass('map-wrapper');
    });

    it('maintains responsiveness with custom dimensions', () => {
      const style = { width: '100%', height: '600px' };
      render(<Map style={style} />);
      
      const wrapper = document.querySelector('.map-wrapper');
      expect(wrapper).toHaveStyle(style);
    });
  });

  describe('Edge Cases', () => {
    it('handles zero zoom level', () => {
      render(<Map zoom={0} />);
      
      const map = screen.getByTestId('map-container');
      expect(map.getAttribute('data-zoom')).toBe('0');
    });

    it('handles maximum zoom level', () => {
      render(<Map zoom={20} />);
      
      const map = screen.getByTestId('map-container');
      expect(map.getAttribute('data-zoom')).toBe('20');
    });

    it('handles coordinates at equator and prime meridian', () => {
      const center = { lat: 0, lng: 0 };
      render(<Map center={center} />);
      
      const map = screen.getByTestId('map-container');
      const mapCenter = JSON.parse(map.getAttribute('data-center'));
      expect(mapCenter).toEqual([0, 0]);
    });

    it('handles negative coordinates', () => {
      const center = { lat: -33.8688, lng: 151.2093 }; // Sydney
      render(<Map center={center} />);
      
      const map = screen.getByTestId('map-container');
      const mapCenter = JSON.parse(map.getAttribute('data-center'));
      expect(mapCenter).toEqual([center.lat, center.lng]);
    });

    it('renders with all props combined', () => {
      const mockTasks = [{ id: 1, title: 'Test Task', coordinates: { lat: 0, lng: 0 } }];
      const props = {
        center: { lat: 51.5074, lng: -0.1278 },
        zoom: 10,
        style: { height: '500px' },
        className: 'my-custom-map',
        zoomControl: false,
        scaleControl: false,
        scrollWheelZoom: false,
        tasks: mockTasks,
      };
      
      render(<Map {...props} />);
      
      const wrapper = document.querySelector('.map-wrapper');
      expect(wrapper).toHaveClass('my-custom-map');
      expect(wrapper).toHaveStyle({ height: '500px' });
      
      const map = screen.getByTestId('map-container');
      const center = JSON.parse(map.getAttribute('data-center'));
      expect(center).toEqual([51.5074, -0.1278]);
      expect(map.getAttribute('data-zoom')).toBe('10');
      expect(map.getAttribute('data-scroll-wheel-zoom')).toBe('false');
      
      expect(screen.queryByTestId('zoom-control')).not.toBeInTheDocument();
      expect(screen.queryByTestId('scale-control')).not.toBeInTheDocument();
      
      const markersLayer = screen.getByTestId('task-markers-layer');
      expect(markersLayer.getAttribute('data-tasks-count')).toBe('1');
    });
  });

  describe('New callback props', () => {
    it('passes onAssignTask to TaskMarkersLayer', async () => {
      const user = await import('@testing-library/user-event').then(m => m.default.setup());
      const handleAssignTask = vi.fn();
      
      render(<Map onAssignTask={handleAssignTask} />);
      
      const assignButton = screen.getByText('Assign');
      await user.click(assignButton);
      
      expect(handleAssignTask).toHaveBeenCalledWith({ id: 1 });
    });

    it('passes onViewDetails to TaskMarkersLayer', async () => {
      const user = await import('@testing-library/user-event').then(m => m.default.setup());
      const handleViewDetails = vi.fn();
      
      render(<Map onViewDetails={handleViewDetails} />);
      
      const detailsButton = screen.getByText('Details');
      await user.click(detailsButton);
      
      expect(handleViewDetails).toHaveBeenCalledWith({ id: 1 });
    });
  });
});
