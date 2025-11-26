import './App.css'
import Map from './components/Map'
import { useUnassignedTasks } from './hooks/useUnassignedTasks'

// Refresh interval: 30 seconds
const REFRESH_INTERVAL = 30000;

function App() {
  const { tasks, loading, error, refresh } = useUnassignedTasks({ 
    refreshInterval: REFRESH_INTERVAL 
  });

  // Handler for task marker clicks
  const handleTaskClick = (task) => {
    // Task selection functionality - popup displays automatically
    void task; // Acknowledge parameter to avoid unused warning
  };

  // Handler for assigning tasks
  const handleAssignTask = (task) => {
    // Task assignment functionality will be implemented in future story
    // For now, just log the action
    console.log('Assign task:', task.id);
    // TODO: Integrate with task assignment workflow
  };

  // Handler for viewing task details
  const handleViewDetails = (task) => {
    // Navigate to task detail view
    // For now, just log the action
    console.log('View task details:', task.id);
    // TODO: Integrate with task detail view or navigation when available
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Location Services</h1>
        <div className="app-status">
          {loading && <span className="status-loading">Loading tasks...</span>}
          {error && <span className="status-error">{error}</span>}
          {!loading && !error && (
            <span className="status-info">
              {tasks.length} unassigned task{tasks.length !== 1 ? 's' : ''} on map
            </span>
          )}
          <button 
            className="refresh-button" 
            onClick={refresh} 
            disabled={loading}
            title="Refresh tasks"
          >
            ↻
          </button>
        </div>
      </header>
      <main className="map-container">
        <Map 
          tasks={tasks} 
          onTaskClick={handleTaskClick}
          onAssignTask={handleAssignTask}
          onViewDetails={handleViewDetails}
        />
      </main>
    </div>
  )
}

export default App
