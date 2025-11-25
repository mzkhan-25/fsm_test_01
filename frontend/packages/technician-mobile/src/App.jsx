import { useState, useEffect } from 'react';
import { isAuthenticated } from './services/authService';
import LoginPage from './components/LoginPage';
import NavigationTabs from './components/NavigationTabs';
import TaskListView from './components/TaskListView';
import TaskDetailView from './components/TaskDetailView';
import MapView from './components/MapView';
import ProfileView from './components/ProfileView';
import './App.css';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated());
  const [activeTab, setActiveTab] = useState('tasks');
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [selectedTaskId, setSelectedTaskId] = useState(null);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  const handleLoginSuccess = () => {
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setActiveTab('tasks');
    setSelectedTaskId(null);
  };

  const handleTaskSelect = (taskId) => {
    setSelectedTaskId(taskId);
  };

  const handleBackToList = () => {
    setSelectedTaskId(null);
  };

  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setSelectedTaskId(null);
  };

  if (!isLoggedIn) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} />;
  }

  const renderActiveView = () => {
    if (activeTab === 'tasks' && selectedTaskId) {
      return (
        <TaskDetailView
          taskId={selectedTaskId}
          onBack={handleBackToList}
        />
      );
    }

    switch (activeTab) {
      case 'tasks':
        return <TaskListView onTaskSelect={handleTaskSelect} />;
      case 'map':
        return <MapView />;
      case 'profile':
        return <ProfileView onLogout={handleLogout} />;
      default:
        return <TaskListView onTaskSelect={handleTaskSelect} />;
    }
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="app-header-content">
          <h1 className="app-title">FSM Technician</h1>
          <div className="online-status">
            <span className={`status-dot ${isOnline ? '' : 'offline'}`} aria-hidden="true"></span>
            <span>{isOnline ? 'Online' : 'Offline'}</span>
          </div>
        </div>
      </header>

      <main className="main-content">
        {renderActiveView()}
      </main>

      <NavigationTabs activeTab={activeTab} onTabChange={handleTabChange} />
    </div>
  );
}

export default App;
