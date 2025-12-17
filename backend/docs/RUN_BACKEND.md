# Hướng dẫn chạy Backend trên Terminal

## Bước 1: Mở PowerShell và vào thư mục backend

```powershell
cd D:\TMDT\WebMayBay\backend
```

## Bước 2: Set JAVA_HOME (quan trọng!)

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

**Lưu ý:** Nếu Java của bạn ở đường dẫn khác, thay đổi cho đúng.

## Bước 3: Kiểm tra Java

```powershell
java -version
```

Kết quả mong đợi: `openjdk version "17.0.17"` hoặc tương tự

## Bước 4: Chạy Backend

### Cách 1: Dùng Maven Wrapper (Khuyến nghị)
```powershell
.\mvnw.cmd spring-boot:run
```

### Cách 2: Nếu có Maven đã cài
```powershell
mvn spring-boot:run
```

## Lệnh đầy đủ (Copy & Paste)

```powershell
cd D:\TMDT\WebMayBay\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

## Sau khi chạy

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

Backend sẽ chạy tại: **http://localhost:8080**

## Dừng Backend

Nhấn `Ctrl + C` trong terminal

## Troubleshooting

### Lỗi: "JAVA_HOME not found"
```powershell
# Kiểm tra Java có ở đâu
Get-ChildItem "C:\Program Files\Java" | Select-Object Name

# Set JAVA_HOME cho đúng đường dẫn
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"  # Thay đổi nếu cần
```

### Lỗi: "maven-wrapper.jar not found"
```powershell
# Tải maven-wrapper.jar
New-Item -ItemType Directory -Path ".\.mvn\wrapper" -Force
Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar" -OutFile ".\.mvn\wrapper\maven-wrapper.jar"
```

### Lỗi: "Port 8080 already in use"
```powershell
# Tìm process đang dùng port 8080
netstat -ano | findstr :8080

# Dừng process (thay PID bằng số từ lệnh trên)
taskkill /PID <PID> /F
```

### Lỗi: Database connection
- Đảm bảo PostgreSQL đang chạy
- Kiểm tra database `flight_booking` đã được tạo
- Kiểm tra username/password trong `application.properties`

