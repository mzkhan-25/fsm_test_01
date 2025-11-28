-- Insert sample users with BCrypt encoded password 'password123'
-- Password hash for 'password123': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Admin user
INSERT INTO users (name, email, phone, role_id, status, password) VALUES
    ('Admin User', 'admin@fsm.com', '+1-555-0100', 1, 'ACTIVE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Dispatcher user
INSERT INTO users (name, email, phone, role_id, status, password) VALUES
    ('Sarah Johnson', 'dispatcher@fsm.com', '+1-555-0101', 2, 'ACTIVE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Supervisor user
INSERT INTO users (name, email, phone, role_id, status, password) VALUES
    ('Mike Williams', 'supervisor@fsm.com', '+1-555-0102', 3, 'ACTIVE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

-- Technician users
INSERT INTO users (name, email, phone, role_id, status, password) VALUES
    ('John Smith', 'john.smith@fsm.com', '+1-555-0201', 4, 'ACTIVE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    ('Emma Davis', 'emma.davis@fsm.com', '+1-555-0202', 4, 'ACTIVE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    ('James Wilson', 'james.wilson@fsm.com', '+1-555-0203', 4, 'ACTIVE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    ('Olivia Brown', 'olivia.brown@fsm.com', '+1-555-0204', 4, 'ACTIVE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    ('William Taylor', 'william.taylor@fsm.com', '+1-555-0205', 4, 'ACTIVE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');
