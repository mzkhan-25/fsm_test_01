#!/bin/bash

# Field Service Management - Complete Application Startup Script
# This script starts both backend and frontend services

PROJECT_ROOT="/home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01"
LOG_DIR="$PROJECT_ROOT/logs"

# Make scripts executable
chmod +x "$PROJECT_ROOT/start-backend.sh"
chmod +x "$PROJECT_ROOT/stop-backend.sh"

echo "🚀 Starting Complete FSM Application..."
echo "========================================="
echo ""

# Check if PostgreSQL is running
echo "Checking PostgreSQL database..."
if docker ps | grep -q fsm-postgres; then
    echo "  ✓ PostgreSQL is running"
else
    echo "  ⚠ PostgreSQL not running. Starting..."
    docker start fsm-postgres || docker run -d --name fsm-postgres \
        -e POSTGRES_PASSWORD=postgres \
        -e POSTGRES_DB=identitydb \
        -p 5432:5432 postgres:15-alpine
    sleep 5
fi
echo ""

# Start backend services
"$PROJECT_ROOT/start-backend.sh"

echo ""
echo "🌐 Starting Frontend Services..."
echo "================================"
cd "$PROJECT_ROOT/frontend"

if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

echo "Starting all frontend applications..."
npm run dev > "$LOG_DIR/frontend.log" 2>&1 &
echo "$!" > "$LOG_DIR/frontend.pid"
echo "  ✓ Frontend started (PID: $(cat $LOG_DIR/frontend.pid))"
echo "    Log: $LOG_DIR/frontend.log"

sleep 10

echo ""
echo "🔍 Checking frontend ports..."
for port in 5173 5174 5175 5176 5178; do
    if nc -z localhost $port 2>/dev/null; then
        echo "  ✓ Port $port: ACTIVE"
    else
        echo "  ✗ Port $port: NOT ACTIVE"
    fi
done

echo ""
echo "✅ Application Started Successfully!"
echo ""
echo "📱 Access Points:"
echo "  🌐 Main Application (Shell):    http://localhost:5173"
echo "  🔐 Identity Service:             http://localhost:5174"
echo "  📍 Location Services:            http://localhost:5175"
echo "  📋 Task Management:              http://localhost:5176"
echo "  📱 Technician Mobile:            http://localhost:5178"
echo ""
echo "🔧 Backend APIs:"
echo "  Identity API:      http://localhost:8080"
echo "  Task API:          http://localhost:8081"
echo "  Location API:      http://localhost:8082"
echo "  Notification API:  http://localhost:8083"
echo ""
echo "👤 Test Credentials:"
echo "  Email:    dispatcher@fsm.com"
echo "  Password: password"
echo ""
echo "📊 View Logs:"
echo "  Backend:  tail -f $LOG_DIR/*.log"
echo "  Frontend: tail -f $LOG_DIR/frontend.log"
echo ""
echo "🛑 To stop all services:"
echo "  $PROJECT_ROOT/stop-all.sh"
