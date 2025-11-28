-- Insert sample technician locations (simulating real-time tracking in New York area)
-- Technician IDs correspond to users created in identity-svc

-- John Smith - Near Brooklyn
INSERT INTO technician_locations (technician_id, latitude, longitude, accuracy, timestamp, created_at) VALUES
    (4, 40.6782, -73.9442, 10.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Emma Davis - Near Manhattan
INSERT INTO technician_locations (technician_id, latitude, longitude, accuracy, timestamp, created_at) VALUES
    (5, 40.7580, -73.9855, 8.3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- James Wilson - Near Queens
INSERT INTO technician_locations (technician_id, latitude, longitude, accuracy, timestamp, created_at) VALUES
    (6, 40.7282, -73.7949, 12.1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Olivia Brown - Near Bronx
INSERT INTO technician_locations (technician_id, latitude, longitude, accuracy, timestamp, created_at) VALUES
    (7, 40.8448, -73.8648, 9.7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- William Taylor - Near Staten Island
INSERT INTO technician_locations (technician_id, latitude, longitude, accuracy, timestamp, created_at) VALUES
    (8, 40.5795, -74.1502, 11.2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
