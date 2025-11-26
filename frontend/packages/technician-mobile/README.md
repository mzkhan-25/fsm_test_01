# FSM Technician Mobile

A mobile-optimized web application for field technicians to view and manage their assigned tasks.

## Features

- **Task List View**: View all assigned tasks with priority and status indicators
- **Map View**: Location tracking and navigation placeholder (map integration coming soon)
- **Profile View**: View account information and logout
- **Mobile-First Design**: Optimized for mobile devices with touch-friendly navigation
- **Offline Detection**: Displays online/offline status to users
- **Authentication**: Secure login for field technicians
- **Push Notifications**: Receive notifications for new task assignments
  - Request notification permissions from users
  - Register device tokens with backend
  - Display foreground notifications as in-app alerts
  - Handle background notifications as system notifications
  - Deep linking to task detail from notifications
  - App badge updates with unread notification count

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
│   │   ├── TaskDetailView.jsx # Task detail page
│   │   ├── MapView.jsx      # Map placeholder
│   │   ├── ProfileView.jsx  # User profile page
│   │   ├── NotificationAlert.jsx # In-app notification alert
│   │   └── NotificationBadge.jsx # Unread count badge
│   ├── services/            # API services
│   │   ├── authService.js   # Authentication API
│   │   ├── taskService.js   # Task management API
│   │   ├── locationService.js # Location tracking API
│   │   └── notificationService.js # Push notification handling
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

### Notifications

- `POST /api/notifications/devices` - Register device token
- `DELETE /api/notifications/devices/:token` - Unregister device token
- `GET /api/notifications` - Get notifications for user
- `PUT /api/notifications/:id/read` - Mark notification as read
- `PUT /api/notifications/read-all` - Mark all notifications as read

## Navigation

The app uses a bottom tab navigation with three main views:

1. **Tasks** (📋) - Default view showing assigned tasks
2. **Map** (🗺️) - Map view for navigation (placeholder)
3. **Profile** (👤) - User profile and settings

## Push Notifications

The app supports web push notifications for task assignments:

### Flow

1. **On Login**: App requests notification permission and generates device token
2. **Device Registration**: Token is sent to backend for push notification delivery
3. **Foreground Notifications**: Displayed as an in-app alert banner
4. **Background Notifications**: Displayed as system notifications
5. **Deep Linking**: Tapping a notification navigates to the task detail
6. **Badge**: Unread count displayed in document title and app badge (where supported)

### Features

- Permission request with graceful degradation
- Device token management (generate, store, clear)
- In-app notification alert with auto-dismiss
- System notification with click handler
- Unread count tracking with badge updates
- Cleanup on logout

## Related Issues

- Story: STORY-007: View Assigned Tasks Mobile
- Story: STORY-012: Technician Notification on Task Assignment
- Task: TASK-023: Set Up Mobile App Project
- Task: TASK-046: Implement Push Notification Handling in Mobile
