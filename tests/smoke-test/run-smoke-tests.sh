#!/bin/bash

# Smoke Test Orchestrator
# Starts services individually, runs smoke tests, and generates report

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
LOG_DIR="$ROOT_DIR/tests/smoke-test/logs"
REPORT_FILE="$ROOT_DIR/tests/SMOKE_REPORT.md"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Initialize
mkdir -p "$LOG_DIR"
START_TIME=$(date +%s)
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

# Service tracking
declare -A SERVICE_PIDS
declare -A SERVICE_STATUS
declare -A SERVICE_HEALTH
declare -A SERVICE_CRUD
declare -A SERVICE_DETAILS
declare -A SERVICE_DURATION

echo "=========================================="
echo "   SMOKE TEST EXECUTION"
echo "=========================================="
echo "Started at: $TIMESTAMP"
echo ""

# Phase 0: Prerequisites
echo "Phase 0: Checking prerequisites..."
if ! command -v docker &> /dev/null; then
    echo -e "${RED}✗ Docker not found${NC}"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}✗ Maven not found${NC}"
    exit 1
fi

if ! command -v node &> /dev/null; then
    echo -e "${RED}✗ Node.js not found${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All prerequisites met${NC}"
echo ""

# Function to start backend service
start_backend_service() {
    local service_name=$1
    local service_port=$2
    local service_dir="$ROOT_DIR/backend/$service_name"
    
    echo "Starting $service_name on port $service_port..."
    
    cd "$service_dir"
    mvn spring-boot:run -Dspring-boot.run.profiles=test > "$LOG_DIR/${service_name}.log" 2>&1 &
    SERVICE_PIDS[$service_name]=$!
    
    echo "  PID: ${SERVICE_PIDS[$service_name]}"
}

# Function to start frontend service
start_frontend_service() {
    local service_name=$1
    local service_port=$2
    local service_dir="$ROOT_DIR/frontend/packages/$service_name"
    
    echo "Starting $service_name on port $service_port..."
    
    cd "$service_dir"
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        echo "  Installing dependencies..."
        npm install > "$LOG_DIR/${service_name}-install.log" 2>&1
    fi
    
    # Start service with custom port
    PORT=$service_port npm run dev > "$LOG_DIR/${service_name}.log" 2>&1 &
    SERVICE_PIDS[$service_name]=$!
    
    echo "  PID: ${SERVICE_PIDS[$service_name]}"
}

# Function to wait for service health
wait_for_health() {
    local service_name=$1
    local health_url=$2
    local max_attempts=30
    local attempt=0
    
    echo "Waiting for $service_name to be healthy..."
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s -f "$health_url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service_name is healthy${NC}"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 2
    done
    
    echo -e "${RED}✗ $service_name failed to become healthy${NC}"
    return 1
}

# Function to run backend smoke test
run_backend_smoke_test() {
    local service_name=$1
    local service_dir="$ROOT_DIR/backend/$service_name"
    local test_start=$(date +%s)
    
    echo "Running smoke tests for $service_name..."
    
    cd "$service_dir"
    if mvn test -Dtest="*SmokeTest" > "$LOG_DIR/${service_name}-test.log" 2>&1; then
        local test_end=$(date +%s)
        local duration=$((test_end - test_start))
        SERVICE_STATUS[$service_name]="PASS"
        SERVICE_HEALTH[$service_name]="✅"
        SERVICE_CRUD[$service_name]="✅"
        SERVICE_DETAILS[$service_name]="All tests passed (${duration}s)"
        SERVICE_DURATION[$service_name]="${duration}s"
        echo -e "${GREEN}✓ $service_name tests passed${NC}"
        return 0
    else
        local test_end=$(date +%s)
        local duration=$((test_end - test_start))
        SERVICE_STATUS[$service_name]="FAIL"
        SERVICE_HEALTH[$service_name]="❌"
        SERVICE_CRUD[$service_name]="❌"
        SERVICE_DETAILS[$service_name]="Tests failed (${duration}s)"
        SERVICE_DURATION[$service_name]="${duration}s"
        echo -e "${RED}✗ $service_name tests failed${NC}"
        return 1
    fi
}

