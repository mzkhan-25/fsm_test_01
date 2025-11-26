import './App.css'
import Map from './components/Map'
import { useUnassignedTasks } from './hooks/useUnassignedTasks'
import { useTechnicianLocations } from './hooks/useTechnicianLocations'

// Refresh interval: 30 seconds
const REFRESH_INTERVAL = 30000;

function App() {
  const { tasks, loading: tasksLoading, error: tasksError, refresh: refreshTasks } = useUnassignedTasks({ 
    refreshInterval: REFRESH_INTERVAL 
  });

  const { technicians, loading: techniciansLoading, error: techniciansError, refresh: refreshTechnicians } = useTechnicianLocations({
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

  // Handler for technician marker clicks
  const handleTechnicianClick = (technician) => {
    // Technician selection functionality - popup displays automatically
    console.log('Technician clicked:', technician.technicianId);
  };

  // Handler for manual refresh
  const handleRefresh = () => {
    refreshTasks();
    refreshTechnicians();
  };

  const loading = tasksLoading || techniciansLoading;
  const error = tasksError || techniciansError;

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Location Services</h1>
        <div className="app-status">
          {loading && <span className="status-loading">Loading...</span>}
          {error && <span className="status-error">{error}</span>}
          {!loading && !error && (
            <span className="status-info">
              {tasks.length} unassigned task{tasks.length !== 1 ? 's' : ''} • {' '}
              {technicians.length} technician{technicians.length !== 1 ? 's' : ''}
            </span>
          )}
          <button 
            className="refresh-button" 
            onClick={handleRefresh} 
            disabled={loading}
            title="Refresh data"
          >
            ↻
          </button>
        </div>
      </header>
      <main className="map-container">
        <Map 
          tasks={tasks} 
          technicians={technicians}
          onTaskClick={handleTaskClick}
          onAssignTask={handleAssignTask}
          onViewDetails={handleViewDetails}
          onTechnicianClick={handleTechnicianClick}
        />
      </main>
    </div>
  )
}

export default App
