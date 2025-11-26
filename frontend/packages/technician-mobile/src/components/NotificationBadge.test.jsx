import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import NotificationBadge from './NotificationBadge';
import * as notificationService from '../services/notificationService';

// Mock the notification service
vi.mock('../services/notificationService', () => ({
  addBadgeListener: vi.fn(),
}));

describe('NotificationBadge', () => {
  let mockUnsubscribe;
  let mockListener;

  beforeEach(() => {
    mockUnsubscribe = vi.fn();
    vi.mocked(notificationService.addBadgeListener).mockImplementation((listener) => {
      mockListener = listener;
      // Call listener immediately with initial count of 0
      listener(0);
      return mockUnsubscribe;
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
    mockListener = null;
  });

  it('should not render when count is 0', () => {
    const { container } = render(<NotificationBadge />);
    expect(container.querySelector('.notification-badge')).toBeNull();
  });

  it('should register badge listener on mount', () => {
    render(<NotificationBadge />);
    expect(notificationService.addBadgeListener).toHaveBeenCalled();
  });

  it('should unsubscribe on unmount', () => {
    const { unmount } = render(<NotificationBadge />);
    unmount();
    expect(mockUnsubscribe).toHaveBeenCalled();
  });

  it('should display count when greater than 0', () => {
    vi.mocked(notificationService.addBadgeListener).mockImplementation((listener) => {
      mockListener = listener;
      listener(5);
      return mockUnsubscribe;
    });

    render(<NotificationBadge />);
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  it('should update when count changes', () => {
    render(<NotificationBadge />);

    act(() => {
      mockListener(3);
    });

    expect(screen.getByText('3')).toBeInTheDocument();

    act(() => {
      mockListener(10);
    });

    expect(screen.getByText('10')).toBeInTheDocument();
  });

  it('should display 99+ for counts over 99', () => {
    vi.mocked(notificationService.addBadgeListener).mockImplementation((listener) => {
      mockListener = listener;
      listener(150);
      return mockUnsubscribe;
    });

    render(<NotificationBadge />);
    expect(screen.getByText('99+')).toBeInTheDocument();
  });

  it('should display exactly 99 without plus', () => {
    vi.mocked(notificationService.addBadgeListener).mockImplementation((listener) => {
      mockListener = listener;
      listener(99);
      return mockUnsubscribe;
    });

    render(<NotificationBadge />);
    expect(screen.getByText('99')).toBeInTheDocument();
  });

  it('should have correct accessibility label for single notification', () => {
    vi.mocked(notificationService.addBadgeListener).mockImplementation((listener) => {
      mockListener = listener;
      listener(1);
      return mockUnsubscribe;
    });

    render(<NotificationBadge />);
    expect(screen.getByLabelText('1 unread notification')).toBeInTheDocument();
  });

  it('should have correct accessibility label for multiple notifications', () => {
    vi.mocked(notificationService.addBadgeListener).mockImplementation((listener) => {
      mockListener = listener;
      listener(5);
      return mockUnsubscribe;
    });

    render(<NotificationBadge />);
    expect(screen.getByLabelText('5 unread notifications')).toBeInTheDocument();
  });

  it('should apply custom className', () => {
    vi.mocked(notificationService.addBadgeListener).mockImplementation((listener) => {
      mockListener = listener;
      listener(5);
      return mockUnsubscribe;
    });

    const { container } = render(<NotificationBadge className="custom-class" />);
    expect(container.querySelector('.notification-badge')).toHaveClass('custom-class');
  });

  it('should hide badge when count becomes 0', () => {
    vi.mocked(notificationService.addBadgeListener).mockImplementation((listener) => {
      mockListener = listener;
      listener(5);
      return mockUnsubscribe;
    });

    const { container } = render(<NotificationBadge />);
    expect(container.querySelector('.notification-badge')).toBeInTheDocument();

    act(() => {
      mockListener(0);
    });

    expect(container.querySelector('.notification-badge')).toBeNull();
  });

  it('should have notification-badge class', () => {
    vi.mocked(notificationService.addBadgeListener).mockImplementation((listener) => {
      mockListener = listener;
      listener(3);
      return mockUnsubscribe;
    });

    const { container } = render(<NotificationBadge />);
    expect(container.querySelector('.notification-badge')).toBeInTheDocument();
  });
});
