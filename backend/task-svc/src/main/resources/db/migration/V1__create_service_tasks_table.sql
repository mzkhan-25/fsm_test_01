-- V1__create_service_tasks_table.sql
-- Creates the service_tasks table with all required fields, constraints, and indexes
-- for the Task Management bounded context.

CREATE TABLE IF NOT EXISTS service_tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    client_address VARCHAR(500) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    estimated_duration INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'UNASSIGNED',
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Domain Invariants: Constraints
    CONSTRAINT chk_title_not_empty CHECK (LENGTH(TRIM(title)) >= 3),
    CONSTRAINT chk_client_address_not_empty CHECK (LENGTH(TRIM(client_address)) > 0),
    CONSTRAINT chk_priority_valid CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT chk_status_valid CHECK (status IN ('UNASSIGNED', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT chk_estimated_duration_positive CHECK (estimated_duration IS NULL OR estimated_duration > 0)
);

-- Indexes for query performance
CREATE INDEX IF NOT EXISTS idx_service_tasks_status ON service_tasks(status);
CREATE INDEX IF NOT EXISTS idx_service_tasks_priority ON service_tasks(priority);
CREATE INDEX IF NOT EXISTS idx_service_tasks_created_at ON service_tasks(created_at);
CREATE INDEX IF NOT EXISTS idx_service_tasks_created_by ON service_tasks(created_by);

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_service_tasks_status_priority ON service_tasks(status, priority);

COMMENT ON TABLE service_tasks IS 'ServiceTask aggregate - represents field service tasks in the FSM system';
COMMENT ON COLUMN service_tasks.id IS 'Unique identifier for the task';
COMMENT ON COLUMN service_tasks.title IS 'Task title (minimum 3 characters)';
COMMENT ON COLUMN service_tasks.description IS 'Optional detailed description of the task';
COMMENT ON COLUMN service_tasks.client_address IS 'Client address where service is needed (required)';
COMMENT ON COLUMN service_tasks.priority IS 'Task priority: HIGH, MEDIUM, or LOW';
COMMENT ON COLUMN service_tasks.estimated_duration IS 'Estimated duration in minutes (must be positive if set)';
COMMENT ON COLUMN service_tasks.status IS 'Task lifecycle status: UNASSIGNED, ASSIGNED, IN_PROGRESS, COMPLETED';
COMMENT ON COLUMN service_tasks.created_by IS 'User who created the task';
COMMENT ON COLUMN service_tasks.created_at IS 'Timestamp when the task was created';
