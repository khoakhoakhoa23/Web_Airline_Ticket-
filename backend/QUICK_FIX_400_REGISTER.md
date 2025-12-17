# 🚀 QUICK FIX: 400 Bad Request - Register API

## ⚡ VẤN ĐỀ

- Frontend gọi `POST /api/users/register` → 400 Bad Request
- Payload đúng: `{email, password, phone, role}`
- DTO validation pass

## ✅ FIX NHANH

### Bước 1: Kiểm tra Email Đã Tồn Tại
```sql
SELECT * FROM auth_user WHERE email = 'tung@gmail.com';
-- Nếu có, xóa để test lại:
DELETE FROM auth_user WHERE email = 'tung@gmail.com';
```

### Bước 2: Restart Backend
```powershell
.\stop-backend.ps1
.\keep-backend-running.ps1
```

### Bước 3: Test API
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

## 🔍 DEBUG

### Xem Backend Logs
- Tìm: `Validation errors:`, `DataIntegrityViolationException:`
- Xem exception message cụ thể

### Test Trực Tiếp
```powershell
# Xem response chi tiết
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/users/register" `
    -Method Post -Body $body -ContentType "application/json" `
    -ErrorAction SilentlyContinue

$response.StatusCode
$response.Content
```

## 📋 CÁC TRƯỜNG HỢP

### 1. Email Đã Tồn Tại
**Response:** `409 Conflict`
```json
{
  "message": "Email already exists",
  "status": "ERROR"
}
```

### 2. Validation Error
**Response:** `400 Bad Request`
```json
{
  "email": "Invalid email format",
  "password": "Password must be at least 6 characters",
  "status": "VALIDATION_ERROR"
}
```

### 3. Database Error
**Response:** `400 Bad Request`
```json
{
  "message": "Database constraint violation: ...",
  "status": "ERROR"
}
```

## 📖 CHI TIẾT

Xem: `FIX_400_BAD_REQUEST_REGISTER.md`

