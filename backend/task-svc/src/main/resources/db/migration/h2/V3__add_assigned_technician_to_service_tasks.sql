-- V3__add_assigned_technician_to_service_tasks.sql (H2 Compatible)
-- Adds assigned_technician_id column to service_tasks table
-- to track which technician is currently assigned to a task.

ALTER TABLE service_tasks ADD COLUMN IF NOT EXISTS assigned_technician_id BIGINT;

-- Create index for querying tasks by assigned technician
CREATE INDEX IF NOT EXISTS idx_service_tasks_assigned_technician ON service_tasks(assigned_technician_id);
