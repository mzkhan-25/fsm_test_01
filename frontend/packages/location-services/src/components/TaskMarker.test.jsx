import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskMarker from './TaskMarker';

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

// Mock markerUtils
vi.mock('../utils/markerUtils', () => ({
  createTaskMarkerIcon: vi.fn(() => ({})),
}));

describe('TaskMarker', () => {
  const mockTask = {
    id: 1,
    title: 'Test Task',
    description: 'Task description',
    clientAddress: '123 Main St',
    priority: 'HIGH',
    estimatedDuration: 60,
    coordinates: {
      lat: 37.7749,
      lng: -122.4194,
    },
  };

  describe('Rendering', () => {
    it('renders a marker when task is provided', () => {
      render(<TaskMarker task={mockTask} />);
      
      const marker = screen.getByTestId('marker');
      expect(marker).toBeInTheDocument();
    });

    it('renders nothing when task is null', () => {
      const { container } = render(<TaskMarker task={null} />);
      expect(container.firstChild).toBeNull();
    });

    it('renders nothing when task is undefined', () => {
      const { container } = render(<TaskMarker task={undefined} />);
      expect(container.firstChild).toBeNull();
    });

    it('renders nothing when task has no coordinates', () => {
      const taskWithoutCoords = { ...mockTask, coordinates: null };
      const { container } = render(<TaskMarker task={taskWithoutCoords} />);
      expect(container.firstChild).toBeNull();
    });

    it('renders popup with task title', () => {
      render(<TaskMarker task={mockTask} />);
      
      expect(screen.getByText('Test Task')).toBeInTheDocument();
    });

    it('renders popup with task ID', () => {
      render(<TaskMarker task={mockTask} />);
      
      expect(screen.getByText('Task #1')).toBeInTheDocument();
    });

    it('renders popup with client address', () => {
      render(<TaskMarker task={mockTask} />);
      
      expect(screen.getByText('123 Main St')).toBeInTheDocument();
    });

    it('renders popup with description when provided', () => {
      render(<TaskMarker task={mockTask} />);
      
      expect(screen.getByText('Task description')).toBeInTheDocument();
    });

    it('does not render description when not provided', () => {
      const taskWithoutDesc = { ...mockTask, description: null };
      render(<TaskMarker task={taskWithoutDesc} />);
      
      expect(screen.queryByText('Task description')).not.toBeInTheDocument();
    });

    it('renders priority badge', () => {
      render(<TaskMarker task={mockTask} />);
      
      expect(screen.getByText('High Priority')).toBeInTheDocument();
    });

    it('uses correct coordinates for marker position', () => {
      render(<TaskMarker task={mockTask} />);
      
      const marker = screen.getByTestId('marker');
      const position = JSON.parse(marker.getAttribute('data-position'));
      expect(position).toEqual([37.7749, -122.4194]);
    });
  });

  describe('Click handling', () => {
    it('calls onClick with task when marker is clicked', async () => {
      const user = userEvent.setup();
      const handleClick = vi.fn();
      
      render(<TaskMarker task={mockTask} onClick={handleClick} />);
      
      const marker = screen.getByTestId('marker');
      await user.click(marker);
      
      expect(handleClick).toHaveBeenCalledWith(mockTask);
    });

    it('does not throw when clicked without onClick handler', async () => {
      const user = userEvent.setup();
      
      render(<TaskMarker task={mockTask} />);
      
      const marker = screen.getByTestId('marker');
      await expect(user.click(marker)).resolves.not.toThrow();
    });
  });

  describe('Priority colors', () => {
    it('renders HIGH priority task', () => {
      const highTask = { ...mockTask, priority: 'HIGH' };
      render(<TaskMarker task={highTask} />);
      
      expect(screen.getByText('High Priority')).toBeInTheDocument();
    });

    it('renders MEDIUM priority task', () => {
      const mediumTask = { ...mockTask, priority: 'MEDIUM' };
      render(<TaskMarker task={mediumTask} />);
      
      expect(screen.getByText('Medium Priority')).toBeInTheDocument();
    });

    it('renders LOW priority task', () => {
      const lowTask = { ...mockTask, priority: 'LOW' };
      render(<TaskMarker task={lowTask} />);
      
      expect(screen.getByText('Low Priority')).toBeInTheDocument();
    });
  });

  describe('New popup features', () => {
    it('renders estimated duration when provided', () => {
      render(<TaskMarker task={mockTask} />);
      
      expect(screen.getByText('60 min')).toBeInTheDocument();
    });

    it('does not render duration when not provided', () => {
      const taskWithoutDuration = { ...mockTask, estimatedDuration: null };
      render(<TaskMarker task={taskWithoutDuration} />);
      
      expect(screen.queryByText(/min$/)).not.toBeInTheDocument();
    });

    it('renders assign task button', () => {
      render(<TaskMarker task={mockTask} />);
      
      expect(screen.getByRole('button', { name: 'Assign Task' })).toBeInTheDocument();
    });

    it('renders view details button', () => {
      render(<TaskMarker task={mockTask} />);
      
      expect(screen.getByRole('button', { name: 'View Details' })).toBeInTheDocument();
    });

    it('calls onAssignTask when assign button is clicked', async () => {
      const user = userEvent.setup();
      const handleAssign = vi.fn();
      
      render(<TaskMarker task={mockTask} onAssignTask={handleAssign} />);
      
      const assignButton = screen.getByRole('button', { name: 'Assign Task' });
      await user.click(assignButton);
      
      expect(handleAssign).toHaveBeenCalledWith(mockTask);
    });

    it('calls onViewDetails when view details button is clicked', async () => {
      const user = userEvent.setup();
      const handleViewDetails = vi.fn();
      
      render(<TaskMarker task={mockTask} onViewDetails={handleViewDetails} />);
      
      const detailsButton = screen.getByRole('button', { name: 'View Details' });
      await user.click(detailsButton);
      
      expect(handleViewDetails).toHaveBeenCalledWith(mockTask);
    });

    it('does not throw when assign button clicked without handler', async () => {
      const user = userEvent.setup();
      
      render(<TaskMarker task={mockTask} />);
      
      const assignButton = screen.getByRole('button', { name: 'Assign Task' });
      await expect(user.click(assignButton)).resolves.not.toThrow();
    });

    it('does not throw when view details button clicked without handler', async () => {
      const user = userEvent.setup();
      
      render(<TaskMarker task={mockTask} />);
      
      const detailsButton = screen.getByRole('button', { name: 'View Details' });
      await expect(user.click(detailsButton)).resolves.not.toThrow();
    });

    it('truncates long descriptions', () => {
      const longDescription = 'A'.repeat(150);
      const taskWithLongDesc = { ...mockTask, description: longDescription };
      
      render(<TaskMarker task={taskWithLongDesc} />);
      
      const descElement = screen.getByText(/A+\.\.\./);
      expect(descElement).toBeInTheDocument();
      expect(descElement.textContent.length).toBeLessThanOrEqual(104); // 100 chars + '...'
      expect(descElement.textContent).toContain('...');
    });

    it('does not truncate short descriptions', () => {
      const shortDescription = 'Short description';
      const taskWithShortDesc = { ...mockTask, description: shortDescription };
      
      render(<TaskMarker task={taskWithShortDesc} />);
      
      expect(screen.getByText('Short description')).toBeInTheDocument();
    });

    it('shows full description in title attribute', () => {
      const description = 'Full task description for tooltip';
      const taskWithDesc = { ...mockTask, description };
      
      render(<TaskMarker task={taskWithDesc} />);
      
      const descElement = screen.getByText(description);
      expect(descElement).toHaveAttribute('title', description);
    });
  });
});
