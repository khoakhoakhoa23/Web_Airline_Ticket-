-- Verify and Fix All Users in Database
-- This script checks all users and ensures they can create bookings

\c flight_booking

-- Step 1: Check table exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'users'
    ) THEN
        RAISE EXCEPTION 'Table "users" does not exist!';
    END IF;
END $$;

-- Step 2: Show current users
\echo '========================================'
\echo 'Current Users in Database:'
\echo '========================================'
SELECT 
    id,
    email,
    COALESCE(phone, 'NULL') as phone,
    COALESCE(role, 'NULL') as role,
    COALESCE(status, 'NULL') as status,
    created_at
FROM users 
ORDER BY created_at DESC;

-- Step 3: Check for users with missing required fields
\echo ''
\echo '========================================'
\echo 'Users with Missing Required Fields:'
\echo '========================================'
SELECT 
    id,
    email,
    CASE 
        WHEN id IS NULL THEN 'Missing ID'
        WHEN email IS NULL THEN 'Missing Email'
        WHEN password IS NULL THEN 'Missing Password'
        WHEN role IS NULL THEN 'Missing Role'
        WHEN status IS NULL THEN 'Missing Status'
        ELSE 'OK'
    END as issue
FROM users
WHERE id IS NULL 
   OR email IS NULL 
   OR password IS NULL
   OR role IS NULL
   OR status IS NULL;

-- Step 4: Fix users with missing role or status
\echo ''
\echo '========================================'
\echo 'Fixing Users with Missing Role/Status:'
\echo '========================================'

UPDATE users 
SET 
    role = COALESCE(role, 'CUSTOMER'),
    status = COALESCE(status, 'ACTIVE')
WHERE role IS NULL OR status IS NULL;

-- Step 5: Verify all users are valid for booking
\echo ''
\echo '========================================'
\echo 'Users Valid for Booking:'
\echo '========================================'
SELECT 
    id,
    email,
    role,
    status,
    CASE 
        WHEN id IS NOT NULL 
         AND email IS NOT NULL 
         AND password IS NOT NULL 
         AND role IS NOT NULL 
         AND status = 'ACTIVE' 
        THEN '✅ VALID'
        ELSE '❌ INVALID'
    END as booking_status
FROM users
ORDER BY created_at DESC;

-- Step 6: Count users
\echo ''
\echo '========================================'
\echo 'User Statistics:'
\echo '========================================'
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_users,
    COUNT(CASE WHEN role = 'CUSTOMER' THEN 1 END) as customer_users,
    COUNT(CASE WHEN role = 'ADMIN' THEN 1 END) as admin_users
FROM users;

\echo ''
\echo '✅ User verification complete!'
\echo ''
\echo 'All users with role=CUSTOMER and status=ACTIVE can create bookings.'
