# Hướng dẫn Test Kết nối Backend - Frontend

## Bước 1: Kiểm tra Backend đang chạy

### Cách 1: Kiểm tra qua Browser
Mở browser và truy cập:
```
http://localhost:8080/api/users
```

**Kết quả mong đợi:**
- Nếu thấy danh sách users (có thể là array rỗng `[]`) → Backend đang chạy ✅
- Nếu thấy lỗi 404 hoặc connection refused → Backend chưa chạy ❌

### Cách 2: Kiểm tra qua Terminal
```bash
curl http://localhost:8080/api/users
```

## Bước 2: Kiểm tra Frontend đang chạy

Mở browser và truy cập:
```
http://localhost:5173
```

**Kết quả mong đợi:**
- Trang Home hiển thị → Frontend đang chạy ✅
- Không hiển thị hoặc lỗi → Frontend chưa chạy ❌

## Bước 3: Test API từ Frontend

### Mở Developer Tools
1. Nhấn `F12` hoặc `Ctrl+Shift+I` (Windows) / `Cmd+Option+I` (Mac)
2. Chuyển sang tab **Console**
3. Chuyển sang tab **Network**

### Test 1: Test Login API
1. Vào trang Login: `http://localhost:5173/login`
2. Nhập email và password
3. Click "Login"
4. Kiểm tra Network tab:
   - Tìm request `POST /api/users/login`
   - Status code:
     - `200` → Thành công ✅
     - `401` → Sai email/password
     - `404` hoặc `Network Error` → Backend chưa chạy hoặc CORS lỗi

### Test 2: Test Register API
1. Vào trang Register: `http://localhost:5173/register`
2. Điền form đầy đủ
3. Click "Register"
4. Kiểm tra Network tab:
   - Tìm request `POST /api/users/register`
   - Status code:
     - `201` → Thành công ✅
     - `400` → Dữ liệu không hợp lệ hoặc email đã tồn tại
     - `404` hoặc `Network Error` → Backend chưa chạy

### Test 3: Test Flight Search API
1. Vào trang Home: `http://localhost:5173`
2. Điền form tìm kiếm:
   - From: `HAN` (Hà Nội)
   - To: `SGN` (Sài Gòn)
   - Depart: Chọn ngày
3. Click "Search"
4. Kiểm tra Network tab:
   - Tìm request `GET /api/flight-segments/search`
   - Status code:
     - `200` → Thành công ✅
     - `404` hoặc `Network Error` → Backend chưa chạy

## Bước 4: Kiểm tra CORS

### Triệu chứng lỗi CORS:
- Console hiển thị: `Access to XMLHttpRequest ... has been blocked by CORS policy`
- Network tab hiển thị status `(failed)` hoặc `CORS error`

### Giải pháp:
1. Đảm bảo backend đang chạy
2. Kiểm tra `CorsConfig.java` đã cho phép port 5173
3. Restart backend sau khi cập nhật CORS config
4. Kiểm tra `application.properties` có cấu hình CORS đúng không

## Bước 5: Test từ Console Browser

Mở Console trong Developer Tools và chạy:

```javascript
// Test API connection
fetch('http://localhost:8080/api/users')
  .then(res => res.json())
  .then(data => console.log('✅ API Connected!', data))
  .catch(err => console.error('❌ API Error:', err));
```

**Kết quả:**
- Thấy danh sách users → Kết nối thành công ✅
- Lỗi CORS → Cần kiểm tra CORS config
- Connection refused → Backend chưa chạy

## Troubleshooting

### Lỗi: "Network Error" hoặc "ERR_CONNECTION_REFUSED"
**Nguyên nhân:** Backend chưa chạy hoặc chạy sai port

**Giải pháp:**
1. Kiểm tra backend có đang chạy không
2. Kiểm tra port backend có đúng 8080 không
3. Kiểm tra firewall không chặn port 8080

### Lỗi: "CORS policy"
**Nguyên nhân:** CORS chưa được cấu hình đúng

**Giải pháp:**
1. Kiểm tra `CorsConfig.java` đã cho phép port 5173
2. Kiểm tra `application.properties` có cấu hình CORS
3. Restart backend
4. Xóa cache browser (Ctrl+Shift+Delete)

### Lỗi: "404 Not Found"
**Nguyên nhân:** Endpoint không đúng hoặc backend chưa có endpoint đó

**Giải pháp:**
1. Kiểm tra URL endpoint có đúng không
2. Kiểm tra backend có controller cho endpoint đó không
3. Kiểm tra `@RequestMapping` trong controller

### Lỗi: "401 Unauthorized"
**Nguyên nhân:** Chưa đăng nhập hoặc token không hợp lệ

**Giải pháp:**
1. Đăng nhập lại
2. Kiểm tra localStorage có user data không
3. Kiểm tra token có được gửi trong header không

## Checklist Kết nối

- [ ] Backend đang chạy tại `http://localhost:8080`
- [ ] Frontend đang chạy tại `http://localhost:5173`
- [ ] CORS đã được cấu hình cho port 5173
- [ ] Database PostgreSQL đang chạy
- [ ] Không có lỗi CORS trong Console
- [ ] API calls trả về status 200/201
- [ ] Login/Register hoạt động
- [ ] Flight search hoạt động

## Test Script

Tạo file `test-connection.html` trong thư mục `frontend/public/`:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Test API Connection</title>
</head>
<body>
    <h1>Test API Connection</h1>
    <button onclick="testConnection()">Test Connection</button>
    <div id="result"></div>
    
    <script>
        async function testConnection() {
            const resultDiv = document.getElementById('result');
            resultDiv.innerHTML = 'Testing...';
            
            try {
                const response = await fetch('http://localhost:8080/api/users');
                const data = await response.json();
                resultDiv.innerHTML = `<p style="color: green;">✅ Connected! Found ${data.length} users</p>`;
            } catch (error) {
                resultDiv.innerHTML = `<p style="color: red;">❌ Error: ${error.message}</p>`;
            }
        }
    </script>
</body>
</html>
```

Truy cập: `http://localhost:5173/test-connection.html`

