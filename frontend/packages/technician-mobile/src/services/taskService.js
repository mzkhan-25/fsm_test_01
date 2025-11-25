const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
import { getAuthHeaders } from './authService';

/**
 * Get tasks assigned to the current technician
 * @param {string} status - Filter by task status (all, assigned, in_progress, completed)
 */
export const getAssignedTasks = async (status = 'all') => {
  const url = new URL(`${API_BASE_URL}/api/technicians/me/tasks`);
  if (status && status !== 'all') {
    url.searchParams.append('status', status);
  }

  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to fetch assigned tasks');
  }

  const data = await response.json();
  return data.tasks || data;
};

/**
 * Get a specific task by ID
 */
export const getTaskById = async (taskId) => {
  const response = await fetch(`${API_BASE_URL}/api/tasks/${taskId}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to fetch task');
  }

  return response.json();
};

/**
 * Update task status
 */
export const updateTaskStatus = async (taskId, status, notes = '') => {
  const response = await fetch(`${API_BASE_URL}/api/tasks/${taskId}/status`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify({ status, notes }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to update task status');
  }

  return response.json();
};

/**
 * Complete a task with summary
 */
export const completeTask = async (taskId, completionSummary) => {
  const response = await fetch(`${API_BASE_URL}/api/tasks/${taskId}/complete`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ completionSummary }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to complete task');
  }

  return response.json();
};

/**
 * Update technician location
 */
export const updateLocation = async (latitude, longitude) => {
  const response = await fetch(`${API_BASE_URL}/api/technicians/location`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify({ latitude, longitude }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to update location');
  }

  return response.json();
};
