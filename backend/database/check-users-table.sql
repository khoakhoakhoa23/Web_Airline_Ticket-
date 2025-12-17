-- Script kiểm tra schema của bảng users
-- Chạy: psql -U dbmaybay -d flight_booking -f check-users-table.sql

\c flight_booking

-- Kiểm tra bảng users có tồn tại không
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'users'
) AS table_exists;

-- Xem cấu trúc bảng users
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default,
    character_maximum_length
FROM information_schema.columns
WHERE table_schema = 'public' 
AND table_name = 'users'
ORDER BY ordinal_position;

-- Xem constraints (NOT NULL, UNIQUE, PRIMARY KEY)
SELECT
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name,
    tc.table_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_schema = 'public' 
AND tc.table_name = 'users'
ORDER BY tc.constraint_type, kcu.column_name;

-- Xem indexes
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public' 
AND tablename = 'users';

-- Kiểm tra quyền của user dbmaybay
SELECT 
    grantee,
    privilege_type
FROM information_schema.role_table_grants
WHERE table_schema = 'public' 
AND table_name = 'users'
AND grantee = 'dbmaybay';

