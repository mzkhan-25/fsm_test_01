-- Add device_token column to users table for push notifications
ALTER TABLE users ADD COLUMN device_token VARCHAR(512);

-- Create index on device_token for faster lookups
CREATE INDEX idx_users_device_token ON users(device_token);
