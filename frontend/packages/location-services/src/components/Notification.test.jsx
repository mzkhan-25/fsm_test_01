import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import Notification from './Notification';

describe('Notification', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  describe('rendering', () => {
    it('renders nothing when isVisible is false', () => {
      const { container } = render(
        <Notification message="Test message" isVisible={false} />
      );
      expect(container.firstChild).toBeNull();
    });

    it('renders notification when isVisible is true', () => {
      render(<Notification message="Test message" isVisible={true} />);
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    it('displays the message', () => {
      render(<Notification message="Success!" isVisible={true} />);
      expect(screen.getByText('Success!')).toBeInTheDocument();
    });

    it('renders close button', () => {
      render(<Notification message="Test" isVisible={true} />);
      expect(screen.getByRole('button', { name: /close notification/i })).toBeInTheDocument();
    });
  });

  describe('notification types', () => {
    it('applies success type class', () => {
      render(<Notification message="Success" type="success" isVisible={true} />);
      expect(screen.getByRole('alert')).toHaveClass('notification-success');
    });

    it('applies error type class', () => {
      render(<Notification message="Error" type="error" isVisible={true} />);
      expect(screen.getByRole('alert')).toHaveClass('notification-error');
    });

    it('applies warning type class', () => {
      render(<Notification message="Warning" type="warning" isVisible={true} />);
      expect(screen.getByRole('alert')).toHaveClass('notification-warning');
    });

    it('applies info type class by default', () => {
      render(<Notification message="Info" isVisible={true} />);
      expect(screen.getByRole('alert')).toHaveClass('notification-info');
    });

    it('shows success icon for success type', () => {
      render(<Notification message="Success" type="success" isVisible={true} />);
      expect(screen.getByText('✓')).toBeInTheDocument();
    });

    it('shows error icon for error type', () => {
      render(<Notification message="Error" type="error" isVisible={true} />);
      expect(screen.getByText('✕')).toBeInTheDocument();
    });

    it('shows warning icon for warning type', () => {
      render(<Notification message="Warning" type="warning" isVisible={true} />);
      expect(screen.getByText('⚠')).toBeInTheDocument();
    });

    it('shows info icon for info type', () => {
      render(<Notification message="Info" type="info" isVisible={true} />);
      expect(screen.getByText('ℹ')).toBeInTheDocument();
    });

    it('shows info icon for unknown type', () => {
      render(<Notification message="Unknown" type="unknown" isVisible={true} />);
      expect(screen.getByText('ℹ')).toBeInTheDocument();
    });
  });

  describe('auto-close', () => {
    it('calls onClose after duration', async () => {
      const onClose = vi.fn();
      render(
        <Notification 
          message="Test" 
          isVisible={true} 
          duration={3000} 
          onClose={onClose} 
        />
      );

      expect(onClose).not.toHaveBeenCalled();
      
      // Fast-forward past duration + exit animation
      vi.advanceTimersByTime(3000);
      vi.advanceTimersByTime(300);

      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it('does not auto-close when duration is 0', async () => {
      const onClose = vi.fn();
      render(
        <Notification 
          message="Test" 
          isVisible={true} 
          duration={0} 
          onClose={onClose} 
        />
      );

      vi.advanceTimersByTime(10000);

      expect(onClose).not.toHaveBeenCalled();
    });

    it('clears timeout on unmount', () => {
      const onClose = vi.fn();
      const { unmount } = render(
        <Notification 
          message="Test" 
          isVisible={true} 
          duration={3000} 
          onClose={onClose} 
        />
      );

      unmount();
      vi.advanceTimersByTime(5000);

      expect(onClose).not.toHaveBeenCalled();
    });
  });

  describe('manual close', () => {
    it('calls onClose when close button is clicked', async () => {
      const onClose = vi.fn();
      render(
        <Notification 
          message="Test" 
          isVisible={true} 
          onClose={onClose} 
          duration={0}
        />
      );

      fireEvent.click(screen.getByRole('button', { name: /close notification/i }));
      
      // Wait for exit animation
      vi.advanceTimersByTime(300);

      expect(onClose).toHaveBeenCalledTimes(1);
    });

    it('applies exiting class when closing', async () => {
      const onClose = vi.fn();
      render(
        <Notification 
          message="Test" 
          isVisible={true} 
          onClose={onClose} 
          duration={0}
        />
      );

      fireEvent.click(screen.getByRole('button', { name: /close notification/i }));

      expect(screen.getByRole('alert')).toHaveClass('notification-exiting');
    });

    it('handles close without onClose callback', async () => {
      render(
        <Notification 
          message="Test" 
          isVisible={true} 
          duration={0}
        />
      );

      // Should not throw
      fireEvent.click(screen.getByRole('button', { name: /close notification/i }));
      vi.advanceTimersByTime(300);
    });
  });

  describe('accessibility', () => {
    it('has role="alert"', () => {
      render(<Notification message="Test" isVisible={true} />);
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    it('has aria-live="polite"', () => {
      render(<Notification message="Test" isVisible={true} />);
      expect(screen.getByRole('alert')).toHaveAttribute('aria-live', 'polite');
    });

    it('icon has aria-hidden="true"', () => {
      render(<Notification message="Test" isVisible={true} />);
      const icon = screen.getByText('ℹ');
      expect(icon).toHaveAttribute('aria-hidden', 'true');
    });

    it('close button has accessible label', () => {
      render(<Notification message="Test" isVisible={true} />);
      expect(screen.getByRole('button')).toHaveAttribute('aria-label', 'Close notification');
    });
  });

  describe('default props', () => {
    it('uses default duration of 3000ms', () => {
      const onClose = vi.fn();
      render(
        <Notification message="Test" isVisible={true} onClose={onClose} />
      );

      vi.advanceTimersByTime(3000);
      vi.advanceTimersByTime(300);

      expect(onClose).toHaveBeenCalled();
    });

    it('uses info type by default', () => {
      render(<Notification message="Test" isVisible={true} />);
      expect(screen.getByRole('alert')).toHaveClass('notification-info');
    });
  });
});
