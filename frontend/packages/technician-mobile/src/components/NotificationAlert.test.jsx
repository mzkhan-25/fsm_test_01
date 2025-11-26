import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import NotificationAlert from './NotificationAlert';
import * as notificationService from '../services/notificationService';

// Mock the notification service
vi.mock('../services/notificationService', () => ({
  addNotificationListener: vi.fn(),
}));

describe('NotificationAlert', () => {
  let mockUnsubscribe;
  let mockListener;

  beforeEach(() => {
    mockUnsubscribe = vi.fn();
    vi.mocked(notificationService.addNotificationListener).mockImplementation((listener) => {
      mockListener = listener;
      return mockUnsubscribe;
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
    vi.clearAllTimers();
    mockListener = null;
  });

  it('should not render when no notification', () => {
    const { container } = render(<NotificationAlert />);
    expect(container.querySelector('.notification-alert')).toBeNull();
  });

  it('should register notification listener on mount', () => {
    render(<NotificationAlert />);
    expect(notificationService.addNotificationListener).toHaveBeenCalled();
  });

  it('should unsubscribe on unmount', () => {
    const { unmount } = render(<NotificationAlert />);
    unmount();
    expect(mockUnsubscribe).toHaveBeenCalled();
  });

  it('should display notification when foreground_notification event received', async () => {
    render(<NotificationAlert />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'New Task Assigned',
          message: 'Task #123 has been assigned to you',
          data: { taskId: 123 },
        },
      });
    });

    expect(screen.getByText('New Task Assigned')).toBeInTheDocument();
    expect(screen.getByText('Task #123 has been assigned to you')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('should have visible class when notification is displayed', () => {
    const { container } = render(<NotificationAlert />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'Test',
          message: 'Test message',
        },
      });
    });

    expect(container.querySelector('.notification-alert')).toHaveClass('visible');
  });

  it('should dismiss notification when close button clicked', async () => {
    vi.useFakeTimers();
    const { container } = render(<NotificationAlert />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'Test',
          message: 'Test message',
        },
      });
    });

    expect(container.querySelector('.notification-alert.visible')).toBeInTheDocument();

    const closeButton = screen.getByRole('button', { name: /dismiss/i });
    fireEvent.click(closeButton);

    // Wait for animation timeout
    act(() => {
      vi.advanceTimersByTime(300);
    });

    expect(container.querySelector('.notification-alert')).toBeNull();
    vi.useRealTimers();
  });

  it('should call onNotificationTap when notification clicked', async () => {
    vi.useFakeTimers();
    const onNotificationTap = vi.fn();
    render(<NotificationAlert onNotificationTap={onNotificationTap} />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'New Task',
          message: 'Task assigned',
          data: { taskId: 456 },
        },
      });
    });

    const content = screen.getByText('New Task').closest('.notification-alert-content');
    fireEvent.click(content);

    expect(onNotificationTap).toHaveBeenCalledWith(456);

    // Wait for dismiss animation
    act(() => {
      vi.advanceTimersByTime(300);
    });
    vi.useRealTimers();
  });

  it('should parse string data in notification', async () => {
    vi.useFakeTimers();
    const onNotificationTap = vi.fn();
    render(<NotificationAlert onNotificationTap={onNotificationTap} />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'New Task',
          message: 'Task assigned',
          data: JSON.stringify({ taskId: 789 }),
        },
      });
    });

    const content = screen.getByText('New Task').closest('.notification-alert-content');
    fireEvent.click(content);

    expect(onNotificationTap).toHaveBeenCalledWith(789);
    vi.useRealTimers();
  });

  it('should auto-dismiss after 5 seconds', async () => {
    vi.useFakeTimers();
    const { container } = render(<NotificationAlert />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'Test',
          message: 'Test message',
        },
      });
    });

    expect(container.querySelector('.notification-alert.visible')).toBeInTheDocument();

    // Advance past auto-dismiss time (5 seconds) + animation time (300ms)
    act(() => {
      vi.advanceTimersByTime(5300);
    });

    expect(container.querySelector('.notification-alert')).toBeNull();
    vi.useRealTimers();
  });

  it('should ignore non-foreground notification events', () => {
    const { container } = render(<NotificationAlert />);

    act(() => {
      mockListener({
        type: 'notification_tap',
        taskId: 123,
      });
    });

    expect(container.querySelector('.notification-alert')).toBeNull();
  });

  it('should not call onNotificationTap if no taskId in data', () => {
    vi.useFakeTimers();
    const onNotificationTap = vi.fn();
    render(<NotificationAlert onNotificationTap={onNotificationTap} />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'Test Title',
          message: 'Test Message',
          data: {},
        },
      });
    });

    const content = screen.getByText('Test Title').closest('.notification-alert-content');
    fireEvent.click(content);

    expect(onNotificationTap).not.toHaveBeenCalled();
    vi.useRealTimers();
  });

  it('should display bell icon', () => {
    render(<NotificationAlert />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'Test',
          message: 'Test',
        },
      });
    });

    expect(screen.getByText('🔔')).toBeInTheDocument();
  });

  it('should have correct accessibility attributes', () => {
    render(<NotificationAlert />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'Test',
          message: 'Test',
        },
      });
    });

    const alert = screen.getByRole('alert');
    expect(alert).toHaveAttribute('aria-live', 'assertive');
  });

  it('should stop click propagation when close button clicked', () => {
    vi.useFakeTimers();
    const onNotificationTap = vi.fn();
    render(<NotificationAlert onNotificationTap={onNotificationTap} />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'Test',
          message: 'Test',
          data: { taskId: 123 },
        },
      });
    });

    const closeButton = screen.getByRole('button', { name: /dismiss/i });
    fireEvent.click(closeButton);

    // onNotificationTap should NOT be called when close is clicked
    expect(onNotificationTap).not.toHaveBeenCalled();
    vi.useRealTimers();
  });

  it('should handle notification without onNotificationTap prop', () => {
    vi.useFakeTimers();
    render(<NotificationAlert />);

    act(() => {
      mockListener({
        type: 'foreground_notification',
        notification: {
          title: 'Test Title No Tap',
          message: 'Test Message No Tap',
          data: { taskId: 123 },
        },
      });
    });

    const content = screen.getByText('Test Title No Tap').closest('.notification-alert-content');
    // Should not throw when clicked without handler
    expect(() => fireEvent.click(content)).not.toThrow();
    vi.useRealTimers();
  });
});
