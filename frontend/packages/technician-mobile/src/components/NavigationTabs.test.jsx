import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import NavigationTabs from './NavigationTabs';

describe('NavigationTabs', () => {
  it('should render all navigation tabs', () => {
    render(<NavigationTabs activeTab="tasks" onTabChange={() => {}} />);

    expect(screen.getByText('Tasks')).toBeInTheDocument();
    expect(screen.getByText('Map')).toBeInTheDocument();
    expect(screen.getByText('Profile')).toBeInTheDocument();
  });

  it('should mark the active tab', () => {
    render(<NavigationTabs activeTab="tasks" onTabChange={() => {}} />);

    const tasksButton = screen.getByLabelText('Tasks');
    const mapButton = screen.getByLabelText('Map');
    const profileButton = screen.getByLabelText('Profile');

    expect(tasksButton).toHaveClass('active');
    expect(mapButton).not.toHaveClass('active');
    expect(profileButton).not.toHaveClass('active');
  });

  it('should mark map tab as active when activeTab is map', () => {
    render(<NavigationTabs activeTab="map" onTabChange={() => {}} />);

    const mapButton = screen.getByLabelText('Map');
    expect(mapButton).toHaveClass('active');
  });

  it('should call onTabChange when tab is clicked', () => {
    const mockOnTabChange = vi.fn();
    render(<NavigationTabs activeTab="tasks" onTabChange={mockOnTabChange} />);

    fireEvent.click(screen.getByLabelText('Map'));
    expect(mockOnTabChange).toHaveBeenCalledWith('map');

    fireEvent.click(screen.getByLabelText('Profile'));
    expect(mockOnTabChange).toHaveBeenCalledWith('profile');
  });

  it('should have aria-current on active tab', () => {
    render(<NavigationTabs activeTab="profile" onTabChange={() => {}} />);

    const profileButton = screen.getByLabelText('Profile');
    expect(profileButton).toHaveAttribute('aria-current', 'page');
  });

  it('should not have aria-current on inactive tabs', () => {
    render(<NavigationTabs activeTab="tasks" onTabChange={() => {}} />);

    const mapButton = screen.getByLabelText('Map');
    expect(mapButton).not.toHaveAttribute('aria-current');
  });

  it('should render navigation element with proper aria-label', () => {
    render(<NavigationTabs activeTab="tasks" onTabChange={() => {}} />);

    const nav = screen.getByRole('navigation');
    expect(nav).toHaveAttribute('aria-label', 'Main navigation');
  });

  it('should render icons for each tab', () => {
    render(<NavigationTabs activeTab="tasks" onTabChange={() => {}} />);

    expect(screen.getByText('📋')).toBeInTheDocument();
    expect(screen.getByText('🗺️')).toBeInTheDocument();
    expect(screen.getByText('👤')).toBeInTheDocument();
  });
});
