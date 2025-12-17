# Bước tiếp theo - Database đã sẵn sàng! ✅

## ✅ Đã hoàn thành

- ✓ Database `flight_booking` đã được tạo
- ✓ User `DBMaybay` đã được tạo với password `123456`
- ✓ Quyền đã được cấp đầy đủ
- ✓ Ownership đã được set

## Bước tiếp theo: Chạy Backend

### 1. Chạy Backend
```powershell
cd D:\TMDT\WebMayBay\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

### 2. Kiểm tra log

Khi backend khởi động, bạn sẽ thấy:

**✅ Kết nối thành công:**
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

**✅ Tạo bảng tự động:**
```
Hibernate: create table users ...
Hibernate: create table bookings ...
Hibernate: create table flight_segments ...
...
```

### 3. Kiểm tra database có bảng chưa

Trong psql:
```sql
\c flight_booking
\dt  -- Liệt kê các bảng
```

Bạn sẽ thấy các bảng như:
- `users`
- `bookings`
- `flight_segments`
- `passengers`
- `payments`
- etc.

## Test kết nối từ Backend

### Test 1: Kiểm tra API
Mở browser: `http://localhost:8080/api/users`

Nếu thấy response (có thể là `[]`) → Backend đã kết nối database thành công! ✅

### Test 2: Tạo user mới
1. Mở Frontend: `http://localhost:5173/register`
2. Điền form và đăng ký
3. Kiểm tra database:
   ```sql
   \c flight_booking
   SELECT * FROM users;
   ```

Nếu thấy user mới → Mọi thứ hoạt động hoàn hảo! 🎉

## Troubleshooting

### Nếu backend không kết nối được database:

1. **Kiểm tra PostgreSQL đang chạy:**
   ```powershell
   Get-Service -Name postgresql*
   ```

2. **Test kết nối thủ công:**
   ```powershell
   psql -U DBMaybay -d flight_booking
   # Password: 123456
   ```

3. **Kiểm tra log backend:**
   - Tìm lỗi connection trong console
   - Thường sẽ hiển thị lỗi cụ thể

4. **Kiểm tra application.properties:**
   - URL, username, password có đúng không

## Checklist

- [x] Database `flight_booking` đã được tạo
- [x] User `DBMaybay` đã được tạo
- [x] Quyền đã được cấp
- [ ] Backend đã chạy thành công
- [ ] Bảng đã được tạo tự động
- [ ] API đã hoạt động
- [ ] Frontend có thể kết nối Backend

## Lưu ý

- Backend sẽ tự động tạo bảng khi chạy lần đầu (nhờ `spring.jpa.hibernate.ddl-auto=update`)
- Không cần chạy migration scripts thủ công
- Mọi thay đổi Entity sẽ tự động cập nhật schema

Bạn có thể chạy backend ngay bây giờ! 🚀

