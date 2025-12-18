-- Create User for Booking
-- User ID: 55f63366-e11d-4b67-a2df-38cf84403d13
-- Email: danh1@gmail.com

-- Step 1: Check if user already exists
DO $$
DECLARE
    v_user_id UUID := '55f63366-e11d-4b67-a2df-38cf84403d13';
    v_email VARCHAR := 'danh1@gmail.com';
    v_full_name VARCHAR := 'Danh User';
    v_phone VARCHAR := '0979008513';
    v_password VARCHAR := '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQFk82zZt8Ci9LE8tXqJ58W22u'; -- 123456 (default)
    
    user_exists BOOLEAN;
BEGIN
    -- Check if user exists
    SELECT EXISTS(SELECT 1 FROM users WHERE id = v_user_id) INTO user_exists;
    
    IF user_exists THEN
        RAISE NOTICE '✅ User already exists!';
        RAISE NOTICE 'User ID: %', v_user_id;
        RAISE NOTICE 'Email: %', (SELECT email FROM users WHERE id = v_user_id);
    ELSE
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
        RAISE NOTICE 'User Details:';
        RAISE NOTICE '  ID: %', v_user_id;
        RAISE NOTICE '  Email: %', v_email;
        RAISE NOTICE '  Phone: %', v_phone;
        RAISE NOTICE '  Password: 123456 (default)';
        RAISE NOTICE '';
        RAISE NOTICE '⚠️ You can now login with:';
        RAISE NOTICE '  Email: %', v_email;
        RAISE NOTICE '  Password: 123456';
    END IF;
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
WHERE id = '55f63366-e11d-4b67-a2df-38cf84403d13';
