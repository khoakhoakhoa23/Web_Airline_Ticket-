# Fix: 400 Bad Request - Register API

## 🔴 1. PHÂN TÍCH NGUYÊN NHÂN

### Root Cause Analysis:

**Payload Frontend gửi:**
```json
{
  "email": "tung@gmail.com",
  "password": "Khoangu060",
  "phone": "0989948323",
  "role": "USER"
}
```

**DTO RegisterRequest validation:**
- ✅ `email`: `@NotBlank`, `@Email` - **PASS** (email hợp lệ)
- ✅ `password`: `@NotBlank`, `@Size(min=6)` - **PASS** (10 ký tự)
- ✅ `phone`: Không có validation - **PASS**
- ✅ `role`: Không có validation - **PASS**

**Kết luận:** Payload frontend **ĐÚNG**, DTO validation **PASS**.

### Các Nguyên Nhân Có Thể:

#### 1. Database Constraint Violation
- **Email đã tồn tại** → UNIQUE constraint violation
- **Column NOT NULL thiếu giá trị** → NOT NULL constraint violation
- **Table không tồn tại hoặc schema không khớp** → Transaction rollback

#### 2. Exception Handler Trả Về 400 Thay Vì 409
- Email đã tồn tại → Nên trả về `409 Conflict` nhưng có thể trả về `400 Bad Request`
- Exception handler có thể không catch đúng `DataIntegrityViolationException`

#### 3. Validation Error Không Được Format Đúng
- `MethodArgumentNotValidException` được handle nhưng format response không đúng
- Frontend không parse được error message

---

## 🔍 2. KIỂM TRA CÁC NGUYÊN NHÂN

### Kiểm tra #1: Email đã tồn tại?
```sql
SELECT * FROM auth_user WHERE email = 'tung@gmail.com';
```

### Kiểm tra #2: Database Schema
```sql
\d auth_user
-- Kiểm tra constraints: PRIMARY KEY, UNIQUE, NOT NULL
```

### Kiểm tra #3: Backend Logs
Tìm trong logs:
```
ERROR: duplicate key value violates unique constraint "auth_user_email_key"
ERROR: null value in column "xxx" violates not-null constraint
ERROR: could not open JPA EntityManager for transaction
```

---

## ✅ 3. CÁCH SỬA

### Fix #1: Cải Thiện Exception Handler

**Vấn đề:** Exception handler có thể không catch đúng exception hoặc format response không đúng.

**File:** `backend/src/main/java/com/flightbooking/exception/GlobalExceptionHandler.java`

**Cần sửa:**
1. Đảm bảo `DataIntegrityViolationException` được handle đúng
2. Đảm bảo `MethodArgumentNotValidException` trả về format đúng
3. Thêm logging để debug

### Fix #2: Đảm Bảo Database Schema Đúng

**Chạy script:**
```powershell
psql -U dbmaybay -d flight_booking -f database/fix-auth-user-table.sql
```

### Fix #3: Cải Thiện Frontend Error Handling

**File:** `frontend/src/services/api.js`

**Cần sửa:**
- Đảm bảo validation errors được parse đúng
- Hiển thị error message rõ ràng

---

## 📋 4. CODE SỬA LỖI

### Backend: Exception Handler (Đã có, cần cải thiện)

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> handleValidationExceptions(
        MethodArgumentNotValidException ex) {
    Map<String, Object> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
    });
    errors.put("status", "VALIDATION_ERROR");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
}
```

**✅ Code này đã đúng**, nhưng cần đảm bảo:
- `@Valid` annotation có trong Controller
- Spring Validation dependency có trong pom.xml

### Backend: DataIntegrityViolationException Handler

```java
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(
        DataIntegrityViolationException e) {
    Map<String, String> error = new HashMap<>();
    String message = e.getMessage();
    
    if (message != null) {
        if (message.contains("unique constraint") || message.contains("duplicate key")) {
            if (message.contains("email") || message.contains("auth_user_email_key")) {
                error.put("message", "Email already exists");
                error.put("status", "ERROR");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
        }
    }
    
    error.put("message", "Database constraint violation");
    error.put("status", "ERROR");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

**✅ Code này đã có**, nhưng cần kiểm tra:
- Exception có được throw đúng không
- Message có chứa "auth_user_email_key" không (sau khi đổi table name)

### Frontend: Error Handling (Cần cải thiện)

**File:** `frontend/src/services/api.js`

```javascript
} else if (status === 400) {
  // Validation errors
  if (typeof data === 'object' && data !== null) {
    if (data.status === 'VALIDATION_ERROR') {
      // Extract validation errors
      const errorMessages = Object.entries(data)
        .filter(([key]) => key !== 'status')
        .map(([field, message]) => `${field}: ${message}`);
      error.message = errorMessages.length > 0 
        ? errorMessages.join(', ') 
        : 'Dữ liệu không hợp lệ.';
      error.validationErrors = data;
    } else if (data.message) {
      error.message = data.message;
    } else {
      // Fallback
      const errors = Object.values(data).filter(v => typeof v === 'string');
      error.message = errors.length > 0 ? errors[0] : 'Dữ liệu không hợp lệ.';
    }
  } else {
    error.message = data?.message || 'Dữ liệu không hợp lệ.';
  }
}
```

**✅ Code này đã có**, nhưng cần đảm bảo:
- Error message được hiển thị đúng trong UI
- Validation errors được parse đúng

---

## 🚀 5. QUY TRÌNH DEBUG

### Bước 1: Kiểm tra Backend Logs
```powershell
# Xem logs backend khi gọi API
# Tìm: ERROR, WARN, hoặc exception stack trace
```

### Bước 2: Test API Trực Tiếp
```powershell
$body = @{
    email = "tung@gmail.com"
    password = "Khoangu060"
    phone = "0989948323"
    role = "USER"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/users/register" `
    -Method Post -Body $body -ContentType "application/json" `
    -ErrorAction SilentlyContinue

$response.StatusCode
$response.Content
```

### Bước 3: Kiểm tra Database
```sql
-- Kiểm tra email đã tồn tại chưa
SELECT * FROM auth_user WHERE email = 'tung@gmail.com';

-- Nếu có, xóa để test lại
DELETE FROM auth_user WHERE email = 'tung@gmail.com';
```

### Bước 4: Kiểm tra Exception Handler
- Đảm bảo `@RestControllerAdvice` được scan
- Đảm bảo exception handlers được gọi

---

## 📝 6. KẾT QUẢ MONG ĐỢI

### Nếu Email Đã Tồn Tại:
```json
Status: 409 Conflict
{
  "message": "Email already exists",
  "status": "ERROR"
}
```

### Nếu Validation Error:
```json
Status: 400 Bad Request
{
  "email": "Invalid email format",
  "password": "Password must be at least 6 characters",
  "status": "VALIDATION_ERROR"
}
```

### Nếu Database Error:
```json
Status: 400 Bad Request
{
  "message": "Database constraint violation: ...",
  "status": "ERROR"
}
```

---

## ⚠️ LƯU Ý

1. **Nếu vẫn lỗi 400 sau khi fix:**
   - Kiểm tra backend logs để xem exception cụ thể
   - Test API trực tiếp bằng curl/Postman
   - Kiểm tra database constraints

2. **Nếu email đã tồn tại:**
   - Xóa user cũ: `DELETE FROM auth_user WHERE email = 'tung@gmail.com';`
   - Hoặc dùng email khác để test

3. **Nếu validation error:**
   - Kiểm tra payload frontend có đúng format không
   - Kiểm tra DTO validation annotations

