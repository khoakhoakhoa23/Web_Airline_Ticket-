# 🚀 QUICK FIX: Entity-Database Table Mismatch

## ⚡ VẤN ĐỀ

- Entity `User` map tới table `users` 
- Database có table `auth_user`
- → Lỗi: "Unable to commit against JDBC Connection"

## ✅ FIX NHANH (3 BƯỚC)

### Bước 1: Entity đã được sửa ✅
```java
@Table(name = "auth_user")  // Đã sửa từ "users"
```

### Bước 2: Fix Database Schema
```powershell
cd D:\TMDT\WebMayBay\backend
psql -U dbmaybay -d flight_booking -f database/fix-auth-user-table.sql
```

### Bước 3: Restart Backend
```powershell
.\stop-backend.ps1
.\keep-backend-running.ps1
```

### Bước 4: Test
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

**Kết quả:** Status 201 Created ✅

---

## 📋 CHANGES ĐÃ ÁP DỤNG

1. ✅ **Entity User** - Đổi table name từ `users` → `auth_user`
2. ✅ **Database Script** - Tạo script fix schema cho `auth_user`
3. ✅ **Check Script** - Tạo script kiểm tra schema

---

## 🔍 NẾU VẪN LỖI

### Kiểm tra Database:
```sql
\c flight_booking
\d auth_user
```

**Phải có:**
- PRIMARY KEY trên `id`
- UNIQUE constraint trên `email`
- NOT NULL trên `id`, `email`, `password`

### Kiểm tra Entity:
```java
@Table(name = "auth_user")  // Phải là "auth_user"
```

---

## 📖 CHI TIẾT

Xem: `FIX_AUTH_USER_TABLE_MISMATCH.md`

