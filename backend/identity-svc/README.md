# Identity Service (identity-svc)

Identity and Access Management microservice for the Field Service Management System.

## Overview

This service manages user accounts and roles for the FSM system. It provides the foundational domain models for authentication and authorization.

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Maven**: Build tool
- **H2**: In-memory database (development/testing)
- **PostgreSQL**: Production database (when configured)
- **JUnit 5**: Testing framework
- **Lombok**: Reduce boilerplate code
- **Swagger/OpenAPI**: API documentation

## Domain Models

### Role Enum
Four user roles in the system:
- `ADMIN` - Full system access
- `DISPATCHER` - Manages task creation and assignment
- `SUPERVISOR` - Monitors operations and analyzes performance
- `TECHNICIAN` - Completes field tasks

### User Entity
User attributes:
- `id` (Long) - Unique identifier
- `name` (String) - User's full name (required)
- `email` (String) - Email address, must be unique (required)
- `phone` (String) - Phone number in E.164 format (optional)
- `role` (Role) - User's role (required)
- `status` (UserStatus) - ACTIVE or INACTIVE (default: ACTIVE)

### Domain Invariants
1. **Email Uniqueness**: Each email can only be associated with one user
2. **Single Role**: Each user must have exactly one role
3. **Status Constraint**: User status can only be ACTIVE or INACTIVE

## Sample Users

The repository is pre-populated with 4 sample users for testing:

| Name | Email | Role | Status |
|------|-------|------|--------|
| John Administrator | admin@fsm.com | ADMIN | ACTIVE |
| Sarah Dispatcher | sarah.dispatcher@fsm.com | DISPATCHER | ACTIVE |
| Mike Supervisor | mike.supervisor@fsm.com | SUPERVISOR | ACTIVE |
| Tom Technician | tom.technician@fsm.com | TECHNICIAN | ACTIVE |

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Run Application
```bash
mvn spring-boot:run
```

The application will start on port 8080.

### Package
```bash
mvn clean package
java -jar target/identity-svc-0.0.1-SNAPSHOT.jar
```

## API Documentation

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

API docs available at:
```
http://localhost:8080/api-docs
```

## Testing

### Test Coverage
- **Line Coverage**: 88% (exceeds 85% requirement)
- **Total Tests**: 55
- **Test Suites**: 3
  - RoleTest: 8 tests
  - UserTest: 18 tests
  - UserRepositoryTest: 29 tests

### Running Tests with Coverage
```bash
mvn test
```

View coverage report at: `target/site/jacoco/index.html`

## Configuration

Main configuration in `src/main/resources/application.properties`:

```properties
spring.application.name=identity-svc
server.port=8080

# H2 Database (default for development)
spring.datasource.url=jdbc:h2:mem:identitydb
spring.jpa.hibernate.ddl-auto=create-drop
```

## Project Structure

```
identity-svc/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/fsm/identity/
│   │   │       ├── IdentityServiceApplication.java
│   │   │       └── domain/
│   │   │           ├── model/
│   │   │           │   ├── Role.java
│   │   │           │   └── User.java
│   │   │           └── repository/
│   │   │               └── UserRepository.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/fsm/identity/
│               └── domain/
│                   ├── model/
│                   │   ├── RoleTest.java
│                   │   └── UserTest.java
│                   └── repository/
│                       └── UserRepositoryTest.java
├── pom.xml
└── README.md
```

## Next Steps

This is the first task in STORY-021 (User Account and Role Management). Future tasks will build upon this foundation to add:
- REST API endpoints for user management
- Service layer with business logic
- Database persistence with PostgreSQL
- Authentication and authorization
- Integration tests

## Related

- **Issue**: #40 - TASK-001: Create User and Role Domain Models
- **Story**: #37 - STORY-021: User Account and Role Management
- **Bounded Context**: Identity & Access Management
