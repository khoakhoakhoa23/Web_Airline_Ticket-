# Hướng dẫn Setup Database PostgreSQL

## Yêu cầu

- PostgreSQL đã được cài đặt và đang chạy
- Quyền truy cập với user `postgres` (superuser)

## Cấu hình Database

- **Database Name**: `flight_booking`
- **Username**: `DBMaybay`
- **Password**: `123456`
- **Host**: `localhost`
- **Port**: `5432`

## Cách 1: Tạo Database bằng psql (Khuyến nghị)

### Bước 1: Mở psql
```powershell
# Vào thư mục PostgreSQL bin (thường là)
cd "C:\Program Files\PostgreSQL\<version>\bin"

# Hoặc nếu đã có trong PATH
psql -U postgres
```

### Bước 2: Chạy script SQL
```sql
-- Tạo database
CREATE DATABASE flight_booking;

-- Kết nối vào database
\c flight_booking

-- Tạo user (nếu chưa có)
CREATE USER DBMaybay WITH PASSWORD '123456';

-- Cấp quyền
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
ALTER DATABASE flight_booking OWNER TO DBMaybay;
```

### Bước 3: Thoát
```sql
\q
```

## Cách 2: Chạy script SQL file

```powershell
# Vào thư mục backend
cd D:\TMDT\WebMayBay\backend

# Chạy script (thay <version> bằng version PostgreSQL của bạn)
& "C:\Program Files\PostgreSQL\<version>\bin\psql.exe" -U postgres -f database\setup.sql
```

## Cách 3: Tạo bằng pgAdmin

1. Mở pgAdmin
2. Kết nối vào PostgreSQL server
3. Click phải vào "Databases" → Create → Database
4. Tên database: `flight_booking`
5. Owner: `postgres` (hoặc `DBMaybay` nếu đã tạo user)
6. Click Save

## Kiểm tra kết nối

### Test 1: Kiểm tra PostgreSQL đang chạy
```powershell
# Kiểm tra service
Get-Service -Name postgresql*

# Hoặc kiểm tra port
netstat -ano | findstr :5432
```

### Test 2: Test kết nối từ psql
```powershell
psql -U DBMaybay -d flight_booking -h localhost
# Nhập password: 123456
```

Nếu kết nối thành công, bạn sẽ thấy prompt:
```
flight_booking=>
```

### Test 3: Test từ Backend
1. Chạy backend:
   ```powershell
   cd D:\TMDT\WebMayBay\backend
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
   .\mvnw.cmd spring-boot:run
   ```

2. Kiểm tra log:
   - Nếu thấy: `HikariPool-1 - Starting...` và `HikariPool-1 - Start completed` → Kết nối thành công ✅
   - Nếu thấy lỗi connection → Kiểm tra lại cấu hình

## Troubleshooting

### Lỗi: "FATAL: password authentication failed"
**Nguyên nhân**: Sai username hoặc password

**Giải pháp**:
1. Kiểm tra username/password trong `application.properties`
2. Hoặc reset password:
   ```sql
   ALTER USER DBMaybay WITH PASSWORD '123456';
   ```

### Lỗi: "FATAL: database 'flight_booking' does not exist"
**Nguyên nhân**: Database chưa được tạo

**Giải pháp**:
```sql
CREATE DATABASE flight_booking;
```

### Lỗi: "Connection refused" hoặc "Connection timed out"
**Nguyên nhân**: PostgreSQL chưa chạy hoặc port sai

**Giải pháp**:
1. Kiểm tra PostgreSQL service:
   ```powershell
   Get-Service -Name postgresql*
   ```
2. Start service nếu chưa chạy:
   ```powershell
   Start-Service postgresql-x64-<version>
   ```
3. Kiểm tra port trong `postgresql.conf` (thường là 5432)

### Lỗi: "permission denied for schema public"
**Nguyên nhân**: User chưa có quyền

**Giải pháp**:
```sql
\c flight_booking
GRANT ALL ON SCHEMA public TO DBMaybay;
ALTER SCHEMA public OWNER TO DBMaybay;
```

## Cấu hình trong application.properties

File: `backend/src/main/resources/application.properties`

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/flight_booking
spring.datasource.username=DBMaybay
spring.datasource.password=123456
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Lưu ý**: 
- `spring.jpa.hibernate.ddl-auto=update` → Tự động tạo/cập nhật bảng khi chạy
- `spring.jpa.show-sql=true` → Hiển thị SQL queries trong console (để debug)

## Sau khi kết nối thành công

Khi backend chạy, Hibernate sẽ tự động:
1. Tạo các bảng dựa trên Entity classes
2. Tạo các indexes và constraints
3. Cập nhật schema nếu có thay đổi

Bạn có thể kiểm tra bằng:
```sql
\c flight_booking
\dt  -- Liệt kê các bảng
```

## Script nhanh (Copy & Paste)

```sql
-- Chạy trong psql với user postgres
CREATE DATABASE flight_booking;
\c flight_booking
CREATE USER DBMaybay WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO DBMaybay;
ALTER DATABASE flight_booking OWNER TO DBMaybay;
GRANT ALL ON SCHEMA public TO DBMaybay;
ALTER SCHEMA public OWNER TO DBMaybay;
\q
```

