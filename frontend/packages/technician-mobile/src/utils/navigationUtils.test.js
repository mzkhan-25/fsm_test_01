import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { detectPlatform, openNavigation, extractCoordinatesFromAddress } from './navigationUtils';

describe('navigationUtils', () => {
  let mockOpen;

  beforeEach(() => {
    // Mock window.open
    mockOpen = vi.fn();
    Object.defineProperty(window, 'open', {
      value: mockOpen,
      writable: true,
      configurable: true,
    });

    // Mock console methods
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    // Restore mocks
    vi.restoreAllMocks();
    mockOpen.mockClear();
  });

  describe('detectPlatform', () => {
    it('should detect iOS platform from iPhone user agent', () => {
      Object.defineProperty(navigator, 'userAgent', {
        value: 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)',
        configurable: true,
      });

      expect(detectPlatform()).toBe('ios');
    });

    it('should detect iOS platform from iPad user agent', () => {
      Object.defineProperty(navigator, 'userAgent', {
        value: 'Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X)',
        configurable: true,
      });

      expect(detectPlatform()).toBe('ios');
    });

    it('should detect iOS platform from iPod user agent', () => {
      Object.defineProperty(navigator, 'userAgent', {
        value: 'Mozilla/5.0 (iPod; CPU iPhone OS 14_0 like Mac OS X)',
        configurable: true,
      });

      expect(detectPlatform()).toBe('ios');
    });

    it('should detect Android platform', () => {
      Object.defineProperty(navigator, 'userAgent', {
        value: 'Mozilla/5.0 (Linux; Android 10; SM-G973F)',
        configurable: true,
      });

      expect(detectPlatform()).toBe('android');
    });

    it('should return "other" for desktop browsers', () => {
      Object.defineProperty(navigator, 'userAgent', {
        value: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        configurable: true,
      });

      expect(detectPlatform()).toBe('other');
    });

    it('should handle missing userAgent gracefully', () => {
      Object.defineProperty(navigator, 'userAgent', {
        value: '',
        configurable: true,
      });

      expect(detectPlatform()).toBe('other');
    });
  });

  describe('openNavigation', () => {
    describe('iOS platform', () => {
      beforeEach(() => {
        Object.defineProperty(navigator, 'userAgent', {
          value: 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)',
          configurable: true,
        });
      });

      it('should open Apple Maps with address on iOS', () => {
        const result = openNavigation({ address: '123 Main St, City, State' });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledWith(
          'maps://maps.apple.com/?daddr=123%20Main%20St%2C%20City%2C%20State',
          '_blank'
        );
      });

      it('should open Apple Maps with coordinates on iOS', () => {
        const result = openNavigation({ 
          address: '123 Main St',
          latitude: 40.7128, 
          longitude: -74.0060 
        });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledWith(
          'maps://maps.apple.com/?daddr=40.7128,-74.006',
          '_blank'
        );
      });

      it('should prefer coordinates over address on iOS', () => {
        const result = openNavigation({ 
          address: '123 Main St',
          latitude: 40.7128, 
          longitude: -74.0060 
        });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledWith(
          expect.stringContaining('40.7128,-74.006'),
          '_blank'
        );
      });
    });

    describe('Android platform', () => {
      beforeEach(() => {
        Object.defineProperty(navigator, 'userAgent', {
          value: 'Mozilla/5.0 (Linux; Android 10; SM-G973F)',
          configurable: true,
        });
      });

      it('should open Google Maps with address on Android', () => {
        const result = openNavigation({ address: '123 Main St, City, State' });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledWith(
          'google.navigation:q=123%20Main%20St%2C%20City%2C%20State',
          '_blank'
        );
      });

      it('should open Google Maps with coordinates on Android', () => {
        const result = openNavigation({ 
          address: '123 Main St',
          latitude: 40.7128, 
          longitude: -74.0060 
        });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledWith(
          'google.navigation:q=40.7128,-74.006',
          '_blank'
        );
      });

      it('should prefer coordinates over address on Android', () => {
        const result = openNavigation({ 
          address: '123 Main St',
          latitude: 40.7128, 
          longitude: -74.0060 
        });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledWith(
          expect.stringContaining('40.7128,-74.006'),
          '_blank'
        );
      });
    });

    describe('Other platforms', () => {
      beforeEach(() => {
        Object.defineProperty(navigator, 'userAgent', {
          value: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
          configurable: true,
        });
      });

      it('should open Google Maps web with address on other platforms', () => {
        const result = openNavigation({ address: '123 Main St, City, State' });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledWith(
          'https://www.google.com/maps/dir/?api=1&destination=123%20Main%20St%2C%20City%2C%20State',
          '_blank'
        );
      });

      it('should open Google Maps web with coordinates on other platforms', () => {
        const result = openNavigation({ 
          latitude: 40.7128, 
          longitude: -74.0060 
        });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledWith(
          'https://www.google.com/maps/dir/?api=1&destination=40.7128,-74.006',
          '_blank'
        );
      });
    });

    describe('Error handling', () => {
      it('should return false when neither address nor coordinates provided', () => {
        const result = openNavigation({});

        expect(result).toBe(false);
        expect(console.error).toHaveBeenCalledWith(
          'Navigation error: Either address or coordinates must be provided'
        );
      });

      it('should return false when only latitude provided', () => {
        const result = openNavigation({ latitude: 40.7128 });

        expect(result).toBe(false);
        expect(console.error).toHaveBeenCalledWith(
          'Navigation error: Either address or coordinates must be provided'
        );
      });

      it('should return false when only longitude provided', () => {
        const result = openNavigation({ longitude: -74.0060 });

        expect(result).toBe(false);
        expect(console.error).toHaveBeenCalledWith(
          'Navigation error: Either address or coordinates must be provided'
        );
      });

      it('should use fallback when window.open throws error', () => {
        mockOpen
          .mockImplementationOnce(() => {
            throw new Error('Primary navigation failed');
          })
          .mockImplementationOnce(() => {});

        const result = openNavigation({ address: '123 Main St' });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledTimes(2);
        expect(console.error).toHaveBeenCalledWith(
          'Failed to open navigation:',
          expect.any(Error)
        );
      });

      it('should return false when both primary and fallback fail', () => {
        mockOpen.mockImplementation(() => {
          throw new Error('Navigation failed');
        });

        const result = openNavigation({ address: '123 Main St' });

        expect(result).toBe(false);
        expect(console.error).toHaveBeenCalledWith(
          'Fallback navigation also failed:',
          expect.any(Error)
        );
      });

      it('should use coordinates in fallback when address not available', () => {
        mockOpen
          .mockImplementationOnce(() => {
            throw new Error('Primary navigation failed');
          })
          .mockImplementationOnce(() => {});

        const result = openNavigation({ 
          latitude: 40.7128, 
          longitude: -74.0060 
        });

        expect(result).toBe(true);
        expect(mockOpen).toHaveBeenCalledWith(
          expect.stringContaining('40.7128,-74.006'),
          '_blank'
        );
      });
    });
  });

  describe('extractCoordinatesFromAddress', () => {
    it('should extract coordinates from address with parentheses', () => {
      const result = extractCoordinatesFromAddress('123 Main St (40.7128, -74.0060)');

      expect(result).toEqual({
        latitude: 40.7128,
        longitude: -74.0060,
      });
    });

    it('should extract coordinates from address with square brackets', () => {
      const result = extractCoordinatesFromAddress('123 Main St [40.7128, -74.0060]');

      expect(result).toEqual({
        latitude: 40.7128,
        longitude: -74.0060,
      });
    });

    it('should extract coordinates from address with curly braces', () => {
      const result = extractCoordinatesFromAddress('123 Main St {40.7128, -74.0060}');

      expect(result).toEqual({
        latitude: 40.7128,
        longitude: -74.0060,
      });
    });

    it('should handle coordinates with no decimal places', () => {
      const result = extractCoordinatesFromAddress('Address (40, -74)');

      expect(result).toEqual({
        latitude: 40,
        longitude: -74,
      });
    });

    it('should handle coordinates with extra spaces', () => {
      const result = extractCoordinatesFromAddress('Address (40.7128,  -74.0060)');

      expect(result).toEqual({
        latitude: 40.7128,
        longitude: -74.0060,
      });
    });

    it('should return null for address without coordinates', () => {
      const result = extractCoordinatesFromAddress('123 Main St, City, State');

      expect(result).toBeNull();
    });

    it('should return null for empty string', () => {
      const result = extractCoordinatesFromAddress('');

      expect(result).toBeNull();
    });

    it('should return null for null input', () => {
      const result = extractCoordinatesFromAddress(null);

      expect(result).toBeNull();
    });

    it('should return null for undefined input', () => {
      const result = extractCoordinatesFromAddress(undefined);

      expect(result).toBeNull();
    });

    it('should return null for non-string input', () => {
      const result = extractCoordinatesFromAddress(123);

      expect(result).toBeNull();
    });

    it('should return null for invalid latitude range', () => {
      const result = extractCoordinatesFromAddress('Address (91, -74.0060)');

      expect(result).toBeNull();
    });

    it('should return null for invalid longitude range', () => {
      const result = extractCoordinatesFromAddress('Address (40.7128, -181)');

      expect(result).toBeNull();
    });

    it('should handle negative coordinates', () => {
      const result = extractCoordinatesFromAddress('Address (-40.7128, -74.0060)');

      expect(result).toEqual({
        latitude: -40.7128,
        longitude: -74.0060,
      });
    });

    it('should handle positive coordinates', () => {
      const result = extractCoordinatesFromAddress('Address (40.7128, 74.0060)');

      expect(result).toEqual({
        latitude: 40.7128,
        longitude: 74.0060,
      });
    });

    it('should handle boundary latitude values', () => {
      const result1 = extractCoordinatesFromAddress('Address (90, 0)');
      const result2 = extractCoordinatesFromAddress('Address (-90, 0)');

      expect(result1).toEqual({ latitude: 90, longitude: 0 });
      expect(result2).toEqual({ latitude: -90, longitude: 0 });
    });

    it('should handle boundary longitude values', () => {
      const result1 = extractCoordinatesFromAddress('Address (0, 180)');
      const result2 = extractCoordinatesFromAddress('Address (0, -180)');

      expect(result1).toEqual({ latitude: 0, longitude: 180 });
      expect(result2).toEqual({ latitude: 0, longitude: -180 });
    });

    it('should extract first occurrence when multiple coordinate patterns exist', () => {
      const result = extractCoordinatesFromAddress('Address (40.7128, -74.0060) near (50, 60)');

      expect(result).toEqual({
        latitude: 40.7128,
        longitude: -74.0060,
      });
    });
  });
});
