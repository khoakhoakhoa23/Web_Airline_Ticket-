# Hướng dẫn chạy Backend - Giải quyết lỗi Connection Refused

## Lỗi: ERR_CONNECTION_REFUSED

Lỗi này có nghĩa là **backend chưa chạy** hoặc đã dừng.

## Giải pháp: Chạy Backend

### Bước 1: Mở PowerShell mới

Mở PowerShell (có thể cần quyền Admin) và chạy:

```powershell
# Vào thư mục backend
cd D:\TMDT\WebMayBay\backend

# Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Chạy backend
.\mvnw.cmd spring-boot:run
```

### Bước 2: Đợi backend khởi động

Bạn sẽ thấy output như:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

...
Started FlightBookingApplication in X.XXX seconds
```

### Bước 3: Kiểm tra backend đang chạy

Khi thấy dòng `Started FlightBookingApplication` → Backend đã chạy thành công! ✅

### Bước 4: Test lại

Mở browser và truy cập:
```
http://localhost:8080/api/users
```

Nếu thấy response (có thể là `[]` hoặc danh sách users) → Thành công! ✅

## Lưu ý

- **KHÔNG đóng terminal** khi backend đang chạy
- Backend sẽ chạy cho đến khi bạn nhấn `Ctrl + C`
- Nếu đóng terminal → Backend sẽ dừng → Lỗi connection refused

## Troubleshooting

### Nếu vẫn lỗi sau khi chạy backend:

1. **Kiểm tra port 8080 có bị chiếm không:**
   ```powershell
   netstat -ano | findstr :8080
   ```
   Nếu có process khác đang dùng port 8080, dừng nó hoặc đổi port trong `application.properties`

2. **Kiểm tra log backend:**
   - Xem có lỗi gì trong console không
   - Thường sẽ hiển thị lỗi cụ thể (database connection, etc.)

3. **Kiểm tra database:**
   - Đảm bảo PostgreSQL đang chạy
   - Đảm bảo database `flight_booking` đã được tạo

4. **Kiểm tra firewall:**
   - Windows Firewall có thể chặn port 8080
   - Tạm thời tắt firewall để test

## Chạy Backend và Frontend cùng lúc

Bạn cần **2 terminal windows**:

**Terminal 1 - Backend:**
```powershell
cd D:\TMDT\WebMayBay\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

**Terminal 2 - Frontend:**
```powershell
cd D:\TMDT\WebMayBay\frontend
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
npm run dev
```

## Quick Start Script

Tạo file `start-all.ps1` trong thư mục gốc để chạy cả 2:

```powershell
# Start Backend
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd D:\TMDT\WebMayBay\backend; `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\mvnw.cmd spring-boot:run"

# Wait a bit
Start-Sleep -Seconds 3

# Start Frontend  
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd D:\TMDT\WebMayBay\frontend; `$env:Path = [System.Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [System.Environment]::GetEnvironmentVariable('Path','User'); npm run dev"
```

Chạy: `.\start-all.ps1`

