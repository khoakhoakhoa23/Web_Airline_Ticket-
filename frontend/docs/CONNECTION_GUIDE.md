# Hướng dẫn kết nối Backend và Frontend

## Cấu hình

### 1. Backend (Spring Boot)
- **Port**: 8080
- **Base URL**: `http://localhost:8080/api`
- **CORS**: Đã cấu hình cho phép `localhost:5173` (Vite) và `localhost:3000`

### 2. Frontend (React + Vite)
- **Port**: 5173 (mặc định)
- **API Base URL**: Được cấu hình trong `src/config/api.config.js`
- **Environment Variable**: Có thể override bằng `VITE_API_BASE_URL` trong file `.env`

## Cách chạy

### Bước 1: Chạy Backend
```bash
cd backend
# Chạy bằng Maven
mvn spring-boot:run

# Hoặc chạy từ IDE
# Mở FlightBookingApplication.java và Run
```

Backend sẽ chạy tại: `http://localhost:8080`

### Bước 2: Chạy Frontend
```bash
cd frontend
npm install  # Nếu chưa cài
npm run dev
```

Frontend sẽ chạy tại: `http://localhost:5173`

## Kiểm tra kết nối

### 1. Kiểm tra Backend đang chạy
Mở browser và truy cập: `http://localhost:8080/api/users`

Nếu thấy response (có thể là lỗi 404 hoặc danh sách users) thì backend đang chạy.

### 2. Kiểm tra CORS
Mở Developer Tools (F12) → Console
- Nếu không có lỗi CORS → Kết nối thành công
- Nếu có lỗi CORS → Kiểm tra lại cấu hình CORS trong backend

### 3. Test API từ Frontend
1. Mở trang Login: `http://localhost:5173/login`
2. Thử đăng nhập hoặc đăng ký
3. Kiểm tra Network tab trong Developer Tools để xem API calls

## Các API Endpoints chính

### Authentication
- `POST /api/users/register` - Đăng ký
- `POST /api/users/login` - Đăng nhập

### Flights
- `GET /api/flight-segments/search?origin=HAN&destination=SGN` - Tìm kiếm chuyến bay

### Bookings
- `POST /api/bookings` - Tạo booking
- `GET /api/bookings/{id}` - Lấy booking theo ID
- `GET /api/bookings/user/{userId}` - Lấy bookings của user

### Payments
- `POST /api/payments` - Tạo payment
- `GET /api/payments/booking/{bookingId}` - Lấy payments của booking

## Troubleshooting

### Lỗi CORS
**Triệu chứng**: Console hiển thị lỗi "CORS policy" hoặc "Access-Control-Allow-Origin"

**Giải pháp**:
1. Kiểm tra backend có đang chạy không
2. Kiểm tra port frontend có đúng không (5173)
3. Restart backend sau khi cập nhật CORS config

### Lỗi 404 Not Found
**Triệu chứng**: API call trả về 404

**Giải pháp**:
1. Kiểm tra endpoint URL có đúng không
2. Kiểm tra backend có đang chạy không
3. Kiểm tra base URL trong `api.config.js`

### Lỗi Network Error
**Triệu chứng**: "Network Error" hoặc "ERR_CONNECTION_REFUSED"

**Giải pháp**:
1. Đảm bảo backend đang chạy
2. Kiểm tra firewall không chặn port 8080
3. Kiểm tra URL trong `api.config.js` có đúng không

### Lỗi 401 Unauthorized
**Triệu chứng**: API trả về 401

**Giải pháp**:
1. Đăng nhập lại
2. Kiểm tra token có được lưu trong localStorage không
3. Kiểm tra token có được gửi trong header không

## Environment Variables

Tạo file `.env` trong thư mục `frontend/`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

Hoặc sử dụng file `.env.example` làm template.

## Lưu ý

- Backend phải chạy trước khi test frontend
- Đảm bảo database PostgreSQL đang chạy
- Kiểm tra console browser để debug các lỗi API
- Sử dụng Network tab trong DevTools để xem chi tiết API calls

