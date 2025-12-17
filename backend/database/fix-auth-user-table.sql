-- Script FIX database schema cho bảng auth_user
-- Chạy: psql -U dbmaybay -d flight_booking -f fix-auth-user-table.sql

\c flight_booking

-- Bước 1: Kiểm tra bảng auth_user có tồn tại không
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'auth_user'
) AS auth_user_exists;

-- Bước 2: Xem schema hiện tại của auth_user (nếu có)
\echo '========================================'
\echo 'Schema hiện tại của auth_user:'
\echo '========================================'
\d auth_user

-- Bước 3: Tạo hoặc sửa bảng auth_user để match với Entity
-- Option A: Nếu bảng chưa tồn tại, tạo mới
CREATE TABLE IF NOT EXISTS auth_user (
    id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    role VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT auth_user_pkey PRIMARY KEY (id),
    CONSTRAINT auth_user_email_key UNIQUE (email)
);

-- Bước 4: Đảm bảo tất cả constraints đúng
-- PRIMARY KEY constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'auth_user_pkey' 
        AND conrelid = 'auth_user'::regclass
    ) THEN
        ALTER TABLE auth_user ADD CONSTRAINT auth_user_pkey PRIMARY KEY (id);
    END IF;
END $$;

-- UNIQUE constraint cho email
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'auth_user_email_key' 
        AND conrelid = 'auth_user'::regclass
    ) THEN
        ALTER TABLE auth_user ADD CONSTRAINT auth_user_email_key UNIQUE (email);
    END IF;
END $$;

-- NOT NULL constraints
ALTER TABLE auth_user 
    ALTER COLUMN id SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN password SET NOT NULL;

-- Bước 5: Cấp quyền cho user dbmaybay
GRANT ALL PRIVILEGES ON TABLE auth_user TO dbmaybay;
GRANT ALL PRIVILEGES ON SEQUENCE IF EXISTS auth_user_id_seq TO dbmaybay;

-- Bước 6: Kiểm tra kết quả
\echo ''
\echo '========================================'
\echo 'Schema bảng auth_user sau khi fix:'
\echo '========================================'
\d auth_user

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
AND tc.table_name = 'auth_user'
ORDER BY tc.constraint_type, kcu.column_name;

\echo ''
\echo '✅ Bảng auth_user đã được fix thành công!'

