import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ClusteredTaskMarkersLayer from './ClusteredTaskMarkersLayer';
import * as clusterUtils from '../utils/clusterUtils';

// Mock map bounds and zoom
const mockMapBounds = {
  getWest: () => -123,
  getSouth: () => 37,
  getEast: () => -122,
  getNorth: () => 38,
};

const mockMap = {
  getBounds: () => mockMapBounds,
  getZoom: () => 12,
  getMaxZoom: () => 18,
  flyTo: vi.fn(),
};

// Mock useMap and useMapEvents
vi.mock('react-leaflet', () => ({
  useMap: () => mockMap,
  useMapEvents: () => null,
  Marker: ({ children, position, icon, eventHandlers }) => (
    <div 
      data-testid={icon?.options?.className?.includes('cluster') ? 'cluster-marker' : 'marker'} 
      data-position={JSON.stringify(position)}
      data-is-cluster={icon?.options?.className?.includes('cluster') ? 'true' : 'false'}
      onClick={() => eventHandlers?.click?.()}
    >
      {children}
    </div>
  ),
  Popup: ({ children }) => <div data-testid="popup">{children}</div>,
}));

// Mock TaskMarker component
vi.mock('./TaskMarker', () => ({
  default: ({ task, onClick, onAssignTask, onViewDetails }) => (
    <div 
      data-testid={`task-marker-${task.id}`}
      data-task-id={task.id}
      onClick={() => onClick?.(task)}
    >
      {task.title}
      <button onClick={() => onAssignTask?.(task)} data-testid={`assign-${task.id}`}>Assign</button>
      <button onClick={() => onViewDetails?.(task)} data-testid={`details-${task.id}`}>Details</button>
    </div>
  ),
}));

// Mock cluster utilities with spy abilities
vi.mock('../utils/clusterUtils', () => ({
  DEFAULT_CLUSTER_OPTIONS: { radius: 60, maxZoom: 16, minPoints: 2 },
  createTaskClusterIndex: vi.fn((tasks) => {
    if (!tasks || tasks.length === 0) return null;
    return { mockIndex: true };
  }),
  getClustersForBounds: vi.fn(() => []),
  getClusterExpansionZoom: vi.fn(() => 14),
  createClusterMarkerIcon: vi.fn(() => ({
    options: { className: 'cluster-marker-icon' },
  })),
  getClusterHighestPriority: vi.fn((cluster) => cluster?.properties?.priority || 'LOW'),
  isCluster: vi.fn((feature) => feature?.properties?.cluster === true),
}));

