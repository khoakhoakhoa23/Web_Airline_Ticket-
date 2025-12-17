-- Script kiểm tra schema của bảng auth_user
-- Chạy: psql -U dbmaybay -d flight_booking -f check-auth-user-table.sql

\c flight_booking

-- Kiểm tra bảng auth_user có tồn tại không
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'auth_user'
) AS table_exists;

-- Xem cấu trúc bảng auth_user
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default,
    character_maximum_length
FROM information_schema.columns
WHERE table_schema = 'public' 
AND table_name = 'auth_user'
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
AND tc.table_name = 'auth_user'
ORDER BY tc.constraint_type, kcu.column_name;

-- Xem indexes
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public' 
AND tablename = 'auth_user';

-- Kiểm tra quyền của user dbmaybay
SELECT 
    grantee,
    privilege_type
FROM information_schema.role_table_grants
WHERE table_schema = 'public' 
AND table_name = 'auth_user'
AND grantee = 'dbmaybay';

-- So sánh với bảng users (nếu có)
SELECT 
    'auth_user' as table_name,
    COUNT(*) as column_count
FROM information_schema.columns
WHERE table_schema = 'public' 
AND table_name = 'auth_user'
UNION ALL
SELECT 
    'users' as table_name,
    COUNT(*) as column_count
FROM information_schema.columns
WHERE table_schema = 'public' 
AND table_name = 'users';

