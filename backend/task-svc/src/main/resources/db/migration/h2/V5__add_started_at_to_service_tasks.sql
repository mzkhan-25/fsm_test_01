-- V5__add_started_at_to_service_tasks.sql (H2 Compatible)
-- Adds the started_at column to service_tasks table to track when a task was started (IN_PROGRESS)

ALTER TABLE service_tasks ADD COLUMN started_at TIMESTAMP;

-- Index for querying tasks by started time
CREATE INDEX IF NOT EXISTS idx_service_tasks_started_at ON service_tasks(started_at);
