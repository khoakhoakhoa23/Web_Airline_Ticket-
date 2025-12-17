# Hướng dẫn Test API Backend

## ✅ Backend đã chạy thành công!

Lỗi "No static resource" khi truy cập `http://localhost:8080` là **BÌNH THƯỜNG** vì đây là REST API backend, không có trang web.

## 📋 Các API Endpoints để Test

### 1. User APIs

#### Đăng ký user mới
```
POST http://localhost:8080/api/users/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Test User",
  "phone": "0123456789"
}
```

#### Đăng nhập
```
POST http://localhost:8080/api/users/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

#### Lấy thông tin user
```
GET http://localhost:8080/api/users/{id}
```

### 2. Flight Search API

#### Tìm chuyến bay
```
GET http://localhost:8080/api/flight-segments/search?origin=HAN&destination=SGN
```

### 3. Booking APIs

#### Tạo booking
```
POST http://localhost:8080/api/bookings
Content-Type: application/json

{
  "userId": "user-id-here",
  "flightSegments": [...],
  "passengers": [...]
}
```

#### Lấy booking theo ID
```
GET http://localhost:8080/api/bookings/{id}
```

#### Lấy booking theo code
```
GET http://localhost:8080/api/bookings/code/{bookingCode}
```

#### Lấy bookings của user
```
GET http://localhost:8080/api/bookings/user/{userId}
```

## 🧪 Cách Test trong Browser

### Test đơn giản nhất:
Mở browser và truy cập:
```
http://localhost:8080/api/users
```

Nếu thấy response (có thể là `[]` hoặc danh sách users) → **Backend hoạt động tốt!**

## 🧪 Cách Test với Postman/Thunder Client

1. **Mở Postman hoặc Thunder Client** (extension trong VS Code)
2. **Tạo request mới:**
   - Method: `GET`
   - URL: `http://localhost:8080/api/users`
   - Click **Send**

3. **Nếu thấy response** → Backend hoạt động!

## 🧪 Cách Test với PowerShell

```powershell
# Test GET request
Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method Get

# Test POST request (register)
$body = @{
    email = "test@example.com"
    password = "password123"
    fullName = "Test User"
    phone = "0123456789"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body -ContentType "application/json"
```

## 🔍 Kiểm tra Database

Backend đã tự động tạo các bảng trong database. Kiểm tra trong pgAdmin4:

1. Mở pgAdmin4
2. Mở rộng: **Databases** → **flight_booking** → **Schemas** → **public** → **Tables**
3. Bạn sẽ thấy các bảng như:
   - `users`
   - `bookings`
   - `flight_segments`
   - `passengers`
   - `seat_selections`
   - `baggage_services`
   - `tickets`
   - `notifications`
   - `payments`

## ✅ Kết luận

Nếu bạn thấy:
- ✅ Backend chạy không có lỗi trong terminal
- ✅ Có thể truy cập các API endpoints
- ✅ Database có các bảng được tạo tự động

→ **Backend đã sẵn sàng kết nối với Frontend!**

## 🚀 Bước tiếp theo

1. **Chạy Frontend:**
   ```powershell
   cd D:\TMDT\WebMayBay\frontend
   npm run dev
   ```

2. **Frontend sẽ chạy trên:** `http://localhost:5173`

3. **Frontend sẽ tự động kết nối với Backend** tại `http://localhost:8080/api`

