-- Fix Foreign Key Constraint for bookings table
-- This script fixes the foreign key constraint to reference auth_user instead of users
-- Run: psql -U dbmaybay -d flight_booking -f fix-bookings-foreign-key.sql

\c flight_booking

-- Step 1: Check current foreign key constraints on bookings table
\echo '========================================'
\echo 'Current Foreign Key Constraints on bookings:'
\echo '========================================'
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_name = 'bookings'
    AND kcu.column_name = 'user_id';

-- Step 2: Drop existing foreign key constraints that reference 'users' table
\echo ''
\echo '========================================'
\echo 'Dropping Foreign Key Constraints referencing users table:'
\echo '========================================'

DO $$
DECLARE
    constraint_name_var TEXT;
BEGIN
    -- Find and drop all foreign key constraints on bookings.user_id that reference 'users' table
    FOR constraint_name_var IN
        SELECT tc.constraint_name
        FROM information_schema.table_constraints AS tc
        JOIN information_schema.key_column_usage AS kcu
            ON tc.constraint_name = kcu.constraint_name
            AND tc.table_schema = kcu.table_schema
        JOIN information_schema.constraint_column_usage AS ccu
            ON ccu.constraint_name = tc.constraint_name
            AND ccu.table_schema = tc.table_schema
        WHERE tc.constraint_type = 'FOREIGN KEY'
            AND tc.table_name = 'bookings'
            AND kcu.column_name = 'user_id'
            AND ccu.table_name = 'users'
    LOOP
        EXECUTE format('ALTER TABLE bookings DROP CONSTRAINT IF EXISTS %I', constraint_name_var);
        RAISE NOTICE 'Dropped constraint: %', constraint_name_var;
    END LOOP;
END $$;

-- Step 3: Verify auth_user table exists
\echo ''
\echo '========================================'
\echo 'Verifying auth_user table exists:'
\echo '========================================'
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'auth_user'
) AS auth_user_exists;

-- Step 4: Create new foreign key constraint referencing auth_user
\echo ''
\echo '========================================'
\echo 'Creating Foreign Key Constraint referencing auth_user:'
\echo '========================================'

-- Drop constraint if it already exists (in case it references auth_user but with wrong name)
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS fk_bookings_user_id;

-- Create new foreign key constraint
DO $$
BEGIN
    ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_user_id
    FOREIGN KEY (user_id)
    REFERENCES auth_user(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;
    
    RAISE NOTICE '✅ Foreign key constraint created: fk_bookings_user_id';
EXCEPTION
    WHEN duplicate_object THEN
        RAISE NOTICE 'Constraint fk_bookings_user_id already exists';
END $$;

-- Step 5: Verify the new constraint
\echo ''
\echo '========================================'
\echo 'Verifying New Foreign Key Constraint:'
\echo '========================================'
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_name = 'bookings'
    AND kcu.column_name = 'user_id';

-- Step 6: Check for orphaned bookings (bookings with user_id not in auth_user)
\echo ''
\echo '========================================'
\echo 'Checking for Orphaned Bookings:'
\echo '========================================'
SELECT 
    b.id,
    b.booking_code,
    b.user_id,
    b.status,
    b.created_at
FROM bookings b
LEFT JOIN auth_user u ON b.user_id = u.id
WHERE u.id IS NULL;

-- Step 7: Summary
\echo ''
\echo '========================================'
\echo '✅ Fix Complete!'
\echo '========================================'
\echo 'Foreign key constraint now references auth_user table'
\echo 'If there are orphaned bookings, you may need to:'
\echo '  1. Create missing users in auth_user table'
\echo '  2. Or delete orphaned bookings'
\echo '========================================'
