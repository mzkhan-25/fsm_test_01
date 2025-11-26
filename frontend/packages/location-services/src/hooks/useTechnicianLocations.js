import { useState, useEffect, useCallback } from 'react';
import { fetchTechnicianLocations } from '../services/technicianService';

/**
 * Custom hook for fetching and managing technician locations
 * 
 * @param {Object} options - Hook options
 * @param {number} options.refreshInterval - Auto-refresh interval in milliseconds (0 to disable)
 * @returns {Object} Hook state and methods
 */
export function useTechnicianLocations({ refreshInterval = 0 } = {}) {
  const [technicians, setTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);

  const loadTechnicians = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const data = await fetchTechnicianLocations();
      
      setTechnicians(data);
      setLastUpdated(new Date());
    } catch (err) {
      setError(err.message || 'Failed to load technician locations');
      setTechnicians([]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Initial load
  useEffect(() => {
    loadTechnicians();
  }, [loadTechnicians]);

  // Auto-refresh
  useEffect(() => {
    if (refreshInterval > 0) {
      const intervalId = setInterval(loadTechnicians, refreshInterval);
      return () => clearInterval(intervalId);
    }
  }, [refreshInterval, loadTechnicians]);

  return {
    technicians,
    loading,
    error,
    lastUpdated,
    refresh: loadTechnicians,
  };
}
