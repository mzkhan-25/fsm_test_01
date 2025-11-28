#!/bin/bash

# Field Service Management - Complete Application Stop Script
# This script stops both backend and frontend services

PROJECT_ROOT="/home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01"
LOG_DIR="$PROJECT_ROOT/logs"

echo "🛑 Stopping Complete FSM Application..."
echo "========================================"
echo ""

# Stop backend
"$PROJECT_ROOT/stop-backend.sh"

echo ""
echo "🌐 Stopping Frontend Services..."
echo "================================"

# Stop frontend
if [ -f "$LOG_DIR/frontend.pid" ]; then
    local pid=$(cat "$LOG_DIR/frontend.pid")
    if ps -p $pid > /dev/null 2>&1; then
        echo "Stopping frontend (PID: $pid)..."
        kill $pid
        sleep 2
        if ps -p $pid > /dev/null 2>&1; then
            kill -9 $pid
        fi
        rm "$LOG_DIR/frontend.pid"
        echo "  ✓ Frontend stopped"
    else
        echo "  ⚠ Frontend not running"
        rm "$LOG_DIR/frontend.pid"
    fi
else
    echo "  ℹ  Frontend PID file not found"
fi

# Kill any remaining npm processes
pkill -f "npm run dev" && echo "  ✓ Killed remaining npm processes" || echo "  ℹ  No remaining npm processes"

echo ""
echo "✅ All services stopped!"
