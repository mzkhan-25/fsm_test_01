import { useState, useEffect, useRef, useCallback } from 'react';
import { getAssignedTasks } from '../services/taskService';
import './TaskListView.css';

const STATUS_FILTERS = [
  { key: 'all', label: 'All' },
  { key: 'assigned', label: 'Assigned' },
  { key: 'in_progress', label: 'In Progress' },
  { key: 'completed', label: 'Completed' },
];

const TaskListView = () => {
  const [tasks, setTasks] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeFilter, setActiveFilter] = useState('all');
  const [isRefreshing, setIsRefreshing] = useState(false);
  const containerRef = useRef(null);
  const touchStartY = useRef(0);
  const touchEndY = useRef(0);

  const fetchTasks = useCallback(async (status = activeFilter) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getAssignedTasks(status);
      setTasks(data);
    } catch (err) {
      setError(err.message || 'Failed to load tasks');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [activeFilter]);

  useEffect(() => {
    fetchTasks(activeFilter);
  }, [activeFilter, fetchTasks]);

  const handleFilterChange = (filterKey) => {
    setActiveFilter(filterKey);
  };

  const handleRefresh = () => {
    fetchTasks(activeFilter);
  };

  const handleTouchStart = (e) => {
    touchStartY.current = e.touches[0].clientY;
  };

  const handleTouchMove = (e) => {
    touchEndY.current = e.touches[0].clientY;
  };

  const handleTouchEnd = () => {
    const container = containerRef.current;
    const pullDistance = touchEndY.current - touchStartY.current;
    const atTop = container && container.scrollTop === 0;

    if (atTop && pullDistance > 50 && !isLoading && !isRefreshing) {
      setIsRefreshing(true);
      fetchTasks(activeFilter);
    }

    touchStartY.current = 0;
    touchEndY.current = 0;
  };

  const getPriorityClass = (priority) => {
    const priorityMap = {
      HIGH: 'high',
      MEDIUM: 'medium',
      LOW: 'low',
    };
    return priorityMap[priority] || '';
  };

  const getStatusClass = (status) => {
    const statusMap = {
      IN_PROGRESS: 'in-progress',
      COMPLETED: 'completed',
    };
    return statusMap[status] || '';
  };

  const formatStatus = (status) => {
    return status.replace(/_/g, ' ');
  };

  const formatDuration = (minutes) => {
    if (!minutes) return '';
    if (minutes < 60) return `${minutes} min`;
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    if (remainingMinutes === 0) return `${hours}h`;
    return `${hours}h ${remainingMinutes}min`;
  };

  if (isLoading && !isRefreshing) {
    return (
      <div className="task-list-view">
        <div className="loading-message" role="status" aria-live="polite">
          Loading tasks...
        </div>
      </div>
    );
  }

  return (
    <div
      className="task-list-view"
      ref={containerRef}
      onTouchStart={handleTouchStart}
      onTouchMove={handleTouchMove}
      onTouchEnd={handleTouchEnd}
    >
      {isRefreshing && (
        <div className="pull-to-refresh-indicator" role="status" aria-live="polite">
          Refreshing...
        </div>
      )}

      <div className="task-list-header">
        <h1 className="task-list-title">My Tasks</h1>
        <button
          className="task-refresh-btn"
          onClick={handleRefresh}
          disabled={isLoading}
          aria-label="Refresh tasks"
        >
          Refresh
        </button>
      </div>

      <div className="status-filter-tabs" role="tablist" aria-label="Filter tasks by status">
        {STATUS_FILTERS.map((filter) => (
          <button
            key={filter.key}
            role="tab"
            className={`filter-tab ${activeFilter === filter.key ? 'active' : ''}`}
            onClick={() => handleFilterChange(filter.key)}
            aria-selected={activeFilter === filter.key}
            aria-controls="task-list-panel"
          >
            {filter.label}
          </button>
        ))}
      </div>

      {error && (
        <div className="error-message" role="alert">
          {error}
        </div>
      )}

      <div id="task-list-panel" role="tabpanel">
        {tasks.length === 0 && !error ? (
          <div className="no-tasks-message">
            <p>No tasks assigned to you.</p>
          </div>
        ) : (
          <div className="task-list" role="list">
            {tasks.map((task) => (
              <article
                key={task.id}
                className="task-card"
                role="listitem"
                tabIndex={0}
                aria-label={`Task: ${task.title}`}
              >
                <div className="task-card-header">
                  <h2 className="task-title">{task.title}</h2>
                  <span className={`task-priority ${getPriorityClass(task.priority)}`}>
                    {task.priority}
                  </span>
                </div>
                <p className="task-address">{task.clientAddress || task.address}</p>
                <div className="task-card-footer">
                  <span className={`task-status ${getStatusClass(task.status)}`}>
                    {formatStatus(task.status)}
                  </span>
                  {task.estimatedDuration && (
                    <span className="task-duration">
                      {formatDuration(task.estimatedDuration)}
                    </span>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default TaskListView;
