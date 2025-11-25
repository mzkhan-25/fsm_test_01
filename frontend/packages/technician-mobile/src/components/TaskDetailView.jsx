import { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import { getTaskById, updateTaskStatus } from '../services/taskService';
import './TaskDetailView.css';

const TaskDetailView = ({ taskId, onBack, onStatusUpdate }) => {
  const [task, setTask] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isUpdating, setIsUpdating] = useState(false);
  const [successMessage, setSuccessMessage] = useState(null);

  const fetchTask = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getTaskById(taskId);
      setTask(data);
    } catch (err) {
      setError(err.message || 'Failed to load task details');
    } finally {
      setIsLoading(false);
    }
  }, [taskId]);

  useEffect(() => {
    fetchTask();
  }, [fetchTask]);

  // Auto-dismiss success message after 5 seconds
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => {
        setSuccessMessage(null);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  const clearMessages = () => {
    setError(null);
    setSuccessMessage(null);
  };

  const handleStartNavigation = () => {
    const address = task.clientAddress || task.address;
    if (address) {
      const encodedAddress = encodeURIComponent(address);
      window.open(`https://www.google.com/maps/dir/?api=1&destination=${encodedAddress}`, '_blank');
    }
  };

  const handleMarkInProgress = async () => {
    setIsUpdating(true);
    clearMessages();
    try {
      await updateTaskStatus(taskId, 'IN_PROGRESS');
      setTask({ ...task, status: 'IN_PROGRESS' });
      setSuccessMessage('Task marked as in progress successfully!');
      if (onStatusUpdate) {
        onStatusUpdate(taskId, 'IN_PROGRESS');
      }
    } catch (err) {
      setError(err.message || 'Failed to update task status');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleMarkCompleted = async () => {
    setIsUpdating(true);
    clearMessages();
    try {
      await updateTaskStatus(taskId, 'COMPLETED');
      setTask({ ...task, status: 'COMPLETED' });
      setSuccessMessage('Task marked as completed successfully!');
      if (onStatusUpdate) {
        onStatusUpdate(taskId, 'COMPLETED');
      }
    } catch (err) {
      setError(err.message || 'Failed to update task status');
    } finally {
      setIsUpdating(false);
    }
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
      ASSIGNED: 'assigned',
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

  const shouldShowStartNavigation = () => {
    return task && (task.status === 'ASSIGNED' || task.status === 'IN_PROGRESS');
  };

  const shouldShowMarkInProgress = () => {
    return task && task.status === 'ASSIGNED';
  };

  const shouldShowMarkCompleted = () => {
    return task && task.status === 'IN_PROGRESS';
  };

  if (isLoading) {
    return (
      <div className="task-detail-view">
        <div className="loading-message" role="status" aria-live="polite">
          Loading task details...
        </div>
      </div>
    );
  }

  if (error && !task) {
    return (
      <div className="task-detail-view">
        <div className="task-detail-header">
          <button
            className="back-button"
            onClick={onBack}
            aria-label="Go back to task list"
          >
            ← Back
          </button>
        </div>
        <div className="error-message" role="alert">
          {error}
        </div>
      </div>
    );
  }

  if (!task) {
    return (
      <div className="task-detail-view">
        <div className="task-detail-header">
          <button
            className="back-button"
            onClick={onBack}
            aria-label="Go back to task list"
          >
            ← Back
          </button>
        </div>
        <div className="no-task-message">
          <p>Task not found.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="task-detail-view">
      <div className="task-detail-header">
        <button
          className="back-button"
          onClick={onBack}
          aria-label="Go back to task list"
        >
          ← Back
        </button>
      </div>

      {error && (
        <div className="error-message" role="alert">
          {error}
        </div>
      )}

      {successMessage && (
        <div className="success-message" role="status" aria-live="polite">
          {successMessage}
        </div>
      )}

      <article className="task-detail-card">
        <div className="task-detail-title-section">
          <h1 className="task-detail-title">{task.title}</h1>
          <div className="task-detail-badges">
            <span className={`task-priority-badge ${getPriorityClass(task.priority)}`}>
              {task.priority}
            </span>
            <span className={`task-status-badge ${getStatusClass(task.status)}`}>
              {formatStatus(task.status)}
            </span>
          </div>
        </div>

        <div className="task-detail-section">
          <h2 className="section-label">Description</h2>
          <p className="task-description">{task.description || 'No description provided.'}</p>
        </div>

        <div className="task-detail-section">
          <h2 className="section-label">Client Address</h2>
          <p className="task-address">{task.clientAddress || task.address || 'No address provided.'}</p>
        </div>

        <div className="task-detail-row">
          <div className="task-detail-item">
            <h2 className="section-label">Estimated Duration</h2>
            <p className="task-duration">
              {task.estimatedDuration ? formatDuration(task.estimatedDuration) : 'Not specified'}
            </p>
          </div>
        </div>

        {task.specialNotes && (
          <div className="task-detail-section">
            <h2 className="section-label">Special Notes</h2>
            <p className="task-special-notes">{task.specialNotes}</p>
          </div>
        )}
      </article>

      <div className="task-action-buttons">
        {shouldShowStartNavigation() && (
          <button
            className="action-button navigation-btn"
            onClick={handleStartNavigation}
            aria-label="Start navigation to client address"
          >
            📍 Start Navigation
          </button>
        )}

        {shouldShowMarkInProgress() && (
          <button
            className="action-button in-progress-btn"
            onClick={handleMarkInProgress}
            disabled={isUpdating}
            aria-label="Mark task as in progress"
          >
            {isUpdating ? 'Updating...' : '🔄 Mark In Progress'}
          </button>
        )}

        {shouldShowMarkCompleted() && (
          <button
            className="action-button completed-btn"
            onClick={handleMarkCompleted}
            disabled={isUpdating}
            aria-label="Mark task as completed"
          >
            {isUpdating ? 'Updating...' : '✓ Mark Completed'}
          </button>
        )}
      </div>
    </div>
  );
};

TaskDetailView.propTypes = {
  taskId: PropTypes.string.isRequired,
  onBack: PropTypes.func.isRequired,
  onStatusUpdate: PropTypes.func,
};

export default TaskDetailView;
