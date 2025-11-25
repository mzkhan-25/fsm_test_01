import { useState } from 'react'
import './App.css'
import TaskCreationForm from './components/TaskCreationForm'
import TaskListView from './components/TaskListView'

function App() {
  const [view, setView] = useState('list');

  const handleCreateTask = () => {
    setView('create');
  };

  const handleSuccess = () => {
    // Navigate back to task list after creating a task
    setView('list');
  };

  const handleCancel = () => {
    // Navigate back to task list on cancel
    setView('list');
  };

  if (view === 'create') {
    return (
      <TaskCreationForm onSuccess={handleSuccess} onCancel={handleCancel} />
    );
  }

  return (
    <TaskListView onCreateTask={handleCreateTask} />
  );
}

export default App
