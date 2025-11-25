import { useState } from 'react'
import './App.css'
import TaskCreationForm from './components/TaskCreationForm'
import TechnicianAssignmentModal from './components/TechnicianAssignmentModal'

function App() {
  const [showAssignmentModal, setShowAssignmentModal] = useState(false);
  const [selectedTask, setSelectedTask] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);

  // Demo tasks for showcase
  const demoTasks = [
    { id: 1, title: 'HVAC Repair - Commercial Building', status: 'UNASSIGNED', priority: 'HIGH' },
    { id: 2, title: 'Electrical Inspection', status: 'UNASSIGNED', priority: 'MEDIUM' },
    { id: 3, title: 'Plumbing Service Call', status: 'UNASSIGNED', priority: 'URGENT' },
  ];

  const handleAssignClick = (task) => {
    setSelectedTask(task);
    setShowAssignmentModal(true);
  };

  const handleAssignmentComplete = (result) => {
    console.log('Task assigned successfully:', result);
    // In a real app, this would refresh the task list
  };

  const handleCreateSuccess = () => {
    setShowCreateForm(false);
    console.log('Task created successfully, would refresh task list');
  };

  const handleCreateCancel = () => {
    setShowCreateForm(false);
    console.log('Task creation cancelled');
  };

  if (showCreateForm) {
    return <TaskCreationForm onSuccess={handleCreateSuccess} onCancel={handleCreateCancel} />;
  }

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Task Management</h1>
        <button className="create-task-button" onClick={() => setShowCreateForm(true)}>
          + Create Task
        </button>
      </header>

      <main className="task-list-container">
        <h2>Unassigned Tasks</h2>
        <div className="task-list">
          {demoTasks.map((task) => (
            <div key={task.id} className={`task-item priority-${task.priority.toLowerCase()}`}>
              <div className="task-info">
                <span className="task-id">#{task.id}</span>
                <h3 className="task-title">{task.title}</h3>
                <span className={`task-priority ${task.priority.toLowerCase()}`}>
                  {task.priority}
                </span>
              </div>
              <button
                className="assign-button"
                onClick={() => handleAssignClick(task)}
              >
                Assign Task
              </button>
            </div>
          ))}
        </div>
      </main>

      <TechnicianAssignmentModal
        task={selectedTask}
        isOpen={showAssignmentModal}
        onClose={() => setShowAssignmentModal(false)}
        onAssignmentComplete={handleAssignmentComplete}
      />
    </div>
  )
}

export default App
