-- Check and Fix User for Booking
-- This script checks if a user exists and creates it if needed

-- ⚠️ IMPORTANT: Replace USER_ID_HERE with actual user ID from JWT token
-- Get user ID by running this in browser console (F12):
-- const user = JSON.parse(localStorage.getItem('user') || '{}');
-- console.log('User ID:', user.id);

-- REPLACE THIS:
DO $$
DECLARE
    v_user_id UUID := 'USER_ID_HERE'; -- ← REPLACE with your User ID
    v_email VARCHAR := 'user@example.com'; -- ← REPLACE with your Email
    v_full_name VARCHAR := 'User Name'; -- ← REPLACE with your Full Name
    v_phone VARCHAR := '0123456789'; -- ← REPLACE with your Phone
    v_password VARCHAR := '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQFk82zZt8Ci9LE8tXqJ58W22u'; -- 123456 (default)
    
    user_exists BOOLEAN;
    user_count INTEGER;
BEGIN
    -- Check if user exists
    SELECT EXISTS(SELECT 1 FROM users WHERE id = v_user_id) INTO user_exists;
    SELECT COUNT(*) INTO user_count FROM users WHERE id = v_user_id;
    
    RAISE NOTICE '=== User Check Results ===';
    RAISE NOTICE 'User ID: %', v_user_id;
    RAISE NOTICE 'User exists: %', user_exists;
    RAISE NOTICE 'User count: %', user_count;
    
    IF user_exists THEN
        RAISE NOTICE '';
        RAISE NOTICE '✅ User EXISTS in database!';
        RAISE NOTICE '';
        RAISE NOTICE 'User Details:';
        SELECT 
            RAISE NOTICE '  ID: %', id,
            RAISE NOTICE '  Email: %', email,
            RAISE NOTICE '  Full Name: %', COALESCE(full_name, 'NULL'),
            RAISE NOTICE '  Phone: %', COALESCE(phone, 'NULL'),
            RAISE NOTICE '  Role: %', COALESCE(role, 'NULL'),
            RAISE NOTICE '  Status: %', COALESCE(status, 'NULL'),
            RAISE NOTICE '  Created At: %', created_at
        FROM users WHERE id = v_user_id;
    ELSE
        RAISE NOTICE '';
        RAISE NOTICE '❌ User DOES NOT EXIST in database!';
        RAISE NOTICE '';
        RAISE NOTICE 'Creating user...';
        
        -- Create the user
        INSERT INTO users (
            id,
            email,
            password,
            full_name,
            phone,
            role,
            status,
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
            'ACTIVE',
            true,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
        
        RAISE NOTICE '✅ User created successfully!';
        RAISE NOTICE '';
        RAISE NOTICE '⚠️ NOTE: You can now login with:';
        RAISE NOTICE '  Email: %', v_email;
        RAISE NOTICE '  Password: 123456 (default)';
    END IF;
    
    RAISE NOTICE '';
    RAISE NOTICE '=== All Users in Database ===';
    FOR rec IN SELECT id, email, full_name, role, status FROM users ORDER BY created_at DESC LIMIT 10
    LOOP
        RAISE NOTICE '  % | % | % | % | %', rec.id, rec.email, COALESCE(rec.full_name, 'NULL'), rec.role, rec.status;
    END LOOP;
END $$;

-- Verify the user
SELECT 
    id,
    email,
    full_name,
    phone,
    role,
    status,
    email_verified,
    created_at
FROM users 
WHERE id = 'USER_ID_HERE'; -- ← REPLACE with your User ID