describe('ClusteredTaskMarkersLayer', () => {
  const mockTasks = [
    {
      id: 1,
      title: 'Task 1',
      clientAddress: '123 Main St',
      priority: 'HIGH',
      coordinates: { lat: 37.7749, lng: -122.4194 },
    },
    {
      id: 2,
      title: 'Task 2',
      clientAddress: '456 Oak Ave',
      priority: 'MEDIUM',
      coordinates: { lat: 37.7750, lng: -122.4195 },
    },
    {
      id: 3,
      title: 'Task 3',
      clientAddress: '789 Pine Rd',
      priority: 'LOW',
      coordinates: { lat: 37.7800, lng: -122.4300 },
    },
  ];

  const mockIndividualPoints = [
    {
      type: 'Feature',
      properties: {
        id: 1,
        task: { id: 1, title: 'Task 1', coordinates: { lat: 37.7749, lng: -122.4194 } },
      },
      geometry: {
        type: 'Point',
        coordinates: [-122.4194, 37.7749],
      },
    },
    {
      type: 'Feature',
      properties: {
        id: 2,
        task: { id: 2, title: 'Task 2', coordinates: { lat: 37.7750, lng: -122.4195 } },
      },
      geometry: {
        type: 'Point',
        coordinates: [-122.4195, 37.7750],
      },
    },
  ];

  const mockCluster = {
    type: 'Feature',
    id: 'cluster-1',
    properties: {
      cluster: true,
      cluster_id: 1,
      point_count: 3,
      priority: 'HIGH',
    },
    geometry: {
      type: 'Point',
      coordinates: [-122.4194, 37.7749],
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockMap.flyTo.mockClear();
    // Default to returning individual points
    clusterUtils.getClustersForBounds.mockReturnValue(mockIndividualPoints);
    clusterUtils.isCluster.mockImplementation((feature) => feature?.properties?.cluster === true);
  });

  describe('Rendering', () => {
    it('renders nothing for empty tasks array', () => {
      const { container } = render(<ClusteredTaskMarkersLayer tasks={[]} />);
      expect(container.firstChild).toBeNull();
    });

    it('renders nothing when tasks is undefined', () => {
      const { container } = render(<ClusteredTaskMarkersLayer />);
      expect(container.firstChild).toBeNull();
    });

    it('renders nothing when tasks is null', () => {
      const { container } = render(<ClusteredTaskMarkersLayer tasks={null} />);
      expect(container.firstChild).toBeNull();
    });

    it('renders nothing when tasks is not an array', () => {
      const { container } = render(<ClusteredTaskMarkersLayer tasks="not an array" />);
      expect(container.firstChild).toBeNull();
    });

    it('renders markers when tasks are provided', () => {
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      const markers = screen.getAllByTestId('task-marker-1');
      expect(markers.length).toBeGreaterThan(0);
    });
  });

  describe('Individual task markers', () => {
    it('renders TaskMarker for non-clustered points', () => {
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      expect(screen.getByTestId('task-marker-1')).toBeInTheDocument();
      expect(screen.getByTestId('task-marker-2')).toBeInTheDocument();
    });

    it('passes task data to TaskMarker', () => {
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      expect(screen.getByText('Task 1')).toBeInTheDocument();
      expect(screen.getByText('Task 2')).toBeInTheDocument();
    });
  });

  describe('Cluster markers', () => {
    beforeEach(() => {
      // Configure mock to return a cluster
      clusterUtils.getClustersForBounds.mockReturnValue([mockCluster]);
    });

    it('renders cluster marker when points are clustered', () => {
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      const clusterMarker = screen.getByTestId('cluster-marker');
      expect(clusterMarker).toBeInTheDocument();
    });

    it('displays task count in cluster popup', () => {
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      expect(screen.getByText('3 tasks in this area')).toBeInTheDocument();
    });

    it('displays cluster title in popup', () => {
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      expect(screen.getByText('Task Cluster')).toBeInTheDocument();
    });

    it('displays highest priority in cluster popup', () => {
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      expect(screen.getByText('Highest Priority:')).toBeInTheDocument();
    });

    it('displays zoom hint in cluster popup', () => {
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      expect(screen.getByText('Click to zoom in')).toBeInTheDocument();
    });

    it('uses singular "task" when cluster has only one task', () => {
      const singleTaskCluster = {
        ...mockCluster,
        properties: { ...mockCluster.properties, point_count: 1 },
      };
      clusterUtils.getClustersForBounds.mockReturnValue([singleTaskCluster]);
      
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      expect(screen.getByText('1 task in this area')).toBeInTheDocument();
    });

    it('calls flyTo when cluster is clicked', async () => {
      const user = userEvent.setup();
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      const clusterMarker = screen.getByTestId('cluster-marker');
      await user.click(clusterMarker);
      
      expect(mockMap.flyTo).toHaveBeenCalled();
      expect(mockMap.flyTo).toHaveBeenCalledWith([37.7749, -122.4194], 14);
    });

    it('respects max zoom when expanding cluster', async () => {
      const user = userEvent.setup();
      clusterUtils.getClusterExpansionZoom.mockReturnValue(25); // Higher than maxZoom
      
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      const clusterMarker = screen.getByTestId('cluster-marker');
      await user.click(clusterMarker);
      
      // Should use maxZoom (18) instead of expansionZoom (25)
      expect(mockMap.flyTo).toHaveBeenCalledWith([37.7749, -122.4194], 18);
    });

    it('creates cluster marker icon with correct parameters', () => {
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      expect(clusterUtils.createClusterMarkerIcon).toHaveBeenCalledWith(3, 'HIGH');
    });
  });

  describe('Click handlers', () => {
    beforeEach(() => {
      clusterUtils.getClustersForBounds.mockReturnValue(mockIndividualPoints);
    });

    it('passes onTaskClick to TaskMarker', async () => {
      const user = userEvent.setup();
      const handleTaskClick = vi.fn();
      
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} onTaskClick={handleTaskClick} />);
      
      await user.click(screen.getByTestId('task-marker-1'));
      
      expect(handleTaskClick).toHaveBeenCalled();
      expect(handleTaskClick.mock.calls[0][0].id).toBe(1);
      expect(handleTaskClick.mock.calls[0][0].title).toBe('Task 1');
    });

    it('passes onAssignTask to TaskMarker', async () => {
      const user = userEvent.setup();
      const handleAssignTask = vi.fn();
      
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} onAssignTask={handleAssignTask} />);
      
      await user.click(screen.getByTestId('assign-1'));
      
      expect(handleAssignTask).toHaveBeenCalled();
    });

    it('passes onViewDetails to TaskMarker', async () => {
      const user = userEvent.setup();
      const handleViewDetails = vi.fn();
      
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} onViewDetails={handleViewDetails} />);
      
      await user.click(screen.getByTestId('details-1'));
      
      expect(handleViewDetails).toHaveBeenCalled();
    });
  });

  describe('Custom cluster options', () => {
    it('accepts custom cluster options', () => {
      const customOptions = { radius: 100, maxZoom: 14, minPoints: 3 };
      
      expect(() => {
        render(<ClusteredTaskMarkersLayer tasks={mockTasks} clusterOptions={customOptions} />);
      }).not.toThrow();
    });

    it('passes custom options to createTaskClusterIndex', () => {
      const customOptions = { radius: 100, maxZoom: 14, minPoints: 3 };
      
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} clusterOptions={customOptions} />);
      
      expect(clusterUtils.createTaskClusterIndex).toHaveBeenCalledWith(mockTasks, customOptions);
    });
  });

  describe('Edge cases', () => {
    it('returns empty clusters array when clusterIndex is null', () => {
      clusterUtils.createTaskClusterIndex.mockReturnValue(null);
      clusterUtils.getClustersForBounds.mockReturnValue([]);
      
      const { container } = render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      // Should not render any task markers (only empty container)
      expect(container.querySelector('[data-testid="task-marker-1"]')).toBeNull();
      expect(container.querySelector('[data-testid="cluster-marker"]')).toBeNull();
    });

    it('handles empty clusters array', () => {
      clusterUtils.getClustersForBounds.mockReturnValue([]);
      
      render(<ClusteredTaskMarkersLayer tasks={mockTasks} />);
      
      // Component renders but with no visible markers
      expect(screen.queryByTestId('task-marker-1')).not.toBeInTheDocument();
      expect(screen.queryByTestId('cluster-marker')).not.toBeInTheDocument();
    });
  });
});
