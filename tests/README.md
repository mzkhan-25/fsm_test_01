# Smoke Test Suite

This directory contains smoke tests for all microservices (backend and frontend) in the FSM application.

## Overview

Smoke tests provide fast validation (< 2 minutes) to ensure:
- Services are operational
- Health endpoints are responding
- Critical paths work (basic CRUD operations)

## Structure

```
tests/
├── smoke-test/
│   ├── run-smoke-tests.sh              # Main orchestrator script
│   ├── identity-svc-smoke.test.java    # Backend: Identity service
│   ├── task-svc-smoke.test.java        # Backend: Task service
│   ├── location-svc-smoke.test.java    # Backend: Location service
│   ├── shell-smoke.test.js             # Frontend: Shell
│   ├── identity-frontend-smoke.test.js # Frontend: Identity
│   ├── task-management-frontend-smoke.test.js # Frontend: Task Management
│   ├── technician-mobile-frontend-smoke.test.js # Frontend: Technician Mobile
│   └── logs/                           # Test execution logs
├── helpers/
│   └── http-client.js                  # Shared HTTP utilities
└── SMOKE_REPORT.md                     # Generated test report
```

## Services Tested

### Backend Services (Spring Boot)
- **identity-svc** (Port 8080) - Identity and Access Management
- **task-svc** (Port 8081) - Task Management
- **location-svc** (Port 8082) - Location Tracking

### Frontend Services (React/Vite)
- **shell** (Port 5173) - Main Shell Application
- **identity** (Port 5174) - Identity UI
- **task-management** (Port 5175) - Task Management UI
- **technician-mobile** (Port 5176) - Technician Mobile UI

## Prerequisites

- Docker (for potential database services)
- Maven 3.6+ (for backend services)
- Node.js 18+ (for frontend services)
- Java 17+ (for backend services)

## Quick Start

### Run All Smoke Tests

```bash
cd tests/smoke-test
./run-smoke-tests.sh
```

This will:
1. Check prerequisites (Docker, Maven, Node.js)
2. Start all backend services individually
3. Wait for health checks
4. Run backend smoke tests
5. Start all frontend services individually
6. Run frontend smoke tests
7. Generate SMOKE_REPORT.md
8. Cleanup all services

### Run Individual Service Tests

#### Backend Services

```bash
# Identity Service
cd backend/identity-svc
mvn test -Dtest="IdentitySvcSmokeTest"

# Task Service
cd backend/task-svc
mvn test -Dtest="TaskSvcSmokeTest"

# Location Service
cd backend/location-svc
mvn test -Dtest="LocationSvcSmokeTest"
```

#### Frontend Services

```bash
# Shell
node tests/smoke-test/shell-smoke.test.js

# Identity
node tests/smoke-test/identity-frontend-smoke.test.js

# Task Management
node tests/smoke-test/task-management-frontend-smoke.test.js

# Technician Mobile
node tests/smoke-test/technician-mobile-frontend-smoke.test.js
```

## Test Coverage

### Backend Tests
Each backend service test includes:
- ✅ Health check endpoint (`/actuator/health`)
- ✅ API documentation endpoint (`/api-docs`)
- ✅ Basic CRUD operations on main resource
- ✅ Status code validation
- ✅ Response structure validation

### Frontend Tests
Each frontend service test includes:
- ✅ Service availability check
- ✅ HTML content rendering
- ✅ HTTP response validation

## Reports

After execution, view the report:

```bash
cat tests/SMOKE_REPORT.md
```

The report includes:
- Execution timestamp
- Total duration
- Per-service results (Health, CRUD, Status)
- Summary statistics
- Link to detailed logs

## Logs

Detailed logs are stored in `tests/smoke-test/logs/`:
- `{service-name}.log` - Service startup logs
- `{service-name}-test.log` - Test execution logs
- `{service-name}-error.log` - Error logs (if any)

## Troubleshooting

### Port Conflicts

If ports are already in use:
```bash
# Check what's using the ports
lsof -i :8080,8081,8082,5173,5174,5175,5176

# Kill processes if needed
kill -9 <PID>
```

### Service Startup Failures

1. Check logs in `tests/smoke-test/logs/{service-name}.log`
2. Verify prerequisites are installed
3. Ensure no other instances are running
4. Check database connectivity (for backend services)

### Test Failures

1. Review test logs: `tests/smoke-test/logs/{service-name}-test.log`
2. Verify service is running: `curl http://localhost:{port}/actuator/health`
3. Check for missing dependencies
4. Verify test data is not conflicting

## CI/CD Integration

To integrate into CI/CD pipeline:

```yaml
# Example GitHub Actions
- name: Run Smoke Tests
  run: |
    cd tests/smoke-test
    ./run-smoke-tests.sh
  timeout-minutes: 5
```

## Development

### Adding New Tests

1. Create test file in `tests/smoke-test/`
2. Follow naming convention: `{service-name}-smoke.test.{ext}`
3. Include health check and basic CRUD tests
4. Update `run-smoke-tests.sh` to include new service
5. Update this README

### Modifying Tests

- Keep tests simple and fast
- Focus on critical paths only
- Avoid complex test scenarios
- Use appropriate test framework (JUnit for Java, Node.js for frontend)

## Exit Codes

- `0` - All tests passed
- `1` - One or more tests failed

## Performance Expectations

- **Total Duration**: < 2 minutes
- **Per Service Startup**: < 30 seconds
- **Per Test Suite**: < 10 seconds

## Best Practices

1. Run smoke tests before committing code
2. Use in CI/CD for fast feedback
3. Don't replace comprehensive integration tests
4. Keep tests independent and idempotent
5. Clean up test data after execution

## Support

For issues or questions:
1. Check logs in `tests/smoke-test/logs/`
2. Review service documentation
3. Verify prerequisites are met
4. Consult team documentation
