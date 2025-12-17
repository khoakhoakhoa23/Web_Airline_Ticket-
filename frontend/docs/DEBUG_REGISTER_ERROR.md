# Debug Lỗi 400 Bad Request khi Đăng ký

## ✅ Các cải thiện đã thực hiện

1. **Cải thiện Error Handling trong API Service**
   - Xử lý validation errors từ backend đúng cách
   - Hiển thị thông báo lỗi chi tiết từ backend

2. **Thêm Validation ở Frontend**
   - Kiểm tra email format
   - Kiểm tra password length (tối thiểu 6 ký tự)
   - Kiểm tra các trường bắt buộc

3. **Cải thiện Error Display**
   - Hiển thị tất cả validation errors từ backend
   - Format: `field: error message`

## 🔍 Cách Debug

### 1. Kiểm tra Browser Console

Mở Browser Console (F12) và xem:
- Request payload được gửi đi
- Response từ server
- Error messages chi tiết

### 2. Kiểm tra Network Tab

1. Mở Browser DevTools (F12)
2. Vào tab **Network**
3. Thử đăng ký lại
4. Click vào request `register`
5. Xem:
   - **Request Payload**: Dữ liệu đang được gửi
   - **Response**: Phản hồi từ server

### 3. Kiểm tra Backend Logs

Xem logs trong terminal chạy backend để thấy:
- Validation errors chi tiết
- Exception messages

## 🐛 Các lỗi thường gặp

### Lỗi: "Email is required"
- **Nguyên nhân**: Email không được gửi hoặc rỗng
- **Giải pháp**: Đảm bảo nhập email đúng format

### Lỗi: "Invalid email format"
- **Nguyên nhân**: Email không đúng format
- **Giải pháp**: Nhập email đúng format, ví dụ: `user@example.com`

### Lỗi: "Password must be at least 6 characters"
- **Nguyên nhân**: Password quá ngắn
- **Giải pháp**: Nhập password ít nhất 6 ký tự

### Lỗi: "Email already exists"
- **Nguyên nhân**: Email đã được đăng ký trước đó
- **Giải pháp**: Sử dụng email khác hoặc đăng nhập

## 📋 Format Dữ liệu đúng

### Request Body:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "phone": "0123456789",
  "role": "USER"
}
```

### Validation Rules:
- `email`: Required, phải đúng format email
- `password`: Required, tối thiểu 6 ký tự
- `phone`: Optional
- `role`: Optional (mặc định là "USER")

## 🧪 Test thủ công

### Test với cURL:
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "phone": "0123456789",
    "role": "USER"
  }'
```

### Test với PowerShell:
```powershell
$body = @{
    email = "test@example.com"
    password = "password123"
    phone = "0123456789"
    role = "USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" `
  -Method Post `
  -Body $body `
  -ContentType "application/json"
```

## ✅ Checklist

- [ ] Backend đang chạy trên port 8080
- [ ] Frontend đang chạy trên port 5173
- [ ] Email đúng format
- [ ] Password ít nhất 6 ký tự
- [ ] Không có lỗi CORS
- [ ] Kiểm tra Network tab để xem request/response

## 🔧 Nếu vẫn gặp lỗi

1. **Kiểm tra Backend Logs**: Xem chi tiết lỗi trong terminal
2. **Kiểm tra Database**: Đảm bảo database đang chạy và kết nối được
3. **Kiểm tra CORS**: Đảm bảo CORS đã được cấu hình đúng
4. **Clear Browser Cache**: Xóa cache và thử lại
5. **Kiểm tra Console**: Xem có lỗi JavaScript nào không

