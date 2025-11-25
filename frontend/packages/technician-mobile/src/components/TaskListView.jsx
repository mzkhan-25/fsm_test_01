import { useState, useEffect } from 'react';
import { getAssignedTasks } from '../services/taskService';
import './TaskListView.css';

const TaskListView = () => {
  const [tasks, setTasks] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchTasks = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getAssignedTasks();
      setTasks(data);
    } catch (err) {
      setError(err.message || 'Failed to load tasks');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, []);

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

  if (isLoading) {
    return (
      <div className="task-list-view">
        <div className="loading-message" role="status" aria-live="polite">
          Loading tasks...
        </div>
      </div>
    );
  }

  return (
    <div className="task-list-view">
      <div className="task-list-header">
        <h1 className="task-list-title">My Tasks</h1>
        <button
          className="task-refresh-btn"
          onClick={fetchTasks}
          disabled={isLoading}
          aria-label="Refresh tasks"
        >
          Refresh
        </button>
      </div>

      {error && (
        <div className="error-message" role="alert">
          {error}
        </div>
      )}

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
              <p className="task-address">{task.address}</p>
              <span className={`task-status ${getStatusClass(task.status)}`}>
                {formatStatus(task.status)}
              </span>
            </article>
          ))}
        </div>
      )}
    </div>
  );
};

export default TaskListView;
