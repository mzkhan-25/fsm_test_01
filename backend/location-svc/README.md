# Location Service

The Location Service is responsible for tracking technician positions in real-time for the Field Service Management (FSM) system.

## Bounded Context

This service is part of the **Location Services** bounded context and provides:
- Real-time technician location tracking
- Location history for technicians
- Geospatial data validation
- Support for map display features

## Domain Model

### TechnicianLocation Entity

The main aggregate root for this service with the following fields:
- `id` - Unique identifier
- `technicianId` - Reference to the technician
- `latitude` - GPS latitude (-90 to 90)
- `longitude` - GPS longitude (-180 to 180)
- `accuracy` - Location accuracy in meters (must be positive)
- `timestamp` - Time when the location was recorded (must not be in future)
- `batteryLevel` - Device battery level (0-100, optional)
- `location` - PostGIS Point geometry (SRID 4326) for geospatial queries

### Domain Invariants

- Latitude must be between -90 and 90
- Longitude must be between -180 and 180
- Accuracy must be positive (in meters)
- Location timestamp must not be in the future
- Location Point is automatically created from latitude/longitude

## PostGIS Integration

The service uses PostGIS for efficient geospatial queries. Key features:

### Geospatial Queries

1. **Find technicians within radius** - Locate all technicians within a specified distance from a point
2. **Find active technicians within radius** - Same as above, but only for recently active technicians
3. **Get last known positions** - Retrieve the most recent location for all active technicians

### Spatial Index

A GIST spatial index is created on the `location` column for optimal query performance:
```sql
CREATE INDEX idx_technician_locations_location ON technician_locations USING GIST(location);
```

### Usage Example

```java
// Find technicians within 5km of a point
Point center = geometryFactory.createPoint(new Coordinate(-89.6501, 39.7817));
List<TechnicianLocation> nearby = repository.findTechniciansWithinRadius(center, 5000);

// Find active technicians (updated in last 10 minutes) within 3km
LocalDateTime since = LocalDateTime.now().minusMinutes(10);
List<TechnicianLocation> active = repository.findActiveTechniciansWithinRadius(center, 3000, since);

// Get last known positions for all active technicians
List<TechnicianLocation> lastPositions = repository.findLastKnownPositionsForActiveTechnicians(since);
```

## API Endpoints

(To be implemented in future tasks)

## Configuration

### Application Properties

```properties
server.port=8082
spring.application.name=location-svc
```

### Database

- Development/Testing: H2 in-memory database
- Production: PostgreSQL with PostGIS extension

### PostgreSQL Setup

For production deployment with PostGIS:

1. Install PostgreSQL and PostGIS extension:
```bash
# On Ubuntu/Debian
sudo apt-get install postgresql postgresql-contrib postgis

# On macOS with Homebrew
brew install postgresql postgis
```

2. Create database and enable PostGIS:
```sql
CREATE DATABASE locationdb;
\c locationdb
CREATE EXTENSION postgis;
```

3. Run with PostgreSQL profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgresql
```

Or set environment variable:
```bash
export SPRING_PROFILES_ACTIVE=postgresql
mvn spring-boot:run
```

## Building

```bash
cd backend/location-svc
mvn clean install
```

## Testing

```bash
mvn test
```

## Running

```bash
mvn spring-boot:run
```

## Swagger UI

When running, access the API documentation at:
- http://localhost:8082/swagger-ui.html

## Related Issues

- Story: STORY-018 - Real-time Technician Location Tracking
- Task: TASK-030 - Create Location Aggregate Domain Model
- Task: TASK-031 - Implement Location Repository with PostGIS

## Architecture Notes

### Why PostGIS?

PostGIS provides:
- **Accurate distance calculations** using geography type (considers Earth's curvature)
- **Efficient spatial indexing** with GIST indexes for fast radius queries
- **Industry-standard** SQL/MM spatial functions
- **Scalability** for large-scale location tracking

### Performance Considerations

- Spatial index (GIST) on location column enables fast proximity searches
- Standard indexes on technician_id and timestamp for common queries
- Location Point is automatically created/updated from latitude/longitude fields
- Old location data can be archived (recommendation: keep last 24 hours live)
