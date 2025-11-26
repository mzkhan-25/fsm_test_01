import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskMarkersLayer from './TaskMarkersLayer';

// Mock TaskMarker component
vi.mock('./TaskMarker', () => ({
  default: ({ task, onClick }) => (
    <div 
      data-testid={`task-marker-${task.id}`}
      data-task-id={task.id}
      onClick={() => onClick?.(task)}
    >
      {task.title}
    </div>
  ),
}));

describe('TaskMarkersLayer', () => {
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
      coordinates: { lat: 37.7850, lng: -122.4090 },
    },
    {
      id: 3,
      title: 'Task 3',
      clientAddress: '789 Pine Rd',
      priority: 'LOW',
      coordinates: { lat: 37.7650, lng: -122.4300 },
    },
  ];

  describe('Rendering', () => {
    it('renders markers for all tasks', () => {
      render(<TaskMarkersLayer tasks={mockTasks} />);
      
      expect(screen.getByTestId('task-marker-1')).toBeInTheDocument();
      expect(screen.getByTestId('task-marker-2')).toBeInTheDocument();
      expect(screen.getByTestId('task-marker-3')).toBeInTheDocument();
    });

    it('renders nothing for empty tasks array', () => {
      const { container } = render(<TaskMarkersLayer tasks={[]} />);
      expect(container.firstChild).toBeNull();
    });

    it('renders nothing when tasks is undefined', () => {
      const { container } = render(<TaskMarkersLayer />);
      expect(container.firstChild).toBeNull();
    });

    it('renders nothing when tasks is null', () => {
      const { container } = render(<TaskMarkersLayer tasks={null} />);
      expect(container.firstChild).toBeNull();
    });

    it('renders nothing when tasks is not an array', () => {
      const { container } = render(<TaskMarkersLayer tasks="not an array" />);
      expect(container.firstChild).toBeNull();
    });

    it('renders correct number of markers', () => {
      render(<TaskMarkersLayer tasks={mockTasks} />);
      
      const markers = screen.getAllByTestId(/task-marker-/);
      expect(markers).toHaveLength(3);
    });

    it('renders task titles', () => {
      render(<TaskMarkersLayer tasks={mockTasks} />);
      
      expect(screen.getByText('Task 1')).toBeInTheDocument();
      expect(screen.getByText('Task 2')).toBeInTheDocument();
      expect(screen.getByText('Task 3')).toBeInTheDocument();
    });
  });

  describe('Click handling', () => {
    it('calls onTaskClick with task when marker is clicked', async () => {
      const user = userEvent.setup();
      const handleTaskClick = vi.fn();
      
      render(<TaskMarkersLayer tasks={mockTasks} onTaskClick={handleTaskClick} />);
      
      await user.click(screen.getByTestId('task-marker-1'));
      
      expect(handleTaskClick).toHaveBeenCalledWith(mockTasks[0]);
    });

    it('calls onTaskClick for each marker', async () => {
      const user = userEvent.setup();
      const handleTaskClick = vi.fn();
      
      render(<TaskMarkersLayer tasks={mockTasks} onTaskClick={handleTaskClick} />);
      
      await user.click(screen.getByTestId('task-marker-2'));
      expect(handleTaskClick).toHaveBeenCalledWith(mockTasks[1]);
      
      await user.click(screen.getByTestId('task-marker-3'));
      expect(handleTaskClick).toHaveBeenCalledWith(mockTasks[2]);
    });

    it('does not throw when clicked without onTaskClick handler', async () => {
      const user = userEvent.setup();
      
      render(<TaskMarkersLayer tasks={mockTasks} />);
      
      await expect(user.click(screen.getByTestId('task-marker-1'))).resolves.not.toThrow();
    });
  });
});
