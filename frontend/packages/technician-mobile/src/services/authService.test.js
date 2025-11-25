import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import {
  getAuthHeaders,
  getCurrentUser,
  isAuthenticated,
  clearAuth,
  login,
} from './authService';

// Mock fetch
global.fetch = vi.fn();

describe('authService', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('getAuthHeaders', () => {
    it('should return headers with Content-Type', () => {
      const headers = getAuthHeaders();
      expect(headers['Content-Type']).toBe('application/json');
    });

    it('should include Authorization header when token exists', () => {
      localStorage.setItem('token', 'test-token');
      const headers = getAuthHeaders();
      expect(headers['Authorization']).toBe('Bearer test-token');
    });

    it('should not include Authorization header when no token', () => {
      const headers = getAuthHeaders();
      expect(headers['Authorization']).toBeUndefined();
    });
  });

  describe('getCurrentUser', () => {
    it('should return user info from localStorage', () => {
      localStorage.setItem('userId', '123');
      localStorage.setItem('userName', 'John Doe');
      localStorage.setItem('userEmail', 'john@example.com');
      localStorage.setItem('userRole', 'TECHNICIAN');

      const user = getCurrentUser();
      expect(user).toEqual({
        id: '123',
        name: 'John Doe',
        email: 'john@example.com',
        role: 'TECHNICIAN',
      });
    });

    it('should return null values when no user data', () => {
      const user = getCurrentUser();
      expect(user).toEqual({
        id: null,
        name: null,
        email: null,
        role: null,
      });
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when token exists', () => {
      localStorage.setItem('token', 'test-token');
      expect(isAuthenticated()).toBe(true);
    });

    it('should return false when no token', () => {
      expect(isAuthenticated()).toBe(false);
    });
  });

  describe('clearAuth', () => {
    it('should remove all auth data from localStorage', () => {
      localStorage.setItem('token', 'test-token');
      localStorage.setItem('userId', '123');
      localStorage.setItem('userName', 'John');
      localStorage.setItem('userEmail', 'john@example.com');
      localStorage.setItem('userRole', 'TECHNICIAN');

      clearAuth();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('userId')).toBeNull();
      expect(localStorage.getItem('userName')).toBeNull();
      expect(localStorage.getItem('userEmail')).toBeNull();
      expect(localStorage.getItem('userRole')).toBeNull();
    });
  });

  describe('login', () => {
    it('should login successfully and store user data', async () => {
      const mockResponse = {
        token: 'jwt-token',
        userId: '123',
        name: 'John Doe',
        email: 'john@example.com',
        role: 'TECHNICIAN',
      };

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse),
      });

      const result = await login('john@example.com', 'password123');

      expect(result).toEqual(mockResponse);
      expect(localStorage.getItem('token')).toBe('jwt-token');
      expect(localStorage.getItem('userId')).toBe('123');
      expect(localStorage.getItem('userName')).toBe('John Doe');
      expect(localStorage.getItem('userEmail')).toBe('john@example.com');
      expect(localStorage.getItem('userRole')).toBe('TECHNICIAN');
    });

    it('should throw error on failed login', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ message: 'Invalid credentials' }),
      });

      await expect(login('john@example.com', 'wrong-password')).rejects.toThrow(
        'Invalid credentials'
      );
    });

    it('should throw default error when no message provided', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({}),
      });

      await expect(login('john@example.com', 'wrong-password')).rejects.toThrow(
        'Invalid credentials'
      );
    });

    it('should send mobile flag in request body', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: () =>
          Promise.resolve({
            token: 'token',
            userId: '1',
            name: 'Test',
            email: 'test@example.com',
            role: 'TECHNICIAN',
          }),
      });

      await login('test@example.com', 'password');

      expect(global.fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          body: expect.stringContaining('"mobile":true'),
        })
      );
    });
  });
});
