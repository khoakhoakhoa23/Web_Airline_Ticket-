# Fix: Entity-Database Table Name Mismatch

## 🔴 1. NGUYÊN NHÂN

### Root Cause: Table Name Mismatch
- **Vấn đề:** Entity `User` map tới table `users` nhưng database có table `auth_user`
- **Triệu chứng:** 
  - "Unable to commit against JDBC Connection"
  - "Could not open JPA EntityManager for transaction"
  - HTTP 500 Internal Server Error
- **Lỗi cụ thể:** Hibernate không tìm thấy table `users` hoặc table `auth_user` có schema khác với Entity

### Phân Tích Chi Tiết:

**Entity User hiện tại:**
```java
@Table(name = "users")  // ❌ SAI - Database có table "auth_user"
```

**Database thực tế:**
- Table name: `auth_user` (không phải `users`)
- Owner: `dbmaybay`
- Schema: `public`

**Kết quả:**
- Hibernate cố gắng INSERT vào table `users` → Table không tồn tại hoặc schema không khớp
- Transaction rollback → "Unable to commit against JDBC Connection"

---

## 🔍 2. CÁCH XÁC NHẬN

### Bước 1: Kiểm tra Database Schema
```powershell
psql -U dbmaybay -d flight_booking -f database/check-auth-user-table.sql
```

**HOẶC chạy thủ công:**
```sql
\c flight_booking

-- Kiểm tra table nào tồn tại
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('users', 'auth_user');

-- Xem schema của auth_user
\d auth_user

-- Xem schema của users (nếu có)
\d users
```

**Kết quả mong đợi:**
- Table `auth_user` tồn tại
- Table `users` có thể không tồn tại hoặc có schema khác

### Bước 2: Kiểm tra Backend Logs
Tìm trong logs:
```
ERROR: relation "users" does not exist
ERROR: relation "auth_user" does not exist
ERROR: could not open JPA EntityManager for transaction
```

### Bước 3: So Sánh Entity và Database

**Entity User fields:**
- `id` (String, @Id, nullable = false)
- `email` (String, nullable = false, unique = true)
- `password` (String, nullable = false)
- `phone` (String, nullable = true)
- `role` (String, nullable = true)
- `status` (String, nullable = true)
- `createdAt` (LocalDateTime, nullable = true)
- `updatedAt` (LocalDateTime, nullable = true)

**Database auth_user columns (cần kiểm tra):**
```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
AND table_name = 'auth_user'
ORDER BY ordinal_position;
```

---

## ✅ 3. CÁCH SỬA

### Fix #1: Sửa Entity User (ƯU TIÊN)

**File:** `backend/src/main/java/com/flightbooking/entity/User.java`

**Thay đổi:**
```java
// TRƯỚC:
@Table(name = "users", 
       uniqueConstraints = @UniqueConstraint(name = "users_email_key", columnNames = "email"))

// SAU:
@Table(name = "auth_user", 
       uniqueConstraints = @UniqueConstraint(name = "auth_user_email_key", columnNames = "email"))
```

**✅ Đã áp dụng trong code**

### Fix #2: Đảm Bảo Database Schema Đúng

**Chạy script SQL:**
```powershell
psql -U dbmaybay -d flight_booking -f database/fix-auth-user-table.sql
```

**Script sẽ:**
- Tạo table `auth_user` nếu chưa có
- Đảm bảo PRIMARY KEY trên `id`
- Đảm bảo UNIQUE constraint trên `email`
- Set NOT NULL cho `id`, `email`, `password`
- Cấp quyền cho user `dbmaybay`

### Fix #3: Cấu Hình Hibernate DDL Auto

**File:** `backend/src/main/resources/application.properties`

**Khuyến nghị cho DEV:**
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Giải thích:**
- `update`: Hibernate sẽ UPDATE schema để match với Entity (thêm columns, constraints)
- **KHÔNG** xóa columns hoặc data hiện có
- Phù hợp cho môi trường DEV

