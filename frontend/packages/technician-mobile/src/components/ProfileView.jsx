import { getCurrentUser, clearAuth } from '../services/authService';
import './ProfileView.css';

const ProfileView = ({ onLogout }) => {
  const user = getCurrentUser();

  const getInitials = (name) => {
    if (!name) return '?';
    return name
      .split(' ')
      .map((part) => part[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const handleLogout = () => {
    clearAuth();
    if (onLogout) {
      onLogout();
    }
  };

  return (
    <div className="profile-view">
      <header className="profile-header">
        <div className="profile-avatar" aria-hidden="true">
          {getInitials(user.name)}
        </div>
        <h1 className="profile-name">{user.name || 'Technician'}</h1>
        <p className="profile-role">{user.role || 'Field Technician'}</p>
      </header>

      <section className="profile-section" aria-labelledby="account-info-title">
        <h2 id="account-info-title" className="section-title">Account Information</h2>
        <div className="profile-info-item">
          <span className="info-label">Email</span>
          <span className="info-value">{user.email || 'Not available'}</span>
        </div>
        <div className="profile-info-item">
          <span className="info-label">User ID</span>
          <span className="info-value">{user.id || 'Not available'}</span>
        </div>
        <div className="profile-info-item">
          <span className="info-label">Role</span>
          <span className="info-value">{user.role || 'Not available'}</span>
        </div>
      </section>

      <section className="profile-section" aria-labelledby="app-settings-title">
        <h2 id="app-settings-title" className="section-title">App Settings</h2>
        <div className="profile-info-item">
          <span className="info-label">Location Tracking</span>
          <span className="info-value">Enabled</span>
        </div>
        <div className="profile-info-item">
          <span className="info-label">Notifications</span>
          <span className="info-value">Enabled</span>
        </div>
      </section>

      <button
        className="logout-button"
        onClick={handleLogout}
        aria-label="Logout from account"
      >
        Logout
      </button>

      <p className="version-info">
        FSM Technician Mobile v1.0.0
      </p>
    </div>
  );
};

export default ProfileView;
