-- Update all user passwords to use a properly encoded BCrypt hash for 'password'
-- This hash was generated using BCryptPasswordEncoder with strength 10
-- Hash for 'password': $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQl8fovJLr0.qJa4LhlqRQHxSe
-- The password for all users is: password

UPDATE users SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQl8fovJLr0.qJa4LhlqRQHxSe';
