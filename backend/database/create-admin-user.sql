-- ============================================
-- CREATE ADMIN USER FOR FLIGHT BOOKING SYSTEM
-- ============================================

-- Connect to your database first:
-- psql -U postgres -d flight_booking

-- Create admin user
-- Email: admin@admin.com
-- Password: admin123 (hashed with BCrypt)

INSERT INTO auth_user (id, email, password, role, status, phone, created_at, updated_at)
VALUES (
  'admin-001-' || REPLACE(uuid_generate_v4()::text, '-', ''),
  'admin@admin.com',
  -- BCrypt hash for: admin123
  '$2a$10$xQh5yQnzYGOQx5xhP0kI3OqGZ6kXvXJxM.eFLLsV.8N3gX1KYI8hu',
  'ADMIN',
  'ACTIVE',
  '0123456789',
  NOW(),
  NOW()
)
ON CONFLICT (email) DO NOTHING;

-- Verify admin user created
SELECT id, email, role, status, created_at 
FROM auth_user 
WHERE email = 'admin@admin.com';

-- ============================================
-- ADMIN LOGIN CREDENTIALS
-- ============================================
-- Email: admin@admin.com
-- Password: admin123
-- ============================================

-- Optional: Create another admin user with different email
/*
INSERT INTO auth_user (id, email, password, role, status, phone, created_at, updated_at)
VALUES (
  'admin-002-' || REPLACE(uuid_generate_v4()::text, '-', ''),
  'admin2@admin.com',
  '$2a$10$xQh5yQnzYGOQx5xhP0kI3OqGZ6kXvXJxM.eFLLsV.8N3gX1KYI8hu',
  'ADMIN',
  'ACTIVE',
  '0987654321',
  NOW(),
  NOW()
)
ON CONFLICT (email) DO NOTHING;
*/

