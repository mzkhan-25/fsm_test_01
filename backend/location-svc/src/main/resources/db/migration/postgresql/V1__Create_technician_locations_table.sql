-- V1__Create_technician_locations_table.sql
-- Creates the technician_locations table with PostGIS support for tracking technician positions

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create the technician_locations table with geography point type
CREATE TABLE technician_locations (
    id BIGSERIAL PRIMARY KEY,
    technician_id BIGINT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    accuracy DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    battery_level INTEGER,
    location GEOGRAPHY(Point, 4326),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Standard index for querying by technician ID (most common query pattern)
CREATE INDEX idx_technician_locations_technician_id ON technician_locations(technician_id);

-- Standard index for querying by timestamp (for history queries)
CREATE INDEX idx_technician_locations_timestamp ON technician_locations(timestamp);

-- Composite index for technician ID and timestamp (for latest location queries)
CREATE INDEX idx_technician_locations_tech_timestamp ON technician_locations(technician_id, timestamp DESC);

-- Spatial index on location column for geospatial queries (e.g., find technicians within radius)
-- Using GIST index for geography type provides optimal performance for spatial queries
CREATE INDEX idx_technician_locations_location ON technician_locations USING GIST(location);

-- Add constraint to ensure latitude and longitude are valid
ALTER TABLE technician_locations ADD CONSTRAINT chk_latitude CHECK (latitude >= -90.0 AND latitude <= 90.0);
ALTER TABLE technician_locations ADD CONSTRAINT chk_longitude CHECK (longitude >= -180.0 AND longitude <= 180.0);
ALTER TABLE technician_locations ADD CONSTRAINT chk_accuracy CHECK (accuracy > 0);
ALTER TABLE technician_locations ADD CONSTRAINT chk_battery_level CHECK (battery_level >= 0 AND battery_level <= 100);
