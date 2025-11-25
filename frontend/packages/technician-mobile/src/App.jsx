import { useState, useEffect } from 'react';
import { isAuthenticated } from './services/authService';
import LoginPage from './components/LoginPage';
import NavigationTabs from './components/NavigationTabs';
import TaskListView from './components/TaskListView';
import MapView from './components/MapView';
import ProfileView from './components/ProfileView';
import './App.css';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated());
  const [activeTab, setActiveTab] = useState('tasks');
  const [isOnline, setIsOnline] = useState(navigator.onLine);

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
  };

  if (!isLoggedIn) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} />;
  }

  const renderActiveView = () => {
    switch (activeTab) {
      case 'tasks':
        return <TaskListView />;
      case 'map':
        return <MapView />;
      case 'profile':
        return <ProfileView onLogout={handleLogout} />;
      default:
        return <TaskListView />;
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

      <NavigationTabs activeTab={activeTab} onTabChange={setActiveTab} />
    </div>
  );
}

export default App;