**Các options khác:**
- `create`: Xóa và tạo lại table mỗi lần start (MẤT DATA)
- `create-drop`: Tạo khi start, xóa khi stop (MẤT DATA)
- `validate`: Chỉ kiểm tra, không thay đổi (PRODUCTION)
- `none`: Không làm gì (PRODUCTION)

---

## 📋 4. SO SÁNH ENTITY VÀ DATABASE

### Entity User (Sau khi fix):

```java
@Entity
@Table(name = "auth_user", 
       uniqueConstraints = @UniqueConstraint(name = "auth_user_email_key", columnNames = "email"))
public class User {
    @Id
    @Column(nullable = false, length = 255)
    private String id;                    // PRIMARY KEY
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;                 // NOT NULL, UNIQUE
    
    @Column(nullable = false, length = 255)
    private String password;              // NOT NULL
    
    @Column(length = 255)
    private String phone;                 // NULLABLE
    
    @Column(length = 255)
    private String role;                  // NULLABLE
    
    @Column(length = 255)
    private String status;                // NULLABLE
    
    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;     // NULLABLE
    
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;      // NULLABLE
}
```

### Database Schema (Sau khi fix):

```sql
CREATE TABLE auth_user (
    id VARCHAR(255) NOT NULL,                    -- PRIMARY KEY
    email VARCHAR(255) NOT NULL,                 -- UNIQUE
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    role VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT auth_user_pkey PRIMARY KEY (id),
    CONSTRAINT auth_user_email_key UNIQUE (email)
);
```

**✅ Match hoàn toàn**

---

## 🚀 5. QUY TRÌNH FIX HOÀN CHỈNH

### Bước 1: Fix Entity User
```java
// Đã sửa: @Table(name = "auth_user")
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

### Bước 4: Test API
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

## 📝 6. KẾT QUẢ MONG ĐỢI

### Sau khi fix:

1. **Entity map đúng table:**
   - Entity `User` → Table `auth_user` ✅

2. **Database Schema:**
   ```sql
   \d auth_user
   ```
   - ✅ PRIMARY KEY trên `id`
   - ✅ UNIQUE constraint trên `email`
   - ✅ NOT NULL trên `id`, `email`, `password`

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

4. **Backend Logs:**
   ```
   Hibernate: insert into auth_user (created_at, email, password, phone, role, status, updated_at, id) values (?, ?, ?, ?, ?, ?, ?, ?)
   ```

---

## ⚠️ LƯU Ý

1. **Nếu table `users` đã có data:**
   - Có thể migrate data từ `users` sang `auth_user`
   - Hoặc rename table: `ALTER TABLE users RENAME TO auth_user;`

2. **Nếu muốn giữ table `users`:**
   - Không sửa Entity, giữ `@Table(name = "users")`
   - Đảm bảo table `users` có đúng schema

3. **Hibernate DDL Auto:**
   - DEV: `spring.jpa.hibernate.ddl-auto=update` ✅
   - PROD: `spring.jpa.hibernate.ddl-auto=validate` hoặc `none`

---

## 🔍 CHECKLIST VERIFICATION

- [ ] Entity User map tới table `auth_user`
- [ ] Database có table `auth_user`
- [ ] Table `auth_user` có PRIMARY KEY trên `id`
- [ ] Table `auth_user` có UNIQUE constraint trên `email`
- [ ] Table `auth_user` có NOT NULL trên `id`, `email`, `password`
- [ ] User `dbmaybay` có quyền trên table `auth_user`
- [ ] Backend restart thành công
- [ ] API register trả về 201 Created
- [ ] Test với email mới thành công
- [ ] Test với email đã tồn tại trả về 409 Conflict

---

## 📖 FILES ĐÃ TẠO/SỬA

1. ✅ `backend/src/main/java/com/flightbooking/entity/User.java` - Sửa table name
2. ✅ `backend/database/fix-auth-user-table.sql` - Script fix database
3. ✅ `backend/database/check-auth-user-table.sql` - Script kiểm tra schema
4. ✅ `backend/FIX_AUTH_USER_TABLE_MISMATCH.md` - Tài liệu này

