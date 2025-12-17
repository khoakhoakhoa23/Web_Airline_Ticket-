# Hướng dẫn Reset Password trong pgAdmin4

## Bước 1: Mở pgAdmin4 và kết nối PostgreSQL

1. Mở pgAdmin4
2. Kết nối vào PostgreSQL server (click vào server trong danh sách bên trái)
   - Nếu chưa lưu password, nhập password của user `postgres`

## Bước 2: Reset Password cho user DBMaybay

1. Trong cây thư mục bên trái, mở rộng:
   - **Servers** → **PostgreSQL (version)** → **Login/Group Roles**

2. Click phải vào user **DBMaybay** → Chọn **Properties**

3. Trong cửa sổ Properties:
   - Tab **Definition**
   - Tìm ô **Password**
   - Nhập password mới: `123456`
   - Click **Save**

## Bước 3: Kiểm tra Database và Quyền

1. Mở rộng: **Databases** → **flight_booking**

2. Click phải vào **flight_booking** → **Properties**

3. Tab **Security**:
   - Đảm bảo user **DBMaybay** có quyền **ALL PRIVILEGES**
   - Nếu chưa có, click **+** để thêm và chọn **DBMaybay** với quyền **ALL**

4. Tab **Owner**:
   - Đảm bảo Owner là **DBMaybay**
   - Nếu không, chọn **DBMaybay** từ dropdown

## Bước 4: Test Kết nối

1. Trong pgAdmin4, click phải vào **flight_booking** → **Query Tool**

2. Chạy query:
   ```sql
   SELECT current_user, current_database();
   ```

3. Nếu hiển thị:
   ```
   current_user | current_database
   DBMaybay     | flight_booking
   ```
   → Thành công!

## Bước 5: Chạy Backend

Sau khi reset password xong, chạy backend:

```powershell
cd D:\TMDT\WebMayBay\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

Backend sẽ tự động tạo các bảng trong database.

## Nếu user DBMaybay chưa tồn tại

1. Click phải vào **Login/Group Roles** → **Create** → **Login/Group Role**

2. Tab **General**:
   - **Name**: `DBMaybay`

3. Tab **Definition**:
   - **Password**: `123456`

4. Tab **Privileges**:
   - Bật tất cả các quyền (Can login, Create databases, etc.)

5. Click **Save**

6. Sau đó làm lại Bước 3 để cấp quyền cho database.

