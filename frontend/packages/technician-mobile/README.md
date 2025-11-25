# FSM Technician Mobile

A mobile-optimized web application for field technicians to view and manage their assigned tasks.

## Features

- **Task List View**: View all assigned tasks with priority and status indicators
- **Map View**: Location tracking and navigation placeholder (map integration coming soon)
- **Profile View**: View account information and logout
- **Mobile-First Design**: Optimized for mobile devices with touch-friendly navigation
- **Offline Detection**: Displays online/offline status to users
- **Authentication**: Secure login for field technicians

## Tech Stack

- **React 19** - UI framework
- **Vite** - Build tool and dev server
- **Vitest** - Testing framework
- **ESLint** - Code linting

## Getting Started

### Prerequisites

- Node.js 18+
- npm 9+

### Installation

From the root `frontend` directory:

```bash
npm install
```

### Development

Run the development server (runs on port 5176):

```bash
# From the frontend root directory
npm run dev

# Or from this package directory
npm run dev
```

### Building

```bash
npm run build
```

### Testing

```bash
# Run tests
npm run test

# Run tests with UI
npm run test:ui

# Run tests with coverage
npm run test:coverage
```

### Linting

```bash
npm run lint
```

## Project Structure

```
technician-mobile/
├── src/
│   ├── components/          # React components
│   │   ├── LoginPage.jsx    # Login form component
│   │   ├── NavigationTabs.jsx # Bottom navigation tabs
│   │   ├── TaskListView.jsx # Task list display
│   │   ├── MapView.jsx      # Map placeholder
│   │   └── ProfileView.jsx  # User profile page
│   ├── services/            # API services
│   │   ├── authService.js   # Authentication API
│   │   └── taskService.js   # Task management API
│   ├── test/                # Test utilities
│   │   └── setup.js         # Vitest setup
│   ├── App.jsx              # Main app component
│   ├── App.css              # App styles
│   ├── main.jsx             # Entry point
│   └── index.css            # Global styles
├── package.json
├── vite.config.js
└── eslint.config.js
```

## API Integration

The app communicates with the backend API at the URL specified by `VITE_API_BASE_URL` environment variable (defaults to `http://localhost:8080`).

### Authentication

- `POST /api/auth/login` - Login with email and password

### Tasks

- `GET /api/tasks/assigned` - Get tasks assigned to the current technician
- `GET /api/tasks/:id` - Get task details
- `PUT /api/tasks/:id/status` - Update task status
- `POST /api/tasks/:id/complete` - Complete a task

### Location

- `PUT /api/technicians/location` - Update technician location

## Navigation

The app uses a bottom tab navigation with three main views:

1. **Tasks** (📋) - Default view showing assigned tasks
2. **Map** (🗺️) - Map view for navigation (placeholder)
3. **Profile** (👤) - User profile and settings

## Related Issues

- Story: STORY-007: View Assigned Tasks Mobile
- Task: TASK-023: Set Up Mobile App Project
