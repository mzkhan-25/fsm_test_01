import './NavigationTabs.css';

const NavigationTabs = ({ activeTab, onTabChange }) => {
  const tabs = [
    { id: 'tasks', label: 'Tasks', icon: '📋' },
    { id: 'map', label: 'Map', icon: '🗺️' },
    { id: 'profile', label: 'Profile', icon: '👤' },
  ];

  return (
    <nav className="navigation-tabs" role="navigation" aria-label="Main navigation">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          className={`nav-tab ${activeTab === tab.id ? 'active' : ''}`}
          onClick={() => onTabChange(tab.id)}
          aria-current={activeTab === tab.id ? 'page' : undefined}
          aria-label={tab.label}
        >
          <span className="nav-tab-icon" aria-hidden="true">{tab.icon}</span>
          <span className="nav-tab-label">{tab.label}</span>
        </button>
      ))}
    </nav>
  );
};

export default NavigationTabs;
