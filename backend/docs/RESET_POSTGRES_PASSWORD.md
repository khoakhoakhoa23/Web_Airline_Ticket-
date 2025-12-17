# Cách Reset Password PostgreSQL

## Cách 1: Reset qua pgAdmin (Dễ nhất)

1. Mở pgAdmin
2. Kết nối vào PostgreSQL server (nếu đã lưu password)
3. Click phải vào user `postgres` → Properties
4. Tab "Definition" → Đổi password
5. Click Save

## Cách 2: Reset qua Command Line (Windows)

### Bước 1: Tìm file pg_hba.conf
Thường ở: `C:\Program Files\PostgreSQL\<version>\data\pg_hba.conf`

### Bước 2: Sửa file pg_hba.conf
Mở file với Notepad (cần quyền Admin), tìm dòng:
```
host    all             all             127.0.0.1/32            md5
```
Đổi thành:
```
host    all             all             127.0.0.1/32            trust
```

**Lưu ý**: Đây là tạm thời để reset password, sau đó nên đổi lại về `md5` hoặc `scram-sha-256`

### Bước 3: Restart PostgreSQL service
```powershell
Restart-Service postgresql-x64-<version>
```

### Bước 4: Reset password
```powershell
psql -U postgres
```
(Không cần password nữa vì đã set trust)

Trong psql:
```sql
ALTER USER postgres WITH PASSWORD 'your_new_password';
```

### Bước 5: Đổi lại pg_hba.conf về md5
Đổi lại dòng trong `pg_hba.conf`:
```
host    all             all             127.0.0.1/32            md5
```

### Bước 6: Restart lại PostgreSQL
```powershell
Restart-Service postgresql-x64-<version>
```

## Cách 3: Nếu quên hoàn toàn - Reset về mặc định

### Windows Service
1. Mở Services (services.msc)
2. Tìm PostgreSQL service
3. Stop service
4. Tạo file `pg_pass.txt` trong thư mục data với nội dung:
   ```
   postgres:your_new_password
   ```
5. Start lại service

### Hoặc dùng pg_ctl
```powershell
cd "C:\Program Files\PostgreSQL\<version>\bin"
.\pg_ctl.exe -D "C:\Program Files\PostgreSQL\<version>\data" -o "-p 5432" restart
```

## Cách 4: Tạo user mới với quyền superuser

Nếu không thể reset postgres, tạo user mới:

```sql
-- Kết nối với user khác có quyền (hoặc dùng cách 2 để vào không cần password)
CREATE USER admin_user WITH PASSWORD 'your_password' SUPERUSER;
```

Sau đó dùng user này để tạo database.

## Cách 5: Kiểm tra password đã lưu

### Trong pgAdmin
- File → Preferences → Servers
- Xem các server đã lưu

### Trong Windows Credential Manager
1. Mở "Credential Manager" (Windows + R → `control /name Microsoft.CredentialManager`)
2. Tìm các entry liên quan đến PostgreSQL

## Lưu ý quan trọng

⚠️ **Sau khi reset password, nhớ cập nhật:**
- Password trong `application.properties` (nếu dùng user postgres)
- Password trong các tool quản lý database (pgAdmin, DBeaver, etc.)

## Nếu không thể reset

Có thể tạo database với user khác hoặc cài lại PostgreSQL (lưu ý: sẽ mất dữ liệu).

