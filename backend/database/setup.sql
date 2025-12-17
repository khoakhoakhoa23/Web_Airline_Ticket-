-- Script tạo database và user cho Flight Booking System
-- Chạy script này với quyền superuser (postgres)

-- Tạo database
CREATE DATABASE flight_booking
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Kết nối vào database flight_booking
\c flight_booking

-- Tạo user (nếu chưa có)
-- Nếu user đã tồn tại, bỏ qua bước này
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'DBMaybay') THEN
        CREATE USER DBMaybay WITH PASSWORD '123456';
    END IF;
END
$$;

-- Cấp quyền cho user
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
ALTER DATABASE flight_booking OWNER TO DBMaybay;

-- Cấp quyền schema public
GRANT ALL ON SCHEMA public TO DBMaybay;
ALTER SCHEMA public OWNER TO DBMaybay;

-- Thông báo
\echo 'Database flight_booking đã được tạo thành công!'
\echo 'User: DBMaybay'
\echo 'Password: 123456'

