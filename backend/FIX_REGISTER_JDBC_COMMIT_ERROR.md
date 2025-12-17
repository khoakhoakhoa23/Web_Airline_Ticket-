# Fix: Register API - JDBC Commit Error

## 🔴 1. NGUYÊN NHÂN

### Root Cause #1: Database Schema Không Khớp Với Entity
- **Vấn đề:** Bảng `users` trong database có thể thiếu PRIMARY KEY constraint hoặc NOT NULL constraints không đúng
- **Triệu chứng:** Hibernate không thể commit transaction vì constraint violation
- **Lỗi cụ thể:** "Unable to commit against JDBC Connection"

### Root Cause #2: Entity User Thiếu Annotation Đầy Đủ
- **Vấn đề:** 
  - `@Id` field `id` không có `@Column(nullable = false)` rõ ràng
  - Thiếu `@UniqueConstraint` annotation ở class level
  - Column length không được specify
- **Impact:** Hibernate tạo schema không đúng hoặc không match với database thực tế

### Root Cause #3: Exception Handler Không Bắt Database Exceptions
- **Vấn đề:** `GlobalExceptionHandler` không handle `DataIntegrityViolationException`, `TransactionSystemException`, `PersistenceException`
- **Impact:** Lỗi database bị wrap thành generic exception, khó debug

---

## 🔍 2. CÁCH XÁC NHẬN

### Bước 1: Kiểm tra Database Schema
```sql
-- Chạy script này:
psql -U dbmaybay -d flight_booking -f database/check-users-table.sql

-- HOẶC chạy thủ công:
\c flight_booking
\d users

-- Kiểm tra constraints:
SELECT
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_schema = 'public' 
AND tc.table_name = 'users';
```

**Kết quả mong đợi:**
- Phải có PRIMARY KEY constraint trên `id`
- Phải có UNIQUE constraint trên `email`
- `id`, `email`, `password` phải có NOT NULL constraint

### Bước 2: Kiểm tra Backend Logs
Tìm trong logs:
```
ERROR: null value in column "xxx" violates not-null constraint
ERROR: duplicate key value violates unique constraint "users_email_key"
ERROR: could not open JPA EntityManager for transaction
```

### Bước 3: Test Insert Thủ Công
```sql
-- Test insert với dữ liệu giống frontend gửi:
INSERT INTO users (id, email, password, phone, role, status, created_at, updated_at)
VALUES (
    'test-uuid-123',
    'tung@gmail.com',
    'Khoangu060',
    '0989948323',
    'USER',
    'ACTIVE',
    NOW(),
    NOW()
);
```

**Nếu lỗi:** Xem error message để biết column nào vi phạm constraint.

---

## ✅ 3. CÁCH SỬA

### Fix #1: Sửa Database Schema

**Chạy script SQL:**
```powershell
cd D:\TMDT\WebMayBay\backend
psql -U dbmaybay -d flight_booking -f database/fix-users-table.sql
```

**HOẶC chạy thủ công:**
```sql
\c flight_booking

-- Đảm bảo PRIMARY KEY
ALTER TABLE users 
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

-- Đảm bảo UNIQUE constraint
ALTER TABLE users 
    ADD CONSTRAINT users_email_key UNIQUE (email);

-- Đảm bảo NOT NULL constraints
ALTER TABLE users 
    ALTER COLUMN id SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN password SET NOT NULL;

-- Cấp quyền
GRANT ALL PRIVILEGES ON TABLE users TO dbmaybay;
```

### Fix #2: Sửa Entity User

**File:** `backend/src/main/java/com/flightbooking/entity/User.java`

**Thay đổi:**
- Thêm `@Column(nullable = false)` cho `id`
- Thêm `@UniqueConstraint` ở class level
- Specify `length` cho tất cả VARCHAR columns
- Đảm bảo `created_at` và `updated_at` có `nullable = true`

### Fix #3: Cải Thiện Exception Handler

**File:** `backend/src/main/java/com/flightbooking/exception/GlobalExceptionHandler.java`

**Thêm handlers cho:**
- `DataIntegrityViolationException` - Constraint violations
- `TransactionSystemException` - Transaction errors
- `PersistenceException` - JPA errors
- `DataAccessException` - General database errors

---

## 📋 4. KẾT QUẢ MONG ĐỢI

### Sau khi fix:

1. **Database Schema:**
   ```sql
   \d users
   ```
   - ✅ PRIMARY KEY trên `id`
   - ✅ UNIQUE constraint trên `email`
   - ✅ NOT NULL trên `id`, `email`, `password`

2. **Backend Logs:**
   ```
   Hibernate: insert into users (created_at, email, password, phone, role, status, updated_at, id) values (?, ?, ?, ?, ?, ?, ?, ?)
   ```

3. **API Response:**
   ```json
   POST /api/users/register
   Status: 201 Created
   {
     "id": "550e8400-e29b-41d4-a716-446655440000",
     "email": "tung@gmail.com",
     "phone": "0989948323",
     "role": "USER",
     "status": "ACTIVE",
     "createdAt": "2025-12-16T14:30:00",
     "updatedAt": "2025-12-16T14:30:00"
   }
   ```

4. **Error Messages (nếu có lỗi):**
   - Email đã tồn tại: `409 Conflict - "Email already exists"`
   - Thiếu field: `400 Bad Request - "Required field is missing: email"`
   - Database error: `400 Bad Request - "Database transaction error. Check database constraints..."`

---

## 🚀 QUY TRÌNH FIX HOÀN CHỈNH

### Bước 1: Backup Database (Nếu cần)
```sql
pg_dump -U dbmaybay -d flight_booking > backup_before_fix.sql
```

### Bước 2: Fix Database Schema
```powershell
psql -U dbmaybay -d flight_booking -f database/fix-users-table.sql
```

### Bước 3: Restart Backend
```powershell
# Dừng backend hiện tại
.\stop-backend.ps1

# Start lại
.\keep-backend-running.ps1
```

### Bước 4: Test API
```powershell
$body = @{
    email = "tung@gmail.com"
    password = "Khoangu060"
    phone = "0989948323"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

**Kết quả:** Status 201 Created với user data.

---

## ⚠️ LƯU Ý

1. **Nếu vẫn lỗi sau khi fix:**
   - Kiểm tra PostgreSQL service đang chạy: `Get-Service -Name postgresql*`
   - Kiểm tra quyền user: `\du dbmaybay`
   - Xem backend logs chi tiết

2. **Nếu muốn reset hoàn toàn:**
   ```sql
   DROP TABLE IF EXISTS users CASCADE;
   -- Sau đó restart backend để Hibernate tạo lại table
   ```

3. **Nếu email đã tồn tại:**
   ```sql
   DELETE FROM users WHERE email = 'tung@gmail.com';
   ```

---

## 📝 CHECKLIST VERIFICATION

- [ ] Database schema có PRIMARY KEY trên `id`
- [ ] Database schema có UNIQUE constraint trên `email`
- [ ] Database schema có NOT NULL trên `id`, `email`, `password`
- [ ] Entity User có đầy đủ annotations
- [ ] Exception handler có handle database exceptions
- [ ] Backend restart thành công
- [ ] API register trả về 201 Created
- [ ] Test với email mới thành công
- [ ] Test với email đã tồn tại trả về 409 Conflict

