-- Quick Setup Script - Chạy với user postgres
-- Copy và paste toàn bộ script này vào psql

-- Tạo database (bỏ qua nếu đã tồn tại)
SELECT 'CREATE DATABASE flight_booking'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'flight_booking')\gexec

-- Kết nối vào database
\c flight_booking

-- Tạo user (bỏ qua nếu đã tồn tại)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'DBMaybay') THEN
        CREATE USER DBMaybay WITH PASSWORD '123456';
        RAISE NOTICE 'User DBMaybay created';
    ELSE
        RAISE NOTICE 'User DBMaybay already exists';
    END IF;
END
$$;

-- Cấp quyền
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
ALTER DATABASE flight_booking OWNER TO DBMaybay;

-- Cấp quyền schema
GRANT ALL ON SCHEMA public TO DBMaybay;
ALTER SCHEMA public OWNER TO DBMaybay;

-- Thông báo thành công
\echo ''
\echo '========================================'
\echo 'Database setup completed successfully!'
\echo '========================================'
\echo 'Database: flight_booking'
\echo 'User: DBMaybay'
\echo 'Password: 123456'
\echo ''
\echo 'You can now start the backend application.'
\echo 'Tables will be created automatically by Hibernate.'
\echo ''

