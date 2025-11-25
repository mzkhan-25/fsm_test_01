# Task Management Micro-frontend

This package contains the Task Management micro-frontend for the Field Service Management system.

## Features

- Task creation form with validation
- Address autocomplete integration
- Priority selection
- Estimated duration input

## Development

```bash
# Install dependencies (from frontend root)
npm install

# Run development server
npm run dev

# Run tests
npm run test

# Run tests with coverage
npm run test:coverage

# Build for production
npm run build
```

## Port

The development server runs on port 5175.

## API Integration

This micro-frontend integrates with the Task Service API:
- `POST /api/tasks` - Create a new task
- `GET /api/tasks/address-suggestions` - Get address autocomplete suggestions

## Environment Variables

- `VITE_API_BASE_URL` - Base URL for the Task Service API (default: `http://localhost:8081`)
