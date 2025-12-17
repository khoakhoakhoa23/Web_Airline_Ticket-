-- Script FIX database schema cho bảng users
-- Chạy: psql -U dbmaybay -d flight_booking -f fix-users-table.sql
-- HOẶC: psql -U postgres -d flight_booking -f fix-users-table.sql

\c flight_booking

-- Bước 1: Xóa bảng cũ nếu có (CHỈ CHẠY NẾU MUỐN RESET HOÀN TOÀN)
-- DROP TABLE IF EXISTS users CASCADE;

-- Bước 2: Tạo lại bảng users với đúng schema
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    role VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_email_key UNIQUE (email)
);

-- Bước 3: Đảm bảo tất cả constraints đúng
-- PRIMARY KEY constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'users_pkey' 
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id);
    END IF;
END $$;

-- UNIQUE constraint cho email
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'users_email_key' 
        AND conrelid = 'users'::regclass
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_email_key UNIQUE (email);
    END IF;
END $$;

-- NOT NULL constraints
ALTER TABLE users 
    ALTER COLUMN id SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN password SET NOT NULL;

-- Bước 4: Cấp quyền cho user dbmaybay
GRANT ALL PRIVILEGES ON TABLE users TO dbmaybay;
GRANT ALL PRIVILEGES ON SEQUENCE IF EXISTS users_id_seq TO dbmaybay;

-- Bước 5: Kiểm tra kết quả
\echo '========================================'
\echo 'Schema bảng users:'
\echo '========================================'
\d users

\echo ''
\echo '========================================'
\echo 'Constraints:'
\echo '========================================'
SELECT
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_schema = 'public' 
AND tc.table_name = 'users'
ORDER BY tc.constraint_type, kcu.column_name;

\echo ''
\echo '✅ Bảng users đã được fix thành công!'

