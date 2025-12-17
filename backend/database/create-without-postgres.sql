-- Script tạo database KHÔNG cần user postgres
-- Chạy với bất kỳ user nào có quyền tạo database

-- Tạo database
CREATE DATABASE flight_booking;

-- Kết nối vào database
\c flight_booking

-- Tạo user DBMaybay
CREATE USER DBMaybay WITH PASSWORD '123456';

-- Cấp quyền
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
ALTER DATABASE flight_booking OWNER TO DBMaybay;

-- Cấp quyền schema
GRANT ALL ON SCHEMA public TO DBMaybay;
ALTER SCHEMA public OWNER TO DBMaybay;

-- Thông báo
\echo 'Database flight_booking created successfully!'
\echo 'User: DBMaybay'
\echo 'Password: 123456'

