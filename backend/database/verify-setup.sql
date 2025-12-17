-- Script kiểm tra setup database
-- Chạy script này để verify mọi thứ đã đúng

\c flight_booking

-- Kiểm tra database
SELECT 'Database: ' || current_database() as info;

-- Kiểm tra user hiện tại
SELECT 'Current user: ' || current_user as info;

-- Kiểm tra user DBMaybay có tồn tại không
SELECT 
    CASE 
        WHEN EXISTS (SELECT FROM pg_user WHERE usename = 'DBMaybay') 
        THEN '✓ User DBMaybay exists'
        ELSE '✗ User DBMaybay NOT found'
    END as user_check;

-- Kiểm tra quyền của DBMaybay
SELECT 
    'Database owner: ' || pg_catalog.pg_get_userbyid(datdba) as owner_info
FROM pg_database 
WHERE datname = 'flight_booking';

-- Kiểm tra schema ownership
SELECT 
    'Schema owner: ' || pg_catalog.pg_get_userbyid(nspowner) as schema_owner
FROM pg_namespace 
WHERE nspname = 'public';

-- Thông báo
\echo ''
\echo '========================================'
\echo 'Setup Verification Complete!'
\echo '========================================'
\echo 'Next step: Start the backend application'
\echo 'Hibernate will automatically create tables'
\echo ''

