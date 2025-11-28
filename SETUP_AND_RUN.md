# FSM Application - Complete Setup & Run Guide

## 🎯 Quick Start (Easiest Way)

### Start Everything
```bash
cd /home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01
./start-all.sh
```

### Stop Everything
```bash
./stop-all.sh
```

### Access the Application
Open http://localhost:5173 in your browser and login with credentials below.

---

## 👤 Login Credentials

### Available User Accounts

| Role | Email | Password | Description |
|------|-------|----------|-------------|
| **Admin** | admin@fsm.com | admin123 | Full system access - can create tasks, assign tasks, manage users |
| **Dispatcher** | dispatcher@fsm.com | dispatcher123 | Can create and assign tasks to technicians |
| **Supervisor** | supervisor@fsm.com | supervisor123 | Can oversee technician work and approve tasks |

### Technicians (Field Service Staff)
All technicians use password: **tech123**

- **John Smith:** john.smith@fsm.com
- **Emma Davis:** emma.davis@fsm.com
- **James Wilson:** james.wilson@fsm.com
- **Olivia Brown:** olivia.brown@fsm.com
- **William Taylor:** william.taylor@fsm.com

**Role:** TECHNICIAN - Can view assigned tasks and update their location

---

## 🌐 Service URLs

### Frontend Applications
- **Main Application (Shell)**: http://localhost:5173
- **Identity UI**: http://localhost:5174
- **Location Services UI**: http://localhost:5175
- **Task Management UI**: http://localhost:5176
- **Technician Mobile UI**: http://localhost:5178

### Backend API URLs
- **Identity API**: http://localhost:8080
- **Task API**: http://localhost:8081
- **Location API**: http://localhost:8082
- **Notification API**: http://localhost:8083

---

## 📊 Sample Data Included

### 5 Service Tasks:
1. **AC Unit Repair** (High Priority) - 123 Main Street, New York, NY
2. **Plumbing Leak Fix** (High Priority) - 456 Oak Avenue, Brooklyn, NY
3. **Electrical Panel Inspection** (Medium Priority) - 789 Elm Street, Queens, NY
4. **HVAC System Maintenance** (Low Priority) - 321 Pine Road, Manhattan, NY
5. **Water Heater Installation** (Medium Priority) - 654 Maple Drive, Bronx, NY

### 5 Technician GPS Locations:
- Brooklyn: (40.6782, -73.9442)
- Manhattan: (40.7489, -73.9680)
- Queens: (40.7282, -73.7949)
- Bronx: (40.8448, -73.8648)
- Staten Island: (40.5795, -74.1502)

---

## 📖 Manual Setup (If you prefer separate terminals)

### Terminal 1 - Identity Service (Port 8080)
```bash
cd /home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01/backend/identity-svc
mvn spring-boot:run
```

### Terminal 2 - Task Service (Port 8081)
```bash
cd /home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01/backend/task-svc
mvn spring-boot:run
```

### Terminal 3 - Location Service (Port 8082)
```bash
cd /home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01/backend/location-svc
mvn spring-boot:run
```

### Terminal 4 - Notification Service (Port 8083)
```bash
cd /home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01/backend/notification-svc
mvn spring-boot:run
```

### Terminal 5 - All Frontend Applications
```bash
cd /home/mkhan/office/COPILOT_CHAT_PROJECT_FOLDER/fsm_test_01/frontend
npm install  # Only needed first time
npm run dev
```

---

## 🔍 Verify Services Are Running

```bash
# Check all ports
for port in 8080 8081 8082 8083 5173 5174 5175 5176 5178; do
  nc -z localhost $port && echo "✓ Port $port: ACTIVE" || echo "✗ Port $port: NOT ACTIVE"
done

# Check backend health endpoints
curl http://localhost:8080/actuator/health  # Identity
curl http://localhost:8081/actuator/health  # Task
curl http://localhost:8082/actuator/health  # Location
curl http://localhost:8083/actuator/health  # Notification
```

---

## 📝 View Logs

### When using start-all.sh:
```bash
# View all backend logs
tail -f logs/*.log

# View specific service log
tail -f logs/identity-svc.log
tail -f logs/task-svc.log
tail -f logs/location-svc.log
tail -f logs/notification-svc.log
tail -f logs/frontend.log
```

