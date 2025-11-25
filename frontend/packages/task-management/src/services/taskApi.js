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
