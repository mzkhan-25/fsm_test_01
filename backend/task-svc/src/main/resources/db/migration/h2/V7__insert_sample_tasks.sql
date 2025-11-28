-- Insert sample service tasks
INSERT INTO service_tasks (title, description, priority, status, client_address, estimated_duration, created_by, created_at) VALUES
    ('AC Unit Repair', 'Air conditioning unit not cooling properly. Customer: Robert Johnson (+1-555-1001, robert.j@email.com). Warm air coming from vents.', 'HIGH', 'UNASSIGNED', '123 Main Street, New York, NY 10001', 120, 'dispatcher@fsm.com', CURRENT_TIMESTAMP),
    
    ('Plumbing Leak Fix', 'Kitchen sink leaking underneath. Customer: Maria Garcia (+1-555-1002, maria.g@email.com). Needs urgent attention.', 'HIGH', 'UNASSIGNED', '456 Oak Avenue, Brooklyn, NY 11201', 90, 'dispatcher@fsm.com', CURRENT_TIMESTAMP),
    
    ('Electrical Panel Inspection', 'Annual electrical panel safety inspection and maintenance. Customer: David Lee (+1-555-1003, david.lee@email.com).', 'MEDIUM', 'UNASSIGNED', '789 Elm Street, Queens, NY 11375', 60, 'dispatcher@fsm.com', CURRENT_TIMESTAMP),
    
    ('HVAC System Maintenance', 'Routine HVAC system check-up and filter replacement. Customer: Jennifer White (+1-555-1004, jennifer.w@email.com).', 'LOW', 'UNASSIGNED', '321 Pine Road, Manhattan, NY 10014', 90, 'dispatcher@fsm.com', CURRENT_TIMESTAMP),
    
    ('Water Heater Installation', 'Replace old water heater with new energy-efficient model. Customer: Michael Brown (+1-555-1005, michael.b@email.com).', 'MEDIUM', 'UNASSIGNED', '654 Maple Drive, Bronx, NY 10451', 180, 'dispatcher@fsm.com', CURRENT_TIMESTAMP);
