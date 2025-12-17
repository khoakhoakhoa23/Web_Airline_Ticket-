-- Test Script: Booking Expiration Scheduler
-- Purpose: Create test bookings to verify scheduled task works correctly

-- =====================================================
-- 1. CREATE TEST BOOKINGS (Expired)
-- =====================================================

-- Test Booking 1: PENDING, expired 1 hour ago
INSERT INTO bookings (id, booking_code, status, user_id, total_amount, currency, hold_expires_at, created_at, updated_at)
VALUES (
    'test-exp-001',
    'BKTEST001',
    'PENDING',
    'user-test-1',
    1500000.00,
    'VND',
    NOW() - INTERVAL '1 hour',  -- Expired 1 hour ago
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '2 hours'
)
ON CONFLICT (id) DO NOTHING;

-- Test Booking 2: HOLD, expired 30 minutes ago
INSERT INTO bookings (id, booking_code, status, user_id, total_amount, currency, hold_expires_at, created_at, updated_at)
VALUES (
    'test-exp-002',
    'BKTEST002',
    'HOLD',
    'user-test-2',
    2500000.00,
    'VND',
    NOW() - INTERVAL '30 minutes',  -- Expired 30 minutes ago
    NOW() - INTERVAL '1 hour',
    NOW() - INTERVAL '1 hour'
)
ON CONFLICT (id) DO NOTHING;

-- Test Booking 3: PENDING, expired 5 minutes ago
INSERT INTO bookings (id, booking_code, status, user_id, total_amount, currency, hold_expires_at, created_at, updated_at)
VALUES (
    'test-exp-003',
    'BKTEST003',
    'PENDING',
    'user-test-3',
    3000000.00,
    'VND',
    NOW() - INTERVAL '5 minutes',  -- Expired 5 minutes ago
    NOW() - INTERVAL '30 minutes',
    NOW() - INTERVAL '30 minutes'
)
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 2. CREATE TEST BOOKINGS (Not Expired - Should NOT change)
-- =====================================================

-- Test Booking 4: PENDING, expires in 1 hour (should NOT be expired)
INSERT INTO bookings (id, booking_code, status, user_id, total_amount, currency, hold_expires_at, created_at, updated_at)
VALUES (
    'test-exp-004',
    'BKTEST004',
    'PENDING',
    'user-test-4',
    1800000.00,
    'VND',
    NOW() + INTERVAL '1 hour',  -- Expires in 1 hour
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

-- Test Booking 5: CONFIRMED (should NOT be expired even if hold_expires_at is past)
INSERT INTO bookings (id, booking_code, status, user_id, total_amount, currency, hold_expires_at, created_at, updated_at)
VALUES (
    'test-exp-005',
    'BKTEST005',
    'CONFIRMED',
    'user-test-5',
    4000000.00,
    'VND',
    NOW() - INTERVAL '2 hours',  -- Expired but CONFIRMED
    NOW() - INTERVAL '3 hours',
    NOW() - INTERVAL '3 hours'
)
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 3. VERIFY TEST DATA
-- =====================================================

SELECT 
    '=== TEST BOOKINGS CREATED ===' AS message,
    '' AS id,
    '' AS booking_code,
    '' AS status,
    '' AS hold_expires_at,
    '' AS expected_result;

SELECT 
    '' AS message,
    id,
    booking_code,
    status,
    hold_expires_at,
    CASE 
        WHEN status IN ('PENDING', 'HOLD') AND hold_expires_at < NOW() THEN 'SHOULD EXPIRE'
        WHEN status IN ('PENDING', 'HOLD') AND hold_expires_at > NOW() THEN 'SHOULD NOT EXPIRE'
        ELSE 'SHOULD NOT EXPIRE (Wrong status)'
    END AS expected_result
FROM bookings
WHERE id LIKE 'test-exp-%'
ORDER BY id;

-- =====================================================
-- 4. WAIT FOR SCHEDULER (Instructions)
-- =====================================================

SELECT '
========================================
NEXT STEPS:
========================================

1. Scheduler runs every 60 seconds
   Wait 1-2 minutes for scheduled task to run

2. Check backend logs for:
   "Starting scheduled task: expire hold bookings"
   "Found 3 expired bookings"
   "Successfully expired 3 bookings"

3. Run verification query below to check results

========================================
' AS instructions;

-- =====================================================
-- 5. VERIFICATION QUERY (Run after scheduler executes)
-- =====================================================

-- Run this query AFTER waiting 1-2 minutes:
/*

SELECT 
    '=== VERIFICATION RESULTS ===' AS message,
    '' AS id,
    '' AS booking_code,
    '' AS old_status,
    '' AS new_status,
    '' AS result;

SELECT 
    '' AS message,
    id,
    booking_code,
    'PENDING/HOLD' AS old_status,
    status AS new_status,
    CASE 
        WHEN id IN ('test-exp-001', 'test-exp-002', 'test-exp-003') THEN
            CASE WHEN status = 'EXPIRED' THEN '✅ PASS' ELSE '❌ FAIL' END
        WHEN id = 'test-exp-004' THEN
            CASE WHEN status = 'PENDING' THEN '✅ PASS' ELSE '❌ FAIL' END
        WHEN id = 'test-exp-005' THEN
            CASE WHEN status = 'CONFIRMED' THEN '✅ PASS' ELSE '❌ FAIL' END
        ELSE '?'
    END AS result
FROM bookings
WHERE id LIKE 'test-exp-%'
ORDER BY id;

*/

-- =====================================================
-- 6. CLEANUP TEST DATA
-- =====================================================

-- Run this after testing:
/*

DELETE FROM bookings WHERE id LIKE 'test-exp-%';

SELECT 'Test data cleaned up' AS message;

*/

-- =====================================================
-- EXPECTED RESULTS SUMMARY
-- =====================================================

/*

Expected after scheduler runs:

| ID            | Code      | Expected Status | Reason                        |
|---------------|-----------|-----------------|-------------------------------|
| test-exp-001  | BKTEST001 | EXPIRED         | PENDING + expired 1h ago      |
| test-exp-002  | BKTEST002 | EXPIRED         | HOLD + expired 30m ago        |
| test-exp-003  | BKTEST003 | EXPIRED         | PENDING + expired 5m ago      |
| test-exp-004  | BKTEST004 | PENDING         | Not expired yet (1h future)   |
| test-exp-005  | BKTEST005 | CONFIRMED       | Wrong status (not PENDING/HOLD)|

*/

