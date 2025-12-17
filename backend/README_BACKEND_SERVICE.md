# Backend Service - Đảm Bảo Backend Luôn Chạy

## 📋 Tổng Quan

Các script này giúp đảm bảo backend Spring Boot luôn chạy và tự động khởi động lại khi gặp sự cố.

## 🚀 Các Script Có Sẵn

### 1. `keep-backend-running.ps1` ⭐ (Khuyên dùng)
**Script chính để giữ backend luôn chạy**

**Tính năng:**
- ✅ Tự động restart khi backend crash
- ✅ Health check định kỳ
- ✅ Logging chi tiết vào `backend-run.log`
- ✅ Kiểm tra dependencies (Java, PostgreSQL)
- ✅ Giới hạn số lần restart (tránh loop vô hạn)

**Cách dùng:**
```powershell
cd D:\TMDT\WebMayBay\backend
.\keep-backend-running.ps1
```

**Dừng:**
- Nhấn `Ctrl+C` trong terminal

---

### 2. `start-backend-service.ps1`
**Tạo Windows Scheduled Task để chạy backend tự động**

**Tính năng:**
- ✅ Tự động start khi Windows boot
- ✅ Tự động start khi user login
- ✅ Chạy background (không cần mở terminal)
- ✅ Tự động restart khi crash (tối đa 3 lần)

**Cách dùng:**
```powershell
cd D:\TMDT\WebMayBay\backend
.\start-backend-service.ps1
```

**Quản lý Task:**
```powershell
# Start
Start-ScheduledTask -TaskName "FlightBookingBackend"

# Stop
Stop-ScheduledTask -TaskName "FlightBookingBackend"

# Xem status
Get-ScheduledTask -TaskName "FlightBookingBackend"

# Xóa task
Unregister-ScheduledTask -TaskName "FlightBookingBackend" -Confirm:$false
```

---

### 3. `stop-backend.ps1`
**Dừng backend đang chạy**

**Cách dùng:**
```powershell
.\stop-backend.ps1
```

---

### 4. `start-backend.ps1` (Script cũ)
**Chạy backend một lần (không auto-restart)**

**Cách dùng:**
```powershell
.\start-backend.ps1
```

---

## 📊 So Sánh Các Phương Pháp

| Phương Pháp | Auto-Restart | Background | Boot Auto | Logging |
|------------|--------------|------------|-----------|---------|
| `keep-backend-running.ps1` | ✅ | ❌ | ❌ | ✅ |
| `start-backend-service.ps1` | ✅ | ✅ | ✅ | ✅ |
| `start-backend.ps1` | ❌ | ❌ | ❌ | ❌ |

---

## 🎯 Khuyến Nghị

### Cho Development:
```powershell
.\keep-backend-running.ps1
```
- Dễ debug (xem logs trực tiếp)
- Dễ dừng (Ctrl+C)
- Auto-restart khi crash

### Cho Production/Testing:
```powershell
.\start-backend-service.ps1
```
- Chạy background
- Tự động start khi boot
- Không cần mở terminal

---

## 📝 Log Files

- **`backend-run.log`**: Log chi tiết của backend
- **`backend.pid`**: File chứa Process ID (tự động tạo/xóa)

---

## 🔍 Kiểm Tra Backend Đang Chạy

### Kiểm tra bằng PowerShell:
```powershell
# Test API
Invoke-RestMethod -Uri "http://localhost:8080/api/users"

# Kiểm tra process
Get-Process -Name "java" | Where-Object { $_.CommandLine -like "*FlightBookingApplication*" }

# Kiểm tra port
netstat -ano | findstr :8080
```

### Kiểm tra bằng Browser:
```
http://localhost:8080/api/users
```

---

## ⚠️ Troubleshooting

### Backend không start:
1. Kiểm tra Java: `java -version`
2. Kiểm tra PostgreSQL: `Get-Service -Name postgresql*`
3. Kiểm tra database: `psql -U dbmaybay -d flight_booking`
4. Xem logs: `Get-Content backend-run.log -Tail 50`

### Backend restart liên tục:
1. Xem logs để tìm lỗi: `Get-Content backend-run.log -Tail 100`
2. Kiểm tra database connection
3. Kiểm tra port 8080 có bị chiếm không: `netstat -ano | findstr :8080`

### Dừng backend không được:
```powershell
# Force kill tất cả Java processes
Get-Process -Name "java" | Stop-Process -Force

# Hoặc dùng script
.\stop-backend.ps1
```

---

## 🔧 Cấu Hình

### Thay đổi số lần restart tối đa:
Mở `keep-backend-running.ps1`, tìm dòng:
```powershell
$script:maxRestarts = 100  # Thay đổi số này
```

### Thay đổi delay trước khi restart:
```powershell
$script:restartDelay = 5   # Thay đổi số giây
```

---

## 📞 Support

Nếu gặp vấn đề:
1. Xem file `backend-run.log`
2. Kiểm tra database connection
3. Kiểm tra Java và PostgreSQL đã cài đặt chưa

