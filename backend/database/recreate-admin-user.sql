-- ============================================
-- RECREATE ADMIN USER
-- ============================================

-- Step 1: Delete old admin user (if exists)
DELETE FROM users WHERE email = 'admin@admin.com';

-- Step 2: Create new admin user
INSERT INTO users (id, email, password, role, status, phone, created_at, updated_at)
VALUES (
  'admin-' || REPLACE(gen_random_uuid()::text, '-', ''),
  'admin@admin.com',
  -- BCrypt hash for: admin123
  '$2a$10$xQh5yQnzYGOQx5xhP0kI3OqGZ6kXvXJxM.eFLLsV.8N3gX1KYI8hu',
  'ADMIN',
  'ACTIVE',
  '0123456789',
  NOW(),
  NOW()
);

-- Step 3: Verify admin user
SELECT 
  id, 
  email, 
  role, 
  status, 
  created_at
FROM users 
WHERE email = 'admin@admin.com';

-- ============================================
-- ADMIN LOGIN CREDENTIALS
-- ============================================
-- Email:    admin@admin.com
-- Password: admin123
-- ============================================






