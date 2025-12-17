# 🚀 QUICK FIX: Register API JDBC Commit Error

## ⚡ FIX NHANH (3 BƯỚC)

### Bước 1: Fix Database Schema
```powershell
cd D:\TMDT\WebMayBay\backend
psql -U dbmaybay -d flight_booking -f database/fix-users-table.sql
```

### Bước 2: Restart Backend
```powershell
# Dừng backend
.\stop-backend.ps1

# Start lại
.\keep-backend-running.ps1
```

### Bước 3: Test
```powershell
$body = @{
    email = "tung@gmail.com"
    password = "Khoangu060"
    phone = "0989948323"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" `
    -Method Post -Body $body -ContentType "application/json"
```

**Kết quả mong đợi:** Status 201 Created ✅

---

## 📋 CHANGES ĐÃ ÁP DỤNG

### 1. Entity User (`User.java`)
- ✅ Thêm `@Column(nullable = false)` cho `id`
- ✅ Thêm `@UniqueConstraint` ở class level
- ✅ Specify `length = 255` cho tất cả VARCHAR columns
- ✅ Set `nullable = true` cho `created_at` và `updated_at`

### 2. Exception Handler (`GlobalExceptionHandler.java`)
- ✅ Thêm handler cho `DataIntegrityViolationException`
- ✅ Thêm handler cho `TransactionSystemException`
- ✅ Thêm handler cho `PersistenceException`
- ✅ Thêm handler cho `DataAccessException`
- ✅ Extract và hiển thị lỗi database rõ ràng

### 3. Database Scripts
- ✅ `fix-users-table.sql` - Fix schema
- ✅ `check-users-table.sql` - Kiểm tra schema

---

## 🔍 NẾU VẪN LỖI

### Kiểm tra Database Schema:
```sql
\c flight_booking
\d users
```

**Phải có:**
- PRIMARY KEY trên `id`
- UNIQUE constraint trên `email`
- NOT NULL trên `id`, `email`, `password`

### Kiểm tra Backend Logs:
Tìm trong logs:
```
ERROR: null value in column "xxx"
ERROR: duplicate key value
ERROR: could not open JPA EntityManager
```

### Test Insert Thủ Công:
```sql
INSERT INTO users (id, email, password, phone, role, status, created_at, updated_at)
VALUES ('test-123', 'test@test.com', 'pass123', '123', 'USER', 'ACTIVE', NOW(), NOW());
```

---

## 📖 CHI TIẾT

Xem file: `FIX_REGISTER_JDBC_COMMIT_ERROR.md`

