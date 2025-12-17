# Tóm tắt Tích hợp API Frontend - Backend

## ✅ Các thay đổi đã thực hiện

### 1. Cập nhật Error Handling
- **Login.jsx**: Cải thiện error handling để hiển thị thông báo lỗi từ API
- **Register.jsx**: Cải thiện error handling
- **Home.jsx**: Cải thiện error handling cho flight search
- **Payment.jsx**: Cải thiện error handling cho booking và payment

### 2. Cập nhật Format Dữ liệu

#### Register Page
- ✅ Đã đảm bảo chỉ gửi các trường: `email`, `password`, `phone`, `role`
- ✅ Loại bỏ `userName` và `family` (không cần thiết cho backend)

#### Payment Page
- ✅ Đã cập nhật format `flightSegments` để loại bỏ `id` và `bookingId` trước khi gửi
- ✅ Đảm bảo format đúng với `FlightSegmentDTO` của backend
- ✅ Đảm bảo format `passengers` đúng với `PassengerDTO` của backend

### 3. Cập nhật CORS Backend
Đã thêm `http://localhost:5173` và `http://localhost:5174` vào các controllers:
- ✅ AdminController
- ✅ NotificationController
- ✅ TicketController
- ✅ SeatSelectionController
- ✅ BaggageServiceController

Các controllers đã có CORS đầy đủ:
- ✅ UserController
- ✅ BookingController
- ✅ FlightSegmentController
- ✅ PaymentController

### 4. API Services đã được cấu hình
Tất cả các services trong `frontend/src/services/api.js` đã được cấu hình đúng:
- ✅ `userService`: register, login, getUserById, getUserByEmail
- ✅ `bookingService`: createBooking, getBookingById, getBookingByCode, getBookingsByUserId
- ✅ `paymentService`: createPayment, getPaymentById, getPaymentsByBookingId
- ✅ `flightSegmentService`: searchFlights, getSegmentsByBookingId
- ✅ `seatSelectionService`: createSeatSelection, getSeatSelectionsByPassengerId, getSeatSelectionsBySegmentId
- ✅ `baggageService`: createBaggageService, getBaggageServicesByPassengerId, getBaggageServicesBySegmentId
- ✅ `ticketService`: getTicketsByBookingId, getTicketById
- ✅ `notificationService`: getNotificationsByUserId, markAsRead

## 📋 Các API Endpoints được sử dụng

### User APIs
- `POST /api/users/register` - Đăng ký user mới
- `POST /api/users/login` - Đăng nhập
- `GET /api/users/{id}` - Lấy thông tin user
- `GET /api/users/email/{email}` - Lấy user theo email

### Flight Search APIs
- `GET /api/flight-segments/search?origin={origin}&destination={destination}` - Tìm chuyến bay

### Booking APIs
- `POST /api/bookings` - Tạo booking mới
- `GET /api/bookings/{id}` - Lấy booking theo ID
- `GET /api/bookings/code/{bookingCode}` - Lấy booking theo code
- `GET /api/bookings/user/{userId}` - Lấy bookings của user

### Payment APIs
- `POST /api/payments` - Tạo payment
- `GET /api/payments/{id}` - Lấy payment theo ID
- `GET /api/payments/booking/{bookingId}` - Lấy payments của booking

## 🔧 Cấu hình

### API Base URL
Được cấu hình trong `frontend/src/config/api.config.js`:
```javascript
BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
```

### Environment Variables
Có thể tạo file `.env` trong thư mục `frontend`:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

## ✅ Kiểm tra

### 1. Test API Connection
Mở browser console và kiểm tra:
```javascript
// Test API connection
fetch('http://localhost:8080/api/users')
  .then(res => res.json())
  .then(data => console.log('API Connected:', data))
  .catch(err => console.error('API Error:', err));
```

### 2. Test từ Frontend
1. Chạy frontend: `npm run dev`
2. Chạy backend: `.\mvnw.cmd spring-boot:run`
3. Mở browser: `http://localhost:5173`
4. Test các chức năng:
   - Đăng ký/Đăng nhập
   - Tìm kiếm chuyến bay
   - Tạo booking
   - Thanh toán

## 🐛 Troubleshooting

### Lỗi CORS
Nếu gặp lỗi CORS:
1. Kiểm tra backend đang chạy trên port 8080
2. Kiểm tra frontend đang chạy trên port 5173
3. Kiểm tra CORS configuration trong các controllers

### Lỗi Network
Nếu gặp lỗi network:
1. Kiểm tra backend đang chạy: `http://localhost:8080/api/users`
2. Kiểm tra API base URL trong `api.config.js`
3. Kiểm tra firewall/antivirus không chặn kết nối

### Lỗi Format Data
Nếu gặp lỗi format data:
1. Kiểm tra console để xem request/response
2. So sánh format với DTOs trong backend
3. Kiểm tra các trường bắt buộc

## 📝 Notes

- Tất cả các API calls đều sử dụng axios với interceptors để xử lý errors
- User data được lưu trong localStorage sau khi login/register
- Các API calls tự động thêm Authorization header nếu có user token
- Error messages được hiển thị bằng tiếng Việt cho user

