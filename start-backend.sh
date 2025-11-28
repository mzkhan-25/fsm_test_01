#!/bin/bash

# Field Service Management - Backend Startup Script
# This script starts all 4 backend services in the background

PROJECT_ROOT="/home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01"
LOG_DIR="$PROJECT_ROOT/logs"

# Create logs directory if it doesn't exist
mkdir -p "$LOG_DIR"

echo "🚀 Starting FSM Backend Services..."
echo "=================================="
echo ""

# Function to start a service
start_service() {
    local service_name=$1
    local service_port=$2
    local service_dir="$PROJECT_ROOT/backend/$service_name"
    
    echo "Starting $service_name on port $service_port..."
    cd "$service_dir"
    mvn spring-boot:run > "$LOG_DIR/$service_name.log" 2>&1 &
    echo "$!" > "$LOG_DIR/$service_name.pid"
    echo "  ✓ $service_name started (PID: $(cat $LOG_DIR/$service_name.pid))"
    echo "    Log: $LOG_DIR/$service_name.log"
    echo ""
}

# Start all backend services
start_service "identity-svc" "8080"
start_service "task-svc" "8081"
start_service "location-svc" "8082"
start_service "notification-svc" "8083"

echo "⏳ Waiting for services to start..."
sleep 20

echo ""
echo "🔍 Checking service status..."
echo "=============================="
for port in 8080 8081 8082 8083; do
    if nc -z localhost $port 2>/dev/null; then
        echo "  ✓ Port $port: ACTIVE"
    else
        echo "  ✗ Port $port: NOT ACTIVE (check logs)"
    fi
done

echo ""
echo "✅ Backend services startup complete!"
echo ""
echo "To view logs:"
echo "  tail -f $LOG_DIR/identity-svc.log"
echo "  tail -f $LOG_DIR/task-svc.log"
echo "  tail -f $LOG_DIR/location-svc.log"
echo "  tail -f $LOG_DIR/notification-svc.log"
echo ""
echo "To stop all services, run:"
echo "  $PROJECT_ROOT/stop-backend.sh"
