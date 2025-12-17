-- Script test kết nối database
-- Chạy script này để kiểm tra kết nối

-- Kết nối vào database
\c flight_booking

-- Kiểm tra version PostgreSQL
SELECT version();

-- Kiểm tra database hiện tại
SELECT current_database();

-- Kiểm tra user hiện tại
SELECT current_user;

-- Liệt kê các bảng (sau khi Spring Boot tạo)
\dt

-- Kiểm tra số lượng bảng
SELECT COUNT(*) as table_count 
FROM information_schema.tables 
WHERE table_schema = 'public';

