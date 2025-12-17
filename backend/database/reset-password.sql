-- Script reset password cho user DBMaybay
-- Chạy script này với quyền superuser (postgres)

-- Reset password cho user DBMaybay
ALTER USER DBMaybay WITH PASSWORD '123456';

-- Kiểm tra user đã tồn tại chưa, nếu chưa thì tạo mới
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'DBMaybay') THEN
        CREATE USER DBMaybay WITH PASSWORD '123456';
        RAISE NOTICE 'User DBMaybay created with password 123456';
    ELSE
        RAISE NOTICE 'User DBMaybay password reset to 123456';
    END IF;
END
$$;

-- Đảm bảo user có quyền trên database flight_booking
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
ALTER DATABASE flight_booking OWNER TO DBMaybay;

-- Kết nối vào database và cấp quyền schema
\c flight_booking
GRANT ALL ON SCHEMA public TO DBMaybay;
ALTER SCHEMA public OWNER TO DBMaybay;

-- Thông báo
\echo ''
\echo '========================================'
\echo 'Password reset completed!'
\echo '========================================'
\echo 'User: DBMaybay'
\echo 'Password: 123456'
\echo ''

