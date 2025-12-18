-- Cleanup Duplicate Foreign Key Constraint
-- This script removes the old constraint and keeps only fk_bookings_user_id
-- Run: psql -U dbmaybay -d flight_booking -f cleanup-duplicate-constraint.sql

\c flight_booking

-- Drop the old constraint (keep only fk_bookings_user_id)
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS fkahlndm425a1xe39wrvkxnmc3v;

-- Verify only one constraint remains
SELECT
    tc.constraint_name,
    ccu.table_name AS foreign_table_name
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

-- Expected: Only fk_bookings_user_id should remain
