import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import MapAssignmentModal from './MapAssignmentModal';

describe('MapAssignmentModal', () => {
  const mockTask = {
    id: 1,
    title: 'Test Task',
    clientAddress: '123 Main St',
    coordinates: {
      lat: 37.7749,
      lng: -122.4194,
    },
  };

  const mockTechnicians = [
    { 
      technicianId: 1, 
      name: 'Close Tech', 
      latitude: 37.7849, 
      longitude: -122.4094,
      status: 'available',
      workload: 2,
    },
    { 
      technicianId: 2, 
      name: 'Far Tech', 
      latitude: 34.0522, 
      longitude: -118.2437,
      status: 'busy',
      workload: 5,
    },
  ];

  const defaultProps = {
    task: mockTask,
    technicians: mockTechnicians,
    isOpen: true,
    onClose: vi.fn(),
    onAssign: vi.fn(),
    onHighlightTechnicians: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('rendering', () => {
    it('renders nothing when isOpen is false', () => {
      const { container } = render(
        <MapAssignmentModal {...defaultProps} isOpen={false} />
      );
      expect(container.firstChild).toBeNull();
    });

    it('renders modal when isOpen is true', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    it('displays modal title', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getByText('Assign Task from Map')).toBeInTheDocument();
    });

    it('displays task info', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getByText('Test Task')).toBeInTheDocument();
      expect(screen.getByText('123 Main St')).toBeInTheDocument();
    });

    it('renders technician list', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getByRole('listbox')).toBeInTheDocument();
    });

    it('displays technicians sorted by distance (nearest first)', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      const listItems = screen.getAllByRole('option');
      expect(listItems[0]).toHaveTextContent('Close Tech');
      expect(listItems[1]).toHaveTextContent('Far Tech');
    });

    it('displays distance for each technician', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      // Distance should be displayed in km
      expect(screen.getAllByText(/km/)).toHaveLength(2);
    });

    it('displays technician status', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getByText('Available')).toBeInTheDocument();
      expect(screen.getByText('Busy')).toBeInTheDocument();
    });

    it('displays technician workload', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getByText('2 tasks')).toBeInTheDocument();
      expect(screen.getByText('5 tasks')).toBeInTheDocument();
    });
  });

  describe('empty states', () => {
    it('shows empty state when no technicians have valid locations', () => {
      const techsWithoutLocation = [
        { technicianId: 1, name: 'No Location Tech', latitude: null, longitude: null },
      ];
      render(
        <MapAssignmentModal 
          {...defaultProps} 
          technicians={techsWithoutLocation} 
        />
      );
      expect(screen.getByText(/no technicians with valid locations/i)).toBeInTheDocument();
    });

    it('shows empty state when technicians array is empty', () => {
      render(<MapAssignmentModal {...defaultProps} technicians={[]} />);
      expect(screen.getByText(/no technicians with valid locations/i)).toBeInTheDocument();
    });
  });

  describe('technician selection', () => {
    it('selects technician on click', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.click(techItem);
      expect(techItem).toHaveAttribute('aria-selected', 'true');
    });

    it('selects technician on keyboard Enter', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.keyDown(techItem, { key: 'Enter' });
      expect(techItem).toHaveAttribute('aria-selected', 'true');
    });

    it('selects technician on keyboard Space', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.keyDown(techItem, { key: ' ' });
      expect(techItem).toHaveAttribute('aria-selected', 'true');
    });

    it('enables Continue button when technician is selected', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      const continueButton = screen.getByRole('button', { name: /continue/i });
      expect(continueButton).toBeDisabled();

      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.click(techItem);

      expect(continueButton).not.toBeDisabled();
    });
  });

  describe('confirmation flow', () => {
    it('shows confirmation view after clicking Continue', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.click(techItem);
      
      fireEvent.click(screen.getByRole('button', { name: /continue/i }));

      // Title changes to Confirm Assignment
      expect(screen.getByRole('heading', { name: 'Confirm Assignment' })).toBeInTheDocument();
      expect(screen.getByText(/are you sure you want to assign/i)).toBeInTheDocument();
    });

    it('displays assignment details in confirmation', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.click(techItem);
      fireEvent.click(screen.getByRole('button', { name: /continue/i }));

      // Check confirmation details - task title appears twice (task info + confirmation)
      expect(screen.getAllByText('Test Task')).toHaveLength(2);
      expect(screen.getByText('Close Tech')).toBeInTheDocument();
    });

    it('shows Back button in confirmation view', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.click(techItem);
      fireEvent.click(screen.getByRole('button', { name: /continue/i }));

      expect(screen.getByRole('button', { name: /back/i })).toBeInTheDocument();
    });

    it('returns to selection view when Back is clicked', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.click(techItem);
      fireEvent.click(screen.getByRole('button', { name: /continue/i }));
      fireEvent.click(screen.getByRole('button', { name: /back/i }));

      expect(screen.getByText('Assign Task from Map')).toBeInTheDocument();
      expect(screen.getByRole('listbox')).toBeInTheDocument();
    });
  });

  describe('assignment', () => {
    it('calls onAssign with task and technician IDs on confirm', () => {
      const onAssign = vi.fn();
      render(<MapAssignmentModal {...defaultProps} onAssign={onAssign} />);
      
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.click(techItem);
      fireEvent.click(screen.getByRole('button', { name: /continue/i }));
      fireEvent.click(screen.getByRole('button', { name: /confirm assignment/i }));

      expect(onAssign).toHaveBeenCalledWith(1, 1); // taskId, technicianId
    });

    it('disables buttons when isAssigning is true', () => {
      render(<MapAssignmentModal {...defaultProps} isAssigning={true} />);
      
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.click(techItem);
      fireEvent.click(screen.getByRole('button', { name: /continue/i }));

      expect(screen.getByRole('button', { name: /assigning/i })).toBeDisabled();
      expect(screen.getByRole('button', { name: /back/i })).toBeDisabled();
    });

    it('shows "Assigning..." text when isAssigning is true', () => {
      render(<MapAssignmentModal {...defaultProps} isAssigning={true} />);
      
      const techItem = screen.getByText('Close Tech').closest('[role="option"]');
      fireEvent.click(techItem);
      fireEvent.click(screen.getByRole('button', { name: /continue/i }));

      expect(screen.getByText('Assigning...')).toBeInTheDocument();
    });
  });

  describe('error and success messages', () => {
    it('displays error message', () => {
      render(<MapAssignmentModal {...defaultProps} error="Assignment failed" />);
      expect(screen.getByRole('alert')).toHaveTextContent('Assignment failed');
    });

    it('displays success message', () => {
      render(<MapAssignmentModal {...defaultProps} successMessage="Task assigned!" />);
      expect(screen.getByRole('status')).toHaveTextContent('Task assigned!');
    });
  });

  describe('modal closing', () => {
    it('calls onClose when Cancel button is clicked', () => {
      const onClose = vi.fn();
      render(<MapAssignmentModal {...defaultProps} onClose={onClose} />);
      
      fireEvent.click(screen.getByRole('button', { name: /cancel/i }));

      expect(onClose).toHaveBeenCalled();
    });

    it('calls onClose when X button is clicked', () => {
      const onClose = vi.fn();
      render(<MapAssignmentModal {...defaultProps} onClose={onClose} />);
      
      fireEvent.click(screen.getByRole('button', { name: /close modal/i }));

      expect(onClose).toHaveBeenCalled();
    });

    it('calls onClose when clicking overlay', () => {
      const onClose = vi.fn();
      render(<MapAssignmentModal {...defaultProps} onClose={onClose} />);
      
      fireEvent.click(screen.getByRole('dialog'));

      expect(onClose).toHaveBeenCalled();
    });

    it('does not close when clicking modal content', () => {
      const onClose = vi.fn();
      render(<MapAssignmentModal {...defaultProps} onClose={onClose} />);
      
      fireEvent.click(screen.getByText('Test Task'));

      expect(onClose).not.toHaveBeenCalled();
    });

    it('calls onClose on Escape key', () => {
      const onClose = vi.fn();
      render(<MapAssignmentModal {...defaultProps} onClose={onClose} />);
      
      fireEvent.keyDown(document, { key: 'Escape' });

      expect(onClose).toHaveBeenCalled();
    });

    it('does not close on Escape when isAssigning is true', () => {
      const onClose = vi.fn();
      render(<MapAssignmentModal {...defaultProps} onClose={onClose} isAssigning={true} />);
      
      fireEvent.keyDown(document, { key: 'Escape' });

      expect(onClose).not.toHaveBeenCalled();
    });

    it('does not close on overlay click when isAssigning is true', () => {
      const onClose = vi.fn();
      render(<MapAssignmentModal {...defaultProps} onClose={onClose} isAssigning={true} />);
      
      fireEvent.click(screen.getByRole('dialog'));

      expect(onClose).not.toHaveBeenCalled();
    });

    it('disables close button when isAssigning is true', () => {
      render(<MapAssignmentModal {...defaultProps} isAssigning={true} />);
      
      expect(screen.getByRole('button', { name: /close modal/i })).toBeDisabled();
    });
  });

  describe('technician highlighting', () => {
    it('calls onHighlightTechnicians with technician IDs when modal opens', () => {
      const onHighlightTechnicians = vi.fn();
      render(
        <MapAssignmentModal 
          {...defaultProps} 
          onHighlightTechnicians={onHighlightTechnicians}
        />
      );

      expect(onHighlightTechnicians).toHaveBeenCalledWith([1, 2]);
    });

    it('calls onHighlightTechnicians with empty array when modal closes', () => {
      const onHighlightTechnicians = vi.fn();
      const { rerender } = render(
        <MapAssignmentModal 
          {...defaultProps} 
          onHighlightTechnicians={onHighlightTechnicians}
        />
      );

      onHighlightTechnicians.mockClear();
      
      rerender(
        <MapAssignmentModal 
          {...defaultProps} 
          isOpen={false}
          onHighlightTechnicians={onHighlightTechnicians}
        />
      );

      expect(onHighlightTechnicians).toHaveBeenCalledWith([]);
    });
  });

  describe('capacity warnings', () => {
    it('shows warning for technicians at capacity', () => {
      const techniciansWithCapacity = [
        { 
          technicianId: 1, 
          name: 'Overloaded Tech', 
          latitude: 37.7849, 
          longitude: -122.4094,
          status: 'busy',
          workload: 10, // At capacity threshold
        },
      ];
      render(
        <MapAssignmentModal 
          {...defaultProps} 
          technicians={techniciansWithCapacity}
        />
      );

      expect(screen.getByText(/at capacity/i)).toBeInTheDocument();
    });

    it('shows warning in confirmation for technician at capacity', () => {
      const techniciansWithCapacity = [
        { 
          technicianId: 1, 
          name: 'Overloaded Tech', 
          latitude: 37.7849, 
          longitude: -122.4094,
          status: 'available',
          workload: 12,
        },
      ];
      render(
        <MapAssignmentModal 
          {...defaultProps} 
          technicians={techniciansWithCapacity}
        />
      );

      const techItem = screen.getByText('Overloaded Tech').closest('[role="option"]');
      fireEvent.click(techItem);
      fireEvent.click(screen.getByRole('button', { name: /continue/i }));

      // Should show capacity warning in confirmation
      expect(screen.getByText(/at or exceeds capacity/i)).toBeInTheDocument();
    });
  });

  describe('accessibility', () => {
    it('has role="dialog"', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });

    it('has aria-modal="true"', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getByRole('dialog')).toHaveAttribute('aria-modal', 'true');
    });

    it('has aria-labelledby pointing to title', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      const modal = screen.getByRole('dialog');
      expect(modal).toHaveAttribute('aria-labelledby', 'map-assignment-modal-title');
    });

    it('technician list has role="listbox"', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getByRole('listbox')).toBeInTheDocument();
    });

    it('technician items have role="option"', () => {
      render(<MapAssignmentModal {...defaultProps} />);
      expect(screen.getAllByRole('option')).toHaveLength(2);
    });
  });
});
