import { useState, useEffect, useCallback } from 'react';
import { fetchUnassignedTasks } from '../services/taskService';
import { processTasksWithCoordinates } from '../services/geocodeService';

/**
 * Custom hook for fetching and managing unassigned tasks with coordinates
 * 
 * @param {Object} options - Hook options
 * @param {number} options.refreshInterval - Auto-refresh interval in milliseconds (0 to disable)
 * @returns {Object} Hook state and methods
 */
export function useUnassignedTasks({ refreshInterval = 0 } = {}) {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);

  const loadTasks = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await fetchUnassignedTasks();
      const tasksWithCoords = processTasksWithCoordinates(response.tasks || []);
      
      setTasks(tasksWithCoords);
      setLastUpdated(new Date());
    } catch (err) {
      setError(err.message || 'Failed to load tasks');
      setTasks([]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Initial load
  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  // Auto-refresh
  useEffect(() => {
    if (refreshInterval > 0) {
      const intervalId = setInterval(loadTasks, refreshInterval);
      return () => clearInterval(intervalId);
    }
  }, [refreshInterval, loadTasks]);

  return {
    tasks,
    loading,
    error,
    lastUpdated,
    refresh: loadTasks,
  };
}