# Function to run frontend smoke test
run_frontend_smoke_test() {
    local service_name=$1
    local test_file="$ROOT_DIR/tests/smoke-test/${service_name}-smoke.test.js"
    local test_start=$(date +%s)
    
    echo "Running smoke tests for $service_name..."
    
    if node "$test_file" > "$LOG_DIR/${service_name}-test.log" 2>&1; then
        local test_end=$(date +%s)
        local duration=$((test_end - test_start))
        SERVICE_STATUS[$service_name]="PASS"
        SERVICE_HEALTH[$service_name]="✅"
        SERVICE_CRUD[$service_name]="✅"
        SERVICE_DETAILS[$service_name]="All tests passed (${duration}s)"
        SERVICE_DURATION[$service_name]="${duration}s"
        echo -e "${GREEN}✓ $service_name tests passed${NC}"
        return 0
    else
        local test_end=$(date +%s)
        local duration=$((test_end - test_start))
        SERVICE_STATUS[$service_name]="FAIL"
        SERVICE_HEALTH[$service_name]="❌"
        SERVICE_CRUD[$service_name]="❌"
        SERVICE_DETAILS[$service_name]="Tests failed (${duration}s)"
        SERVICE_DURATION[$service_name]="${duration}s"
        echo -e "${RED}✗ $service_name tests failed${NC}"
        return 1
    fi
}

