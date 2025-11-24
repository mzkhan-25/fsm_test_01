import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import * as userApi from './userApi';

// Mock fetch
global.fetch = vi.fn();

describe('userApi', () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('token', 'test-token');
    fetch.mockReset();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('getAllUsers', () => {
    it('fetches all users successfully', async () => {
      const mockUsers = [
        { id: 1, name: 'User 1', email: 'user1@example.com', role: 'ADMIN' },
        { id: 2, name: 'User 2', email: 'user2@example.com', role: 'TECHNICIAN' },
      ];

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockUsers,
      });

      const result = await userApi.getAllUsers();

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/users', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer test-token',
        },
      });
      expect(result).toEqual(mockUsers);
    });

    it('throws error when fetch fails', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Unauthorized' }),
      });

      await expect(userApi.getAllUsers()).rejects.toThrow('Unauthorized');
    });

    it('throws default error when no message provided', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({}),
      });

      await expect(userApi.getAllUsers()).rejects.toThrow('Failed to fetch users');
    });
  });

  describe('getUserById', () => {
    it('fetches user by id successfully', async () => {
      const mockUser = { id: 1, name: 'User 1', email: 'user1@example.com', role: 'ADMIN' };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockUser,
      });

      const result = await userApi.getUserById(1);

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/users/1', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer test-token',
        },
      });
      expect(result).toEqual(mockUser);
    });

    it('throws error when user not found', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'User not found' }),
      });

      await expect(userApi.getUserById(999)).rejects.toThrow('User not found');
    });
  });

  describe('createUser', () => {
    it('creates user successfully', async () => {
      const userData = {
        name: 'New User',
        email: 'new@example.com',
        phone: '+1234567890',
        password: 'password123',
        role: 'TECHNICIAN',
      };
      const mockResponse = { id: 3, ...userData };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await userApi.createUser(userData);

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer test-token',
        },
        body: JSON.stringify(userData),
      });
      expect(result).toEqual(mockResponse);
    });

    it('throws error when email already exists', async () => {
      const userData = {
        name: 'New User',
        email: 'existing@example.com',
        password: 'password123',
        role: 'TECHNICIAN',
      };

      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Email already exists' }),
      });

      await expect(userApi.createUser(userData)).rejects.toThrow('Email already exists');
    });

    it('throws error when validation fails', async () => {
      const userData = {
        name: '',
        email: 'invalid-email',
        password: '',
        role: 'TECHNICIAN',
      };

      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'Validation failed' }),
      });

      await expect(userApi.createUser(userData)).rejects.toThrow('Validation failed');
    });
  });

  describe('updateUser', () => {
    it('updates user successfully', async () => {
      const updateData = {
        name: 'Updated Name',
        email: 'updated@example.com',
        phone: '+9876543210',
        role: 'SUPERVISOR',
      };
      const mockResponse = { id: 1, ...updateData };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await userApi.updateUser(1, updateData);

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/users/1', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer test-token',
        },
        body: JSON.stringify(updateData),
      });
      expect(result).toEqual(mockResponse);
    });

    it('throws error when user not found', async () => {
      const updateData = { name: 'Updated Name' };

      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'User not found' }),
      });

      await expect(userApi.updateUser(999, updateData)).rejects.toThrow('User not found');
    });
  });

  describe('deactivateUser', () => {
    it('deactivates user successfully', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      });

      const result = await userApi.deactivateUser(1);

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/users/1', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer test-token',
        },
      });
      expect(result).toBe(true);
    });

    it('throws error when user not found', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'User not found' }),
      });

      await expect(userApi.deactivateUser(999)).rejects.toThrow('User not found');
    });
  });

  describe('getAuthHeaders', () => {
    it('includes token from localStorage', async () => {
      const mockUsers = [];
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockUsers,
      });

      await userApi.getAllUsers();

      expect(fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer test-token',
          }),
        })
      );
    });

    it('handles missing token', async () => {
      localStorage.clear();
      const mockUsers = [];
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockUsers,
      });

      await userApi.getAllUsers();

      expect(fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.not.objectContaining({
            'Authorization': expect.anything(),
          }),
        })
      );
    });
  });
});
