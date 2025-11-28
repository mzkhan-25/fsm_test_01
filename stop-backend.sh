#!/bin/bash

# Field Service Management - Backend Stop Script
# This script stops all running backend services

PROJECT_ROOT="/home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01"
LOG_DIR="$PROJECT_ROOT/logs"

echo "🛑 Stopping FSM Backend Services..."
echo "==================================="
echo ""

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="$LOG_DIR/$service_name.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo "Stopping $service_name (PID: $pid)..."
            kill $pid
            sleep 2
            if ps -p $pid > /dev/null 2>&1; then
                echo "  Force killing $service_name..."
                kill -9 $pid
            fi
            rm "$pid_file"
            echo "  ✓ $service_name stopped"
        else
            echo "  ⚠ $service_name not running"
            rm "$pid_file"
        fi
    else
        echo "  ℹ  $service_name PID file not found"
    fi
    echo ""
}

# Stop all services
stop_service "identity-svc"
stop_service "task-svc"
stop_service "location-svc"
stop_service "notification-svc"

# Also kill any remaining Maven processes
echo "Checking for remaining Maven processes..."
pkill -f "spring-boot:run" && echo "  ✓ Killed remaining Maven processes" || echo "  ℹ  No remaining Maven processes"

echo ""
echo "✅ All backend services stopped!"
