# Quick Start - Chạy Backend

## Cách nhanh nhất (Khuyến nghị)

### Windows PowerShell:
```powershell
cd D:\TMDT\WebMayBay\backend
.\start-backend.ps1
```

## Các cách khác

### Cách 1: Dùng Maven Wrapper (nếu đã có maven-wrapper.jar)
```powershell
cd D:\TMDT\WebMayBay\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

### Cách 2: Dùng Maven (nếu đã cài Maven)
```powershell
cd D:\TMDT\WebMayBay\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
mvn spring-boot:run
```

### Cách 3: Chạy từ IDE
- **IntelliJ IDEA**: Mở `FlightBookingApplication.java` → Click phải → Run
- **Eclipse**: Click phải `FlightBookingApplication.java` → Run As → Java Application

## Yêu cầu trước khi chạy

1. ✅ **Java JDK 17** đã được cài đặt
2. ✅ **PostgreSQL** đang chạy
3. ✅ **Database `flight_booking`** đã được tạo
4. ✅ **JAVA_HOME** được set (script sẽ tự động set)

## Kiểm tra Backend đang chạy

Mở browser và truy cập:
```
http://localhost:8080/api/users
```

Nếu thấy response (có thể là `[]` hoặc danh sách users) → Backend đang chạy thành công! ✅

## Troubleshooting

### Lỗi: "JAVA_HOME not found"
**Giải pháp:**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

### Lỗi: "maven-wrapper.jar not found"
**Giải pháp:** Script `start-backend.ps1` sẽ tự động tải file này

### Lỗi: "Connection refused" hoặc database error
**Giải pháp:**
1. Kiểm tra PostgreSQL đang chạy
2. Kiểm tra database `flight_booking` đã được tạo
3. Kiểm tra thông tin trong `application.properties`

### Lỗi: Port 8080 đã được sử dụng
**Giải pháp:**
1. Tìm process đang dùng port 8080:
   ```powershell
   netstat -ano | findstr :8080
   ```
2. Dừng process đó hoặc đổi port trong `application.properties`

