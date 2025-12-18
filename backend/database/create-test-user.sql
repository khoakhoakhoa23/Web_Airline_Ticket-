-- Create Test User for Booking Flow Testing
-- This creates a valid user that can be used for testing booking functionality

-- Step 1: Delete existing test user if any
DELETE FROM bookings WHERE user_id IN (
    SELECT id FROM users WHERE email = 'testuser@gmail.com'
);
DELETE FROM users WHERE email = 'testuser@gmail.com';

-- Step 2: Create test user
-- Password: 123456 (hashed with BCrypt)
INSERT INTO users (
    id,
    email,
    password,
    full_name,
    phone,
    role,
    email_verified,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'testuser@gmail.com',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQFk82zZt8Ci9LE8tXqJ58W22u', -- 123456
    'Test User',
    '0123456789',
    'CUSTOMER',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Verify the user was created
SELECT 
    id,
    email,
    full_name,
    phone,
    role,
    email_verified,
    created_at
FROM users 
WHERE email = 'testuser@gmail.com';

-- Output message
DO $$
DECLARE
    user_id_var UUID;
BEGIN
    SELECT id INTO user_id_var FROM users WHERE email = 'testuser@gmail.com';
    RAISE NOTICE '✅ Test user created successfully!';
    RAISE NOTICE 'Email: testuser@gmail.com';
    RAISE NOTICE 'Password: 123456';
    RAISE NOTICE 'User ID: %', user_id_var;
END $$;
