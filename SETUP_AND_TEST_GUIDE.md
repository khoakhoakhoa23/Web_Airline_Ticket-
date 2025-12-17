# 🚀 HƯỚNG DẪN CÀI ĐẶT & TEST

## ✅ ĐÃ HOÀN THÀNH

1. ✅ **Dependencies đã update** - Spring Security + JWT
2. ✅ **Code đã implement** - Password hashing, JWT, Flight Search
3. ✅ **Backend đã compile** - mvn clean install thành công

---

## 📋 CÀI ĐẶT

### Bước 1: Start Backend

**Option 1: Dùng PowerShell mới (RECOMMENDED)**
```powershell
cd D:\TMDT\WebMayBay\backend
mvn spring-boot:run
```

**Option 2: Dùng IDE**
- Mở `FlightBookingApplication.java`
- Click Run hoặc Debug
- Đợi backend khởi động (khoảng 30 giây)

**Kiểm tra backend đã chạy:**
- Xem console có dòng: `Started FlightBookingApplication`
- Hoặc test: http://localhost:8080/api/users (sẽ trả 401 - OK)

---

### Bước 2: Seed Database (Flights Data)

**Option 1: Dùng pgAdmin 4**
1. Mở pgAdmin 4
2. Connect đến database `flight_booking`
3. Mở Query Tool
4. Copy nội dung file: `D:\TMDT\WebMayBay\backend\database\seed-flights.sql`
5. Paste và Execute (F5)
6. Verify: `SELECT COUNT(*) FROM flights;` → Kết quả: 24 flights

**Option 2: Dùng Command Line**
```powershell
# Set password
$env:PGPASSWORD = "123456"

# Run seed script
psql -U dbmaybay -d flight_booking -f "D:\TMDT\WebMayBay\backend\database\seed-flights.sql"

# Verify
psql -U dbmaybay -d flight_booking -c "SELECT COUNT(*) FROM flights;"
```

---

## 🧪 TEST APIs

### Test 1: Register User (Password Hashing)

**Request:**
```powershell
$body = @{
    email = "test@test.com"
    password = "password123"
    phone = "0123456789"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

**Expected Response:**
```json
{
  "id": "uuid-here",
  "email": "test@test.com",
  "phone": "0123456789",
  "role": "USER",
  "status": "ACTIVE",
  "createdAt": "2025-12-17T...",
  "updatedAt": "2025-12-17T..."
}
```

**✅ Verify:**
- Password NOT in response
- Check database: Password starts with `$2a$` (BCrypt hash)

```sql
SELECT email, password FROM auth_user WHERE email = 'test@test.com';
-- Password should be: $2a$10$...
```

---

### Test 2: Login (Get JWT Token)

**Request:**
```powershell
$body = @{
    email = "test@test.com"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/users/login" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"

# Save token
$token = $response.token
Write-Host "Token: $token"
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "uuid-here",
    "email": "test@test.com",
    "phone": "0123456789",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

**✅ Verify:**
- Token is a long string (JWT)
- User object included
- Password NOT in response

---

### Test 3: Protected Endpoint (No Token) → Should Fail

**Request:**
```powershell
# This should return 401 Unauthorized
Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method Get
```

**Expected:** `401 Unauthorized` ✅

---

### Test 4: Protected Endpoint (With Token) → Should Work

**Request:**
```powershell
$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/users" `
    -Method Get `
    -Headers $headers
```

**Expected:** List of users ✅

---

### Test 5: Flight Search (Public Endpoint)

**Request:**
```powershell
$body = @{
    origin = "HAN"
    destination = "SGN"
    departDate = "2025-01-20"
    passengers = 2
    cabinClass = "ECONOMY"
    page = 0
    size = 10
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/flights/search" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

**Expected Response:**
```json
{
  "content": [
    {
      "id": "flight-001",
      "flightNumber": "VN123",
      "airline": "Vietnam Airlines",
      "origin": "HAN",
      "destination": "SGN",
      "departTime": "2025-01-20T06:00:00",
      "arriveTime": "2025-01-20T08:15:00",
      "baseFare": 2000000,
      "taxes": 500000,
      "totalPrice": 2500000,
      "availableSeats": 150,
      "cabinClass": "ECONOMY"
    }
  ],
  "totalElements": 4,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

**✅ Verify:**
- Returns list of flights
- Prices calculated correctly (baseFare + taxes)
- Available seats shown

---

## 🌐 TEST FRONTEND

### Bước 1: Start Frontend
```powershell
cd D:\TMDT\WebMayBay\frontend
npm run dev
```

### Bước 2: Test Flow

**1. Register:**
- Go to: http://localhost:5173/register
- Fill form and submit
- Should auto-login and get JWT token

**2. Login:**
- Go to: http://localhost:5173/login
- Enter email/password
- Should receive JWT token
- Check localStorage: `user` object contains `token`

**3. Flight Search:**
- Go to: http://localhost:5173
- Enter: HAN → SGN, Date: 2025-01-20
- Click "Search Flights"
- Should see list of flights

**4. Select Flight:**
- Click on a flight
- Should navigate to traveller info

---

## ✅ CHECKLIST

### Backend Security
- [ ] Register user → password hashed in database
- [ ] Login → receives JWT token
- [ ] Password NOT in API response
- [ ] Protected endpoints require token
- [ ] Public endpoints work without token

### Flight Search
- [ ] Database has 24 flights
- [ ] Search HAN → SGN returns results
- [ ] Pagination works
- [ ] Prices displayed correctly

### Frontend
- [ ] Register works
- [ ] Login works
- [ ] JWT token stored in localStorage
- [ ] Flight search works
- [ ] Flight selection works

---

## 🐛 TROUBLESHOOTING

### Backend không start
```powershell
# Check Java version
java -version  # Should be 17+

# Check PostgreSQL
Get-Service -Name postgresql*  # Should be Running

# Check port 8080
netstat -ano | findstr :8080  # Should be empty or backend process
```

### Database connection error
```powershell
# Test connection
psql -U dbmaybay -d flight_booking -c "SELECT 1;"

# Check application.properties
# spring.datasource.url=jdbc:postgresql://localhost:5432/flight_booking
# spring.datasource.username=dbmaybay
# spring.datasource.password=123456
```

### 401 Unauthorized on all endpoints
- Check if JWT token is being sent
- Check localStorage has `token` field
- Check token format: `Bearer <token>`

### Flight search returns empty
- Run seed-flights.sql
- Check: `SELECT COUNT(*) FROM flights;`
- Should have 24 flights

---

## 📊 SUCCESS CRITERIA

### All Tests Pass:
✅ Password hashed with BCrypt  
✅ JWT authentication working  
✅ Password not exposed  
✅ Endpoints protected  
✅ Flight search working  
✅ Frontend connected  

---

## 🚀 NEXT STEPS

After all tests pass:

1. **Payment Gateway Integration**
   - VNPay/Momo/Stripe
   - Webhook handling
   - Transaction management

2. **Email Notifications**
   - Spring Mail setup
   - Booking confirmation
   - Payment receipt

3. **Admin Panel**
   - Flight management
   - User management
   - Statistics dashboard

4. **Testing**
   - Unit tests
   - Integration tests
   - E2E tests

---

**Document Version:** 1.0  
**Last Updated:** December 17, 2025  
**Status:** Ready for Testing

