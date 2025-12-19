# Hệ thống Đặt Vé Máy Bay

Dự án website đặt vé máy bay sử dụng React.js cho frontend và Spring Boot cho backend.

## Cấu trúc dự án

```
WebMayBay/
├── backend/          # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/flightbooking/
│   │   │   │   ├── entity/        # Các entity models
│   │   │   │   ├── repository/    # JPA Repositories
│   │   │   │   ├── service/       # Business logic
│   │   │   │   ├── controller/    # REST Controllers
│   │   │   │   ├── dto/           # Data Transfer Objects
│   │   │   │   └── config/        # Configuration
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── pom.xml
└── frontend/         # React.js Frontend
    ├── src/
    │   ├── components/    # React components
    │   ├── pages/         # Page components
    │   ├── App.js
    │   └── index.js
    └── package.json
```

## Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6+
- Node.js 16+ và npm
- PostgreSQL 12+

## Cài đặt và chạy Backend

1. Cài đặt PostgreSQL và tạo database:
   ```sql
   CREATE DATABASE flight_booking;
   ```

2. Cập nhật thông tin database trong `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/flight_booking
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. Mở terminal trong thư mục `backend` và chạy:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. Backend sẽ chạy tại: `http://localhost:8080`

## Cài đặt và chạy Frontend

1. Mở terminal trong thư mục `frontend` và cài đặt dependencies:
   ```bash
   npm install
   ```

2. Chạy ứng dụng:
   ```bash
   npm start
   ```

3. Frontend sẽ chạy tại: `http://localhost:3000`

## API Endpoints

### Bookings
- `POST /api/bookings` - Tạo booking mới
- `GET /api/bookings/{id}` - Lấy thông tin booking theo ID
- `GET /api/bookings/code/{bookingCode}` - Lấy thông tin booking theo mã
- `GET /api/bookings/user/{userId}` - Lấy danh sách booking của user
- `PUT /api/bookings/{id}/status` - Cập nhật trạng thái booking

## Database Schema

Hệ thống sử dụng 12 bảng chính:
- `users` - Thông tin người dùng
- `bookings` - Đặt chỗ
- `flight_segments` - Chuyến bay
- `passengers` - Hành khách
- `payments` - Thanh toán
- `payment_webhooks` - Webhook thanh toán
- `seat_selections` - Chọn ghế
- `baggage_services` - Dịch vụ hành lý
- `tickets` - Vé máy bay
- `notifications` - Thông báo
- `admin_actions` - Hành động admin
- `system_config` - Cấu hình hệ thống

## Tính năng

- ✅ Tìm kiếm chuyến bay
- ✅ Đặt vé máy bay
- ✅ Quản lý đặt chỗ
- ✅ Quản lý hành khách
- ✅ Thanh toán
- ✅ Quản lý ghế ngồi
- ✅ Dịch vụ hành lý

## Phát triển tiếp

Các tính năng cần phát triển thêm:
- [ ] Tích hợp API tìm kiếm chuyến bay thực tế
- [ ] Xác thực người dùng (JWT)
- [ ] Tích hợp thanh toán
- [ ] Gửi email thông báo
- [ ] Quản lý admin
- [ ] Dashboard thống kê

## License

MIT

