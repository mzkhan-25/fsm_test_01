/**
 * Task service for fetching task data from the API
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * Priority enum matching backend
 */
export const TaskPriority = {
  HIGH: 'HIGH',
  MEDIUM: 'MEDIUM',
  LOW: 'LOW',
};

/**
 * Task status enum matching backend
 */
export const TaskStatus = {
  UNASSIGNED: 'UNASSIGNED',
  ASSIGNED: 'ASSIGNED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
};

/**
 * Fetches unassigned tasks from the API
 * @param {Object} options - Fetch options
 * @param {number} options.page - Page number (0-based)
 * @param {number} options.pageSize - Number of items per page
 * @returns {Promise<Object>} Task list response with tasks and pagination info
 */
export async function fetchUnassignedTasks({ page = 0, pageSize = 100 } = {}) {
  const params = new URLSearchParams({
    status: TaskStatus.UNASSIGNED,
    page: page.toString(),
    pageSize: pageSize.toString(),
  });
  
  const url = `${API_BASE_URL}/tasks?${params.toString()}`;

  const response = await fetch(url);
  
  if (!response.ok) {
    throw new Error(`Failed to fetch tasks: ${response.status} ${response.statusText}`);
  }
  
  return response.json();
}

/**
 * Gets priority color for a given task priority
 * @param {string} priority - Task priority (HIGH, MEDIUM, LOW)
 * @returns {string} CSS color value
 */
export function getPriorityColor(priority) {
  switch (priority) {
    case TaskPriority.HIGH:
      return '#dc3545'; // Red
    case TaskPriority.MEDIUM:
      return '#fd7e14'; // Orange
    case TaskPriority.LOW:
      return '#ffc107'; // Yellow
    default:
      return '#6c757d'; // Gray for unknown
  }
}

/**
 * Gets text color for a given priority (for contrast on priority badge)
 * @param {string} priority - Task priority (HIGH, MEDIUM, LOW)
 * @returns {string} CSS color value for text
 */
export function getPriorityTextColor(priority) {
  // LOW priority has yellow background, needs dark text for contrast
  return priority === TaskPriority.LOW ? '#000000' : '#ffffff';
}

/**
 * Gets priority label for display
 * @param {string} priority - Task priority
 * @returns {string} Human-readable priority label
 */
export function getPriorityLabel(priority) {
  switch (priority) {
    case TaskPriority.HIGH:
      return 'High';
    case TaskPriority.MEDIUM:
      return 'Medium';
    case TaskPriority.LOW:
      return 'Low';
    default:
      return 'Unknown';
  }
}
