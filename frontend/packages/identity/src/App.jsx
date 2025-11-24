import { useState } from 'react'
import LoginPage from './components/LoginPage'
import UserManagement from './components/UserManagement'

function App() {
  // Initialize state directly from localStorage
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return !!localStorage.getItem('token');
  });
  const [userRole, setUserRole] = useState(() => {
    return localStorage.getItem('userRole');
  });

  const handleLogout = () => {
    localStorage.clear();
    setIsAuthenticated(false);
    setUserRole(null);
  };

  if (!isAuthenticated) {
    return <LoginPage />;
  }

  if (userRole === 'ADMIN') {
    return (
      <div>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          padding: '10px 20px', 
          background: '#f8f9fa',
          borderBottom: '1px solid #dee2e6'
        }}>
          <span>Welcome, {localStorage.getItem('userName')}</span>
          <button 
            onClick={handleLogout}
            style={{
              padding: '6px 12px',
              background: '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Logout
          </button>
        </div>
        <UserManagement />
      </div>
    );
  }

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>Welcome, {localStorage.getItem('userName')}</h1>
      <p>Your role: {userRole}</p>
      <button 
        onClick={handleLogout}
        style={{
          padding: '10px 20px',
          background: '#dc3545',
          color: 'white',
          border: 'none',
          borderRadius: '6px',
          cursor: 'pointer',
          marginTop: '20px'
        }}
      >
        Logout
      </button>
    </div>
  );
}

export default App

