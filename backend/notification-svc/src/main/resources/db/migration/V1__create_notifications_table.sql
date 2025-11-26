-- V1__create_notifications_table.sql
-- Creates the notifications table with all required fields, constraints, and indexes
-- for the Notification System bounded context.

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    data TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Domain Invariants: Constraints
    CONSTRAINT chk_title_not_empty CHECK (LENGTH(TRIM(title)) > 0),
    CONSTRAINT chk_message_not_empty CHECK (LENGTH(TRIM(message)) > 0),
    CONSTRAINT chk_type_valid CHECK (type IN ('PUSH', 'EMAIL', 'SMS'))
);

-- Indexes for efficient queries
-- Index on userId for retrieving user's notifications
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);

-- Index on sentAt for querying notifications by sent date (last 30 days queries)
CREATE INDEX IF NOT EXISTS idx_notifications_sent_at ON notifications(sent_at);

-- Composite index for unread notifications per user
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read);

-- Composite index for user recent notifications queries
CREATE INDEX IF NOT EXISTS idx_notifications_user_sent_at ON notifications(user_id, sent_at);

-- Composite index for user notifications ordered by created date
CREATE INDEX IF NOT EXISTS idx_notifications_user_created_at ON notifications(user_id, created_at);

COMMENT ON TABLE notifications IS 'Notification aggregate - represents alert delivery in the FSM system';
COMMENT ON COLUMN notifications.id IS 'Unique identifier for the notification';
COMMENT ON COLUMN notifications.user_id IS 'Recipient user ID (required)';
COMMENT ON COLUMN notifications.type IS 'Notification type determining delivery channel: PUSH, EMAIL, or SMS';
COMMENT ON COLUMN notifications.title IS 'Notification title (required)';
COMMENT ON COLUMN notifications.message IS 'Notification message content (required, max 1000 chars)';
COMMENT ON COLUMN notifications.data IS 'Optional JSON data associated with the notification';
COMMENT ON COLUMN notifications.is_read IS 'Whether the notification has been read by the user';
COMMENT ON COLUMN notifications.sent_at IS 'Timestamp when notification was sent';
COMMENT ON COLUMN notifications.delivered_at IS 'Timestamp when notification was delivered (indicates successful push)';
COMMENT ON COLUMN notifications.created_at IS 'Timestamp when the notification was created';
