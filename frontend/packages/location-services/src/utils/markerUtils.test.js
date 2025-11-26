import { describe, it, expect, vi } from 'vitest';
import L from 'leaflet';
import { createTaskMarkerIcon } from './markerUtils';

// Mock Leaflet divIcon
vi.mock('leaflet', () => ({
  default: {
    divIcon: vi.fn((config) => config),
  },
}));

describe('markerUtils', () => {
  describe('createTaskMarkerIcon', () => {
    it('creates divIcon with correct className', () => {
      createTaskMarkerIcon('HIGH');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          className: 'task-marker-icon',
        })
      );
    });

    it('creates icon with correct size', () => {
      createTaskMarkerIcon('HIGH');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          iconSize: [32, 40],
        })
      );
    });

    it('creates icon with correct anchor', () => {
      createTaskMarkerIcon('HIGH');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          iconAnchor: [16, 40],
        })
      );
    });

    it('creates icon with correct popup anchor', () => {
      createTaskMarkerIcon('HIGH');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          popupAnchor: [0, -40],
        })
      );
    });

    it('creates icon with red color for HIGH priority', () => {
      createTaskMarkerIcon('HIGH');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('background-color: #dc3545'),
        })
      );
    });

    it('creates icon with orange color for MEDIUM priority', () => {
      createTaskMarkerIcon('MEDIUM');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('background-color: #fd7e14'),
        })
      );
    });

    it('creates icon with yellow color for LOW priority', () => {
      createTaskMarkerIcon('LOW');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('background-color: #ffc107'),
        })
      );
    });

    it('creates icon with gray color for unknown priority', () => {
      createTaskMarkerIcon('UNKNOWN');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('background-color: #6c757d'),
        })
      );
    });

    it('creates icon with SVG marker shape', () => {
      createTaskMarkerIcon('HIGH');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('<svg'),
        })
      );
    });
  });
});
