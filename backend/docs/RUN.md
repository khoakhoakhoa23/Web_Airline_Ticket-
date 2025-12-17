# Hướng dẫn chạy Backend

## Cách 1: Chạy từ IDE (Khuyến nghị)

### IntelliJ IDEA:
1. Mở project trong IntelliJ IDEA
2. Mở file `FlightBookingApplication.java`
3. Click chuột phải → Run 'FlightBookingApplication.main()'

### Eclipse:
1. Mở project trong Eclipse
2. Click chuột phải vào `FlightBookingApplication.java`
3. Run As → Java Application

## Cách 2: Chạy bằng Maven Command Line

Nếu đã cài Maven:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

## Cách 3: Chạy bằng Maven Wrapper (nếu có)

```bash
cd backend
./mvnw spring-boot:run
```

## Lưu ý trước khi chạy:

1. **Đảm bảo PostgreSQL đang chạy**
2. **Tạo database:**
   ```sql
   CREATE DATABASE flight_booking;
   ```
3. **Kiểm tra thông tin kết nối trong `application.properties`:**
   - Username: DBMaybay
   - Password: 123456
   - Database: flight_booking

## Sau khi chạy thành công:

- Backend sẽ chạy tại: http://localhost:8080
- API endpoints: http://localhost:8080/api/bookings

