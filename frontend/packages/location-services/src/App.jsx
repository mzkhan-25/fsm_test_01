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
  // TODO: Integrate with task detail view or navigation when available
  const handleTaskClick = (task) => {
    // Task selection functionality will be implemented in future story
    // For now, the popup displays task details on click
    void task; // Acknowledge parameter to avoid unused warning
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
        <Map tasks={tasks} onTaskClick={handleTaskClick} />
      </main>
    </div>
  )
}

export default App
