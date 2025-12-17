# Quick Start - Chạy toàn bộ hệ thống

## Bước 1: Chạy Backend

Mở PowerShell và chạy:

```powershell
cd D:\TMDT\WebMayBay\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

**Đợi đến khi thấy:**
```
Started FlightBookingApplication in X.XXX seconds
```

✅ Backend đang chạy tại: `http://localhost:8080`

## Bước 2: Chạy Frontend (Terminal mới)

Mở PowerShell mới và chạy:

```powershell
cd D:\TMDT\WebMayBay\frontend
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
npm run dev
```

✅ Frontend đang chạy tại: `http://localhost:5173`

## Bước 3: Test

1. Mở browser: `http://localhost:5173`
2. Thử đăng ký/đăng nhập
3. Kiểm tra Network tab (F12) để xem API calls

## Lưu ý quan trọng

- **KHÔNG đóng terminal** khi backend/frontend đang chạy
- Backend phải chạy trước khi test frontend
- Nếu thấy lỗi "Connection Refused" → Backend chưa chạy

## Troubleshooting

### Backend không chạy?
- Kiểm tra Java: `java -version`
- Kiểm tra PostgreSQL đang chạy
- Kiểm tra database đã được tạo
- Xem log trong console để tìm lỗi

### Frontend không chạy?
- Refresh PATH: `$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")`
- Kiểm tra Node.js: `node --version`
- Chạy lại: `npm run dev`

