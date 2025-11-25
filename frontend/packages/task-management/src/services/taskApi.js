const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

/**
 * Get auth headers with token
 */
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return headers;
};

/**
 * Create a new task
 * @param {Object} taskData - Task creation data
 * @param {string} taskData.title - Task title (required, min 3 chars)
 * @param {string} taskData.description - Task description
 * @param {string} taskData.clientAddress - Client address (required)
 * @param {string} taskData.priority - Priority level (LOW, MEDIUM, HIGH, URGENT)
 * @param {number} taskData.estimatedDuration - Estimated duration in minutes
 * @returns {Promise<Object>} Created task response
 */
export const createTask = async (taskData) => {
  const response = await fetch(`${API_BASE_URL}/api/tasks`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(taskData),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to create task');
  }

  return response.json();
};

/**
 * Get tasks with filtering, sorting, and pagination
 * @param {Object} params - Query parameters
 * @param {string} params.status - Filter by task status (UNASSIGNED, ASSIGNED, IN_PROGRESS, COMPLETED)
 * @param {string} params.priority - Filter by task priority (LOW, MEDIUM, HIGH, URGENT)
 * @param {string} params.search - Search term for title, id, and client address
 * @param {string} params.sortBy - Field to sort by (priority, createdAt, status)
 * @param {string} params.sortOrder - Sort order (asc, desc)
 * @param {number} params.page - Page number (0-based)
 * @param {number} params.pageSize - Number of items per page
 * @returns {Promise<Object>} Task list response with tasks, pagination, and status counts
 */
export const getTasks = async (params = {}) => {
  const queryParams = new URLSearchParams();
  
  if (params.status) queryParams.append('status', params.status);
  if (params.priority) queryParams.append('priority', params.priority);
  if (params.search) queryParams.append('search', params.search);
  if (params.sortBy) queryParams.append('sortBy', params.sortBy);
  if (params.sortOrder) queryParams.append('sortOrder', params.sortOrder);
  if (params.page !== undefined) queryParams.append('page', params.page);
  if (params.pageSize !== undefined) queryParams.append('pageSize', params.pageSize);

  const queryString = queryParams.toString();
  const url = `${API_BASE_URL}/api/tasks${queryString ? `?${queryString}` : ''}`;

  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to fetch tasks');
  }

  return response.json();
};

/**
 * Get address suggestions for autocomplete
 * @param {string} partialAddress - Partial address to search for (min 3 chars)
 * @returns {Promise<Array>} List of address suggestions
 */
export const getAddressSuggestions = async (partialAddress) => {
  if (!partialAddress || partialAddress.length < 3) {
    return [];
  }

  const response = await fetch(
    `${API_BASE_URL}/api/tasks/address-suggestions?partialAddress=${encodeURIComponent(partialAddress)}`,
    {
      method: 'GET',
      headers: getAuthHeaders(),
    }
  );

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to fetch address suggestions');
  }

  return response.json();
};
