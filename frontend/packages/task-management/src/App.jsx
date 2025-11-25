import './App.css'
import TaskCreationForm from './components/TaskCreationForm'

function App() {
  const handleSuccess = () => {
    // In a real app, this would navigate to task list
    console.log('Task created successfully, would redirect to task list');
  };

  const handleCancel = () => {
    // In a real app, this would navigate back
    console.log('Task creation cancelled');
  };

  return (
    <TaskCreationForm onSuccess={handleSuccess} onCancel={handleCancel} />
  )
}

export default App
