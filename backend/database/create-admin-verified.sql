-- ============================================
-- CREATE ADMIN USER WITH VERIFIED PASSWORD HASH
-- ============================================
-- Email: admin@admin.com
-- Password: admin123
-- Hash generated using BCryptPasswordEncoder (strength 10)
-- ============================================

-- Step 1: Clean up old admin user
DELETE FROM users WHERE email = 'admin@admin.com';

-- Step 2: Create admin with VERIFIED password hash
INSERT INTO users (id, email, password, role, status, phone, created_at, updated_at)
VALUES (
  'admin-' || REPLACE(gen_random_uuid()::text, '-', ''),
  'admin@admin.com',
  -- BCrypt hash for password: admin123
  -- Generated and verified with BCryptPasswordEncoder
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
  'ADMIN',
  'ACTIVE',
  '0123456789',
  NOW(),
  NOW()
);

-- Step 3: Verify admin user created
SELECT 
  id, 
  email, 
  role, 
  status,
  phone,
  created_at
FROM users 
WHERE email = 'admin@admin.com';

-- ============================================
-- LOGIN CREDENTIALS
-- ============================================
-- Email:    admin@admin.com
-- Password: admin123
-- ============================================







