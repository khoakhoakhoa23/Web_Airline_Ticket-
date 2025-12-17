# Hướng dẫn Restart Backend

## ⚠️ Quan trọng

Sau khi cập nhật code backend, bạn **PHẢI restart backend** để các thay đổi có hiệu lực.

## 🔄 Cách Restart Backend

### Bước 1: Dừng Backend hiện tại
- Nếu đang chạy trong terminal, nhấn `Ctrl + C` để dừng
- Hoặc đóng terminal window

### Bước 2: Restart Backend
```powershell
cd D:\TMDT\WebMayBay\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

### Bước 3: Đợi Backend khởi động
- Đợi đến khi thấy: `Started FlightBookingApplication`
- Thường mất khoảng 10-30 giây

## ✅ Kiểm tra Backend đã chạy

Mở browser và truy cập:
```
http://localhost:8080/api/users
```

Nếu thấy response (có thể là `[]`) → Backend đã chạy thành công!

## 🧪 Test Đăng ký

Sau khi restart, thử đăng ký lại:
1. Mở frontend: `http://localhost:5173/register`
2. Điền form đăng ký
3. Click "Register"
4. Bây giờ bạn sẽ thấy error message chi tiết từ backend (nếu có lỗi)

## 📝 Lưu ý

- **Luôn restart backend** sau khi thay đổi code Java
- Nếu không restart, các thay đổi sẽ không có hiệu lực
- Backend sẽ tự động compile lại khi restart