# Function to cleanup
cleanup() {
    echo ""
    echo "Cleaning up services..."
    
    for service in "${!SERVICE_PIDS[@]}"; do
        local pid=${SERVICE_PIDS[$service]}
        if ps -p $pid > /dev/null 2>&1; then
            echo "  Stopping $service (PID: $pid)..."
            kill -15 $pid 2>/dev/null || true
            sleep 2
            if ps -p $pid > /dev/null 2>&1; then
                kill -9 $pid 2>/dev/null || true
            fi
        fi
    done
    
    echo -e "${GREEN}✓ Cleanup complete${NC}"
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Phase 1: Start Backend Services
echo "=========================================="
echo "Phase 1: Starting Backend Services"
echo "=========================================="
echo ""

start_backend_service "identity-svc" 8080
sleep 5
wait_for_health "identity-svc" "http://localhost:8080/actuator/health" || true

start_backend_service "task-svc" 8081
sleep 5
wait_for_health "task-svc" "http://localhost:8081/actuator/health" || true

start_backend_service "location-svc" 8082
sleep 5
wait_for_health "location-svc" "http://localhost:8082/actuator/health" || true

echo ""
echo "Waiting additional time for services to fully initialize..."
sleep 10

# Phase 2: Run Backend Tests
echo "=========================================="
echo "Phase 2: Running Backend Smoke Tests"
echo "=========================================="
echo ""

run_backend_smoke_test "identity-svc" || true
run_backend_smoke_test "task-svc" || true
run_backend_smoke_test "location-svc" || true

# Phase 3: Start Frontend Services
echo ""
echo "=========================================="
echo "Phase 3: Starting Frontend Services"
echo "=========================================="
echo ""

start_frontend_service "shell" 5173
sleep 5
wait_for_health "shell" "http://localhost:5173" || true

start_frontend_service "identity" 5174
sleep 5
wait_for_health "identity" "http://localhost:5174" || true

start_frontend_service "task-management" 5175
sleep 5
wait_for_health "task-management" "http://localhost:5175" || true

start_frontend_service "technician-mobile" 5176
sleep 5
wait_for_health "technician-mobile" "http://localhost:5176" || true

echo ""
echo "Waiting additional time for frontend services to fully initialize..."
sleep 10

# Phase 4: Run Frontend Tests
echo ""
echo "=========================================="
echo "Phase 4: Running Frontend Smoke Tests"
echo "=========================================="
echo ""

run_frontend_smoke_test "shell" || true
run_frontend_smoke_test "identity-frontend" || true
run_frontend_smoke_test "task-management-frontend" || true
run_frontend_smoke_test "technician-mobile-frontend" || true

# Generate Report
echo ""
echo "=========================================="
echo "Generating Report"
echo "=========================================="
echo ""

END_TIME=$(date +%s)
TOTAL_DURATION=$((END_TIME - START_TIME))
MINUTES=$((TOTAL_DURATION / 60))
SECONDS=$((TOTAL_DURATION % 60))

PASSED=0
FAILED=0
for service in "${!SERVICE_STATUS[@]}"; do
    if [ "${SERVICE_STATUS[$service]}" = "PASS" ]; then
        PASSED=$((PASSED + 1))
    else
        FAILED=$((FAILED + 1))
    fi
done

# Create report
cat > "$REPORT_FILE" << EOF
# Smoke Test Report

**Executed:** $TIMESTAMP  
**Duration:** ${MINUTES}m ${SECONDS}s

## Results Summary

**Total Services:** $((PASSED + FAILED))  
**Passed:** $PASSED  
**Failed:** $FAILED

## Backend Services

| Service | Health | CRUD | Status | Details |
|---------|--------|------|--------|---------|
| identity-svc | ${SERVICE_HEALTH[identity-svc]:-❌} | ${SERVICE_CRUD[identity-svc]:-❌} | ${SERVICE_STATUS[identity-svc]:-FAIL} | ${SERVICE_DETAILS[identity-svc]:-Service did not start} |
| task-svc | ${SERVICE_HEALTH[task-svc]:-❌} | ${SERVICE_CRUD[task-svc]:-❌} | ${SERVICE_STATUS[task-svc]:-FAIL} | ${SERVICE_DETAILS[task-svc]:-Service did not start} |
| location-svc | ${SERVICE_HEALTH[location-svc]:-❌} | ${SERVICE_CRUD[location-svc]:-❌} | ${SERVICE_STATUS[location-svc]:-FAIL} | ${SERVICE_DETAILS[location-svc]:-Service did not start} |

## Frontend Services

| Service | Health | UI | Status | Details |
|---------|--------|-----|--------|---------|
| shell | ${SERVICE_HEALTH[shell]:-❌} | ${SERVICE_CRUD[shell]:-❌} | ${SERVICE_STATUS[shell]:-FAIL} | ${SERVICE_DETAILS[shell]:-Service did not start} |
| identity | ${SERVICE_HEALTH[identity-frontend]:-❌} | ${SERVICE_CRUD[identity-frontend]:-❌} | ${SERVICE_STATUS[identity-frontend]:-FAIL} | ${SERVICE_DETAILS[identity-frontend]:-Service did not start} |
| task-management | ${SERVICE_HEALTH[task-management-frontend]:-❌} | ${SERVICE_CRUD[task-management-frontend]:-❌} | ${SERVICE_STATUS[task-management-frontend]:-FAIL} | ${SERVICE_DETAILS[task-management-frontend]:-Service did not start} |
| technician-mobile | ${SERVICE_HEALTH[technician-mobile-frontend]:-❌} | ${SERVICE_CRUD[technician-mobile-frontend]:-❌} | ${SERVICE_STATUS[technician-mobile-frontend]:-FAIL} | ${SERVICE_DETAILS[technician-mobile-frontend]:-Service did not start} |

## Logs

Detailed logs can be found in \`tests/smoke-test/logs/\`

## Summary

**Status:** $([ $FAILED -eq 0 ] && echo "✅ ALL TESTS PASSED" || echo "❌ SOME TESTS FAILED")

EOF

echo "Report generated: $REPORT_FILE"
cat "$REPORT_FILE"

# Exit with appropriate code
if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}✓ All smoke tests passed!${NC}"
    exit 0
else
    echo -e "\n${RED}✗ Some smoke tests failed${NC}"
    exit 1
fi
