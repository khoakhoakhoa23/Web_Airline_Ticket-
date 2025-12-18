-- Fix Existing User - Add user to database if it doesn't exist
-- This script creates a user entry for an existing JWT token user_id

-- ⚠️ IMPORTANT: Replace the placeholders below with actual values from localStorage
-- Get values by running this in browser console (F12):
-- const user = JSON.parse(localStorage.getItem('user') || '{}');
-- console.log('User ID:', user.id);
-- console.log('Email:', user.email);
-- console.log('Full Name:', user.fullName || user.name);
-- console.log('Phone:', user.phone);

-- REPLACE THESE VALUES:
DO $$
DECLARE
    v_user_id UUID := '7bf41ea3-c384-463c-b27d-fa93bb9ea660'; -- ← REPLACE with your User ID
    v_email VARCHAR := 'user@example.com';                     -- ← REPLACE with your Email
    v_full_name VARCHAR := 'User Name';                        -- ← REPLACE with your Full Name
    v_phone VARCHAR := '0123456789';                           -- ← REPLACE with your Phone
    v_password VARCHAR := '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQFk82zZt8Ci9LE8tXqJ58W22u'; -- 123456 (default)
    
    user_exists BOOLEAN;
BEGIN
    -- Check if user already exists
    SELECT EXISTS(SELECT 1 FROM users WHERE id = v_user_id) INTO user_exists;
    
    IF user_exists THEN
        RAISE NOTICE '✅ User already exists in database: %', v_user_id;
        RAISE NOTICE 'Email: %', (SELECT email FROM users WHERE id = v_user_id);
    ELSE
        -- Create the user
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
            v_user_id,
            v_email,
            v_password,
            v_full_name,
            v_phone,
            'CUSTOMER',
            true,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
        
        RAISE NOTICE '✅ User created successfully!';
        RAISE NOTICE 'User ID: %', v_user_id;
        RAISE NOTICE 'Email: %', v_email;
        RAISE NOTICE 'Password: 123456 (default)';
        RAISE NOTICE '';
        RAISE NOTICE '⚠️ NOTE: You can now login with this email and password: 123456';
    END IF;
    
    -- Show final user details
    RAISE NOTICE '';
    RAISE NOTICE '📋 Final User Details:';
    SELECT 
        RAISE NOTICE 'ID: %', id,
        RAISE NOTICE 'Email: %', email,
        RAISE NOTICE 'Full Name: %', full_name,
        RAISE NOTICE 'Phone: %', phone,
        RAISE NOTICE 'Role: %', role
    FROM users WHERE id = v_user_id;
END $$;

-- Verify the user
SELECT 
    id,
    email,
    full_name,
    phone,
    role,
    email_verified,
    created_at
FROM users 
WHERE id = '7bf41ea3-c384-463c-b27d-fa93bb9ea660'; -- ← REPLACE with your User ID
