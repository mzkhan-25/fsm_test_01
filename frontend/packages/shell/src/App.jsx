import { useState } from 'react'
import './App.css'

const IDENTITY_SERVICE_URL = import.meta.env.VITE_IDENTITY_SERVICE_URL || 'http://localhost:5174';
const TASK_MANAGEMENT_SERVICE_URL = import.meta.env.VITE_TASK_MANAGEMENT_SERVICE_URL || 'http://localhost:5175';
const TECHNICIAN_MOBILE_SERVICE_URL = import.meta.env.VITE_TECHNICIAN_MOBILE_SERVICE_URL || 'http://localhost:5176';

function App() {
  const [activeService, setActiveService] = useState('identity');

  return (
    <div className="shell-container">
      <header className="shell-header">
        <h1>Field Service Management System</h1>
        <nav className="shell-nav">
          <button
            className={`nav-button ${activeService === 'identity' ? 'active' : ''}`}
            onClick={() => setActiveService('identity')}
          >
            Identity
          </button>
          <button
            className={`nav-button ${activeService === 'task-management' ? 'active' : ''}`}
            onClick={() => setActiveService('task-management')}
          >
            Tasks
          </button>
          <button
            className={`nav-button ${activeService === 'technician-mobile' ? 'active' : ''}`}
            onClick={() => setActiveService('technician-mobile')}
          >
            Technician Mobile
          </button>
        </nav>
      </header>
      <main className="shell-content">
        {activeService === 'identity' && (
          <iframe
            src={IDENTITY_SERVICE_URL}
            title="Identity Service"
            className="micro-frontend-iframe"
          />
        )}
        {activeService === 'task-management' && (
          <iframe
            src={TASK_MANAGEMENT_SERVICE_URL}
            title="Task Management Service"
            className="micro-frontend-iframe"
          />
        )}
        {activeService === 'technician-mobile' && (
          <iframe
            src={TECHNICIAN_MOBILE_SERVICE_URL}
            title="Technician Mobile Service"
            className="micro-frontend-iframe"
          />
        )}
      </main>
    </div>
  )
}

export default App