### When running manually:
Logs are displayed directly in each terminal.

---

## 🛠️ Troubleshooting

### Issue: "Failed to fetch" on login

**Solution**: Make sure all backend services are running:
```bash
curl http://localhost:8080/actuator/health
```

### Issue: PostgreSQL not running

**Solution**: Start PostgreSQL container:
```bash
docker start fsm-postgres
# OR if it doesn't exist:
docker run -d --name fsm-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=identitydb \
  -p 5432:5432 postgres:15-alpine
```

### Issue: Port already in use

**Solution**: Stop all services and try again:
```bash
./stop-all.sh
sleep 5
./start-all.sh
```

### Issue: Frontend not loading

**Solution**: Reinstall dependencies:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### Issue: Can't login

**Solution**: Verify you're using the correct passwords:
- Admin: admin123
- Dispatcher: dispatcher123
- Supervisor: supervisor123
- Technicians: tech123

---

## 🗄️ Database Management

### Access PostgreSQL (Identity DB):
```bash
docker exec -it fsm-postgres psql -U postgres -d identitydb
```

### Useful SQL Queries:
```sql
-- List all users
SELECT id, name, email, role_id FROM users;

-- List all roles
SELECT * FROM roles;

-- Check Flyway migrations
SELECT * FROM flyway_schema_history;
```

---

## 📚 API Documentation

### Swagger UI (OpenAPI):
- Identity Service: http://localhost:8080/swagger-ui.html
- Task Service: http://localhost:8081/swagger-ui.html
- Location Service: http://localhost:8082/swagger-ui.html
- Notification Service: http://localhost:8083/swagger-ui.html

---

## 🔄 Restart Services

### Restart everything:
```bash
./stop-all.sh && sleep 3 && ./start-all.sh
```

### Restart only backend:
```bash
./stop-backend.sh && sleep 3 && ./start-backend.sh
```

---

## 📦 Prerequisites

- **Java 17** - For backend services
- **Maven 3.6+** - For building backend
- **Node.js 18+** - For frontend
- **npm** - For frontend dependencies
- **Docker** - For PostgreSQL database
- **PostgreSQL 15** - Running in Docker container

---

## 🎓 Development Workflow

1. **Start the application**: `./start-all.sh`
2. **Open browser**: http://localhost:5173
3. **Login**: Use dispatcher@fsm.com / dispatcher123
4. **Make changes**: Edit code in your IDE
5. **Backend changes**: Service auto-reloads (Spring Boot DevTools)
6. **Frontend changes**: Auto-reloads (Vite HMR)
7. **Stop when done**: `./stop-all.sh`

---

## ⚡ Performance Tips

- **First startup**: Takes ~30-40 seconds for all services
- **Subsequent startups**: ~15-20 seconds
- **Keep Docker running**: Faster database access
- **Use scripts**: Much easier than manual terminals

---

## 🐛 Known Issues

1. **Notification Service**: May be slow on first startup (Firebase dependencies)
2. **H2 Database**: Task/Location/Notification services use in-memory DB (data lost on restart)
3. **CORS**: Already configured for localhost:5173-5178
4. **JWT Tokens**: Expire after 24 hours, login again if needed

---

## 📞 Need Help?

Check logs first:
```bash
ls -lh logs/
tail -f logs/identity-svc.log  # or whichever service is having issues
```

Common error patterns:
- `Address already in use` → Stop services and restart
- `Connection refused` → Service not started yet, wait a few seconds
- `401 Unauthorized` → Check password is correct
- `Database connection failed` → Check PostgreSQL is running
- `Failed to fetch` → Backend service not responding, check logs

---

## ✅ Success Checklist

- [ ] All 4 backend services running (ports 8080-8083)
- [ ] All 5 frontend apps running (ports 5173-5178)
- [ ] PostgreSQL container running
- [ ] Can access http://localhost:5173
- [ ] Can login with dispatcher@fsm.com / dispatcher123
- [ ] Can see 5 tasks in task management
- [ ] Can see 5 technician locations

---

**Happy Coding! 🚀**
