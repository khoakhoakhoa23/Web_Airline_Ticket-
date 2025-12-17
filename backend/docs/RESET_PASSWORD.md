# Reset Password cho User DBMaybay

## Vấn đề
Backend không thể kết nối database vì password authentication failed cho user `DBMaybay`.

## Giải pháp

### Cách 1: Reset qua psql (Khuyến nghị)

1. **Mở PowerShell và kết nối PostgreSQL:**
   ```powershell
   psql -U postgres
   ```
   (Nhập password của user `postgres` khi được hỏi)

2. **Chạy script reset password:**
   ```sql
   -- Reset password
   ALTER USER DBMaybay WITH PASSWORD '123456';
   
   -- Đảm bảo quyền
   GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
   \c flight_booking
   GRANT ALL ON SCHEMA public TO DBMaybay;
   ALTER SCHEMA public OWNER TO DBMaybay;
   ```

3. **Hoặc chạy file script:**
   ```powershell
   psql -U postgres -f database/reset-password.sql
   ```

### Cách 2: Reset qua pgAdmin

1. Mở pgAdmin
2. Kết nối vào PostgreSQL server
3. Mở rộng: **Login/Group Roles** → Click phải vào `DBMaybay` → **Properties**
4. Tab **Definition** → Đổi password thành `123456`
5. Click **Save**

### Cách 3: Tạo lại user (nếu user không tồn tại)

```sql
-- Kết nối với postgres
psql -U postgres

-- Tạo user mới
CREATE USER DBMaybay WITH PASSWORD '123456';

-- Cấp quyền
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
ALTER DATABASE flight_booking OWNER TO DBMaybay;

-- Kết nối vào database
\c flight_booking

-- Cấp quyền schema
GRANT ALL ON SCHEMA public TO DBMaybay;
ALTER SCHEMA public OWNER TO DBMaybay;
```

## Kiểm tra

Sau khi reset, test kết nối:

```powershell
psql -U DBMaybay -d flight_booking
```

Nhập password: `123456`

Nếu kết nối thành công → Password đã được reset đúng.

## Sau khi reset

Chạy lại backend:
```powershell
cd D:\TMDT\WebMayBay\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

Backend sẽ tự động tạo các bảng trong database.

