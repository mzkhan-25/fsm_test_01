import { useState, useEffect, useCallback } from 'react';
import { isAuthenticated } from './services/authService';
import {
  initializePushNotifications,
  cleanupPushNotifications,
  addNotificationListener,
} from './services/notificationService';
import LoginPage from './components/LoginPage';
import NavigationTabs from './components/NavigationTabs';
import TaskListView from './components/TaskListView';
import TaskDetailView from './components/TaskDetailView';
import MapView from './components/MapView';
import ProfileView from './components/ProfileView';
import NotificationAlert from './components/NotificationAlert';
import './App.css';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(isAuthenticated());
  const [activeTab, setActiveTab] = useState('tasks');
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [selectedTaskId, setSelectedTaskId] = useState(null);

  // Initialize push notifications when user logs in
  useEffect(() => {
    if (isLoggedIn) {
      initializePushNotifications().catch(console.error);
    }
  }, [isLoggedIn]);

  // Handle notification tap events (deep linking)
  useEffect(() => {
    const handleNotificationEvent = (event) => {
      if (event.type === 'notification_tap' && event.taskId) {
        // Deep link to the task
        setActiveTab('tasks');
        setSelectedTaskId(event.taskId);
      }
    };

    const unsubscribe = addNotificationListener(handleNotificationEvent);
    return unsubscribe;
  }, []);

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

  const handleLogout = async () => {
    // Cleanup push notifications on logout
    await cleanupPushNotifications();
    setIsLoggedIn(false);
    setActiveTab('tasks');
    setSelectedTaskId(null);
  };

  const handleTaskSelect = (taskId) => {
    setSelectedTaskId(taskId);
  };

  // Handle notification tap to navigate to task (deep linking)
  const handleNotificationTap = useCallback((taskId) => {
    setActiveTab('tasks');
    setSelectedTaskId(taskId);
  }, []);

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
      <NotificationAlert onNotificationTap={handleNotificationTap} />
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
