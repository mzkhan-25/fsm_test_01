-- V1__Create_technician_locations_table.sql
-- Creates the technician_locations table for tracking technician positions

CREATE TABLE technician_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    technician_id BIGINT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    accuracy DOUBLE NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    battery_level INT,
    location GEOMETRY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for querying by technician ID (most common query pattern)
CREATE INDEX idx_technician_locations_technician_id ON technician_locations(technician_id);

-- Index for querying by timestamp (for history queries)
CREATE INDEX idx_technician_locations_timestamp ON technician_locations(timestamp);

-- Composite index for technician ID and timestamp (for latest location queries)
CREATE INDEX idx_technician_locations_tech_timestamp ON technician_locations(technician_id, timestamp DESC);
