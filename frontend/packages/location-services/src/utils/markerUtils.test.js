import { describe, it, expect, vi } from 'vitest';
import L from 'leaflet';
import { createTaskMarkerIcon, createTechnicianMarkerIcon, createHighlightedTechnicianMarkerIcon } from './markerUtils';

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

  describe('createTechnicianMarkerIcon', () => {
    it('creates divIcon with correct className', () => {
      createTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          className: 'technician-marker-icon',
        })
      );
    });

    it('creates icon with correct size', () => {
      createTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          iconSize: [32, 40],
        })
      );
    });

    it('creates icon with correct anchor', () => {
      createTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          iconAnchor: [16, 40],
        })
      );
    });

    it('creates icon with correct popup anchor', () => {
      createTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          popupAnchor: [0, -40],
        })
      );
    });

    it('creates icon with green color for available status', () => {
      createTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('background-color: #28a745'),
        })
      );
    });

    it('creates icon with yellow color for busy status', () => {
      createTechnicianMarkerIcon('busy');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('background-color: #ffc107'),
        })
      );
    });

    it('creates icon with gray color for offline status', () => {
      createTechnicianMarkerIcon('offline');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('background-color: #6c757d'),
        })
      );
    });

    it('creates icon with person SVG shape', () => {
      createTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('<svg'),
        })
      );
    });

    it('uses different SVG path than task marker', () => {
      const taskIcon = createTaskMarkerIcon('HIGH');
      const techIcon = createTechnicianMarkerIcon('available');
      
      expect(taskIcon.html).not.toBe(techIcon.html);
    });
  });

  describe('createHighlightedTechnicianMarkerIcon', () => {
    it('creates divIcon with highlighted className', () => {
      createHighlightedTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          className: 'technician-marker-icon highlighted',
        })
      );
    });

    it('creates icon with larger size than regular technician icon', () => {
      createHighlightedTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          iconSize: [36, 44],
        })
      );
    });

    it('creates icon with adjusted anchor for larger size', () => {
      createHighlightedTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          iconAnchor: [18, 44],
        })
      );
    });

    it('creates icon with adjusted popup anchor for larger size', () => {
      createHighlightedTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          popupAnchor: [0, -44],
        })
      );
    });

    it('creates icon with box-shadow for highlight effect', () => {
      createHighlightedTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('box-shadow'),
        })
      );
    });

    it('creates icon with green color for available status', () => {
      createHighlightedTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('background-color: #28a745'),
        })
      );
    });

    it('creates icon with yellow color for busy status', () => {
      createHighlightedTechnicianMarkerIcon('busy');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('background-color: #ffc107'),
        })
      );
    });

    it('creates highlighted class on inner element', () => {
      createHighlightedTechnicianMarkerIcon('available');
      
      expect(L.divIcon).toHaveBeenCalledWith(
        expect.objectContaining({
          html: expect.stringContaining('class="technician-marker highlighted"'),
        })
      );
    });
  });
});
