# Field Service Management - Frontend

Modern React-based frontend using a micro-frontend architecture with Lerna monorepo management.

## Architecture

The frontend is structured as a monorepo with multiple micro-frontends:

- **Shell** (`packages/shell/`) - Main container application that hosts other micro-frontends in iframes
- **Identity** (`packages/identity/`) - Authentication and user management micro-frontend

### Technology Stack

- **React 19** - UI framework
- **Vite 7** - Build tool and dev server
- **Vitest** - Unit testing framework
- **React Testing Library** - Component testing utilities
- **Lerna 9** - Monorepo management
- **npm workspaces** - Package management

## Getting Started

### Prerequisites

- Node.js 18 or higher
- npm 9 or higher
- Backend identity service running on port 8080 (see `../backend/identity-svc/`)

### Installation

```bash
# Install dependencies for all packages
npm install
```

### Development

Start all micro-frontends in development mode:

```bash
npm run dev
```

This will start:
- Shell app at http://localhost:5173
- Identity app at http://localhost:5174 (embedded in shell)

Access the application at http://localhost:5173

### Testing

```bash
# Run all tests
npm test

# Run tests with coverage report
npm run test:coverage

# Run tests in watch mode
lerna run test --parallel
```

### Building

```bash
# Build all packages for production
npm run build

# Build specific package
cd packages/identity && npm run build
```

### Linting

```bash
# Lint all packages
npm run lint
```

## Project Structure

```
frontend/
├── packages/
│   ├── shell/              # Main container app
│   │   ├── src/
│   │   │   ├── App.jsx     # Main app component with iframe container
│   │   │   └── ...
│   │   ├── .env.example    # Environment variables template
│   │   └── package.json
│   │
│   └── identity/           # Login micro-frontend
│       ├── src/
│       │   ├── components/
│       │   │   ├── LoginPage.jsx      # Login form component
│       │   │   ├── LoginPage.css      # Styles
│       │   │   └── LoginPage.test.jsx # Unit tests
│       │   └── test/
│       │       └── setup.js           # Test configuration
│       ├── .env.example               # Environment variables template
│       └── package.json
│
├── lerna.json              # Lerna configuration
└── package.json           # Root package with workspace config
```

## Environment Configuration

Each micro-frontend can be configured using environment variables:

### Shell App

Create `packages/shell/.env.development`:

```env
VITE_IDENTITY_SERVICE_URL=http://localhost:5174
```

### Identity App

Create `packages/identity/.env.development`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

See `.env.example` files in each package for all available options.

## Available Scripts

### Root Level

- `npm run dev` - Start all micro-frontends in parallel
- `npm test` - Run tests for all packages
- `npm run test:coverage` - Run tests with coverage for all packages
- `npm run build` - Build all packages
- `npm run lint` - Lint all packages

### Package Level

Navigate to a package directory (e.g., `cd packages/identity`) and run:

- `npm run dev` - Start the package dev server
- `npm test` - Run tests
- `npm run test:coverage` - Run tests with coverage
- `npm run build` - Build the package
- `npm run lint` - Lint the package

## Micro-Frontend Guidelines

### Creating a New Micro-Frontend

1. Use Vite to scaffold a new React app:
   ```bash
   cd packages
   npm create vite@latest my-app -- --template react
   ```

2. Add testing dependencies:
   ```bash
   npm install -D -w my-app vitest @vitest/ui jsdom @testing-library/react @testing-library/jest-dom @testing-library/user-event
   ```

3. Configure Vitest in `vite.config.js`

4. Update the shell app to include the new micro-frontend iframe

### Design Principles

1. **Isolation**: Each micro-frontend runs independently in its own iframe
2. **No Shared State**: Micro-frontends do not directly import code from each other
3. **Independent Deploy**: Each micro-frontend can be built and deployed separately
4. **Consistent Testing**: All micro-frontends maintain 85%+ test coverage
5. **Environment Config**: Use environment variables for configuration

## Testing Guidelines

### Writing Tests

- Use React Testing Library for component tests
- Follow the testing pattern in `packages/identity/src/components/LoginPage.test.jsx`
- Test user interactions, not implementation details
- Include accessibility tests (ARIA attributes)
- Mock external dependencies (API calls, etc.)

### Coverage Requirements

All packages must maintain:
- Statement coverage: ≥ 85%
- Branch coverage: ≥ 85%
- Function coverage: ≥ 85%
- Line coverage: ≥ 85%

## Contributing

### Code Style

- Use ESLint for linting
- Follow existing code patterns
- Write meaningful commit messages
- Keep components small and focused

### Pull Request Process

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Ensure all tests pass
5. Run linting
6. Submit PR with description

## Current Features

### Identity Micro-Frontend

- ✅ Login page with email/password
- ✅ Form validation (required fields, email format)
- ✅ API integration with backend
- ✅ JWT token storage
- ✅ Role-based redirect
- ✅ Error handling and display
- ✅ 100% test coverage

## Roadmap

Future micro-frontends to be added:
- Task management
- Dispatcher dashboard
- Supervisor dashboard
- Technician mobile view
- Real-time location tracking
- Notifications

## Troubleshooting

### Port Already in Use

If ports 5173 or 5174 are already in use:

1. Stop the processes using those ports
2. Or, modify the port in `vite.config.js`:
   ```js
   server: {
     port: 5175, // Use a different port
   }
   ```

### Tests Failing

1. Clear node_modules and reinstall:
   ```bash
   rm -rf node_modules package-lock.json
   rm -rf packages/*/node_modules
   npm install
   ```

2. Clear test cache:
   ```bash
   lerna run test -- --clearCache
   ```

### Build Errors

1. Ensure all dependencies are installed:
   ```bash
   npm install
   ```

2. Try building packages individually:
   ```bash
   cd packages/identity
   npm run build
   ```

## License

[To be determined]

## Support

For issues and questions, please create an issue in the repository.
