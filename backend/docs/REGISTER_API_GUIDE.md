# Register API - Complete Guide

## 📋 Correct JSON Payload

### Request Format:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "phone": "0123456789",
  "role": "USER"
}
```

### Field Requirements:

| Field | Required | Type | Validation | Default |
|-------|----------|------|------------|---------|
| `email` | ✅ Yes | String | Valid email format | - |
| `password` | ✅ Yes | String | Min 6 characters | - |
| `phone` | ❌ No | String | Any string | `null` |
| `role` | ❌ No | String | Any string | `"USER"` |

### Example Requests:

#### Minimal (Required fields only):
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

#### Full Request:
```json
{
  "email": "john.doe@example.com",
  "password": "securePass123",
  "phone": "+1234567890",
  "role": "USER"
}
```

## ✅ Success Response (201 Created)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "phone": "0123456789",
  "role": "USER",
  "status": "ACTIVE",
  "createdAt": "2025-12-16T14:30:00",
  "updatedAt": "2025-12-16T14:30:00"
}
```

## ❌ Error Responses

### 400 Bad Request - Validation Error
```json
{
  "email": "Email is required",
  "password": "Password must be at least 6 characters",
  "status": "VALIDATION_ERROR"
}
```

### 400 Bad Request - Runtime Error
```json
{
  "message": "Could not open JPA EntityManager for transaction",
  "status": "ERROR"
}
```
**Fix:** Restart backend with updated `application.properties`

### 409 Conflict - Email Already Exists
```json
{
  "message": "Email already exists",
  "status": "ERROR"
}
```

## 🔧 Backend Configuration

### application.properties (Updated)
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/flight_booking
spring.datasource.username=DBMaybay
spring.datasource.password=123456
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration (CRITICAL!)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

## 🧪 Test with cURL

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

## 🧪 Test with PowerShell

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

## 🧪 Test with Postman/Thunder Client

1. **Method:** POST
2. **URL:** `http://localhost:8080/api/users/register`
3. **Headers:**
   - `Content-Type: application/json`
4. **Body (raw JSON):**
   ```json
   {
     "email": "test@example.com",
     "password": "password123",
     "phone": "0123456789",
     "role": "USER"
   }
   ```

## 📝 Frontend Usage

### React Component Example:
```javascript
const handleRegister = async () => {
  try {
    const response = await userService.register({
      email: formData.email,
      password: formData.password,
      phone: formData.phone,
      role: 'USER'
    });
    console.log('User registered:', response.data);
  } catch (error) {
    console.error('Registration failed:', error.message);
    // Error message is already formatted by API interceptor
  }
};
```

## ✅ Checklist Before Testing

- [ ] Backend is running on port 8080
- [ ] PostgreSQL is running
- [ ] Database `flight_booking` exists
- [ ] User `DBMaybay` has correct password
- [ ] `application.properties` has `ddl-auto=update`
- [ ] Backend logs show successful database connection

## 🐛 Common Issues

### Issue: "Could not open JPA EntityManager"
**Solution:** 
1. Add `spring.jpa.hibernate.ddl-auto=update` to `application.properties`
2. Restart backend
3. Verify database connection

### Issue: "Password authentication failed"
**Solution:**
```sql
ALTER USER DBMaybay WITH PASSWORD '123456';
```

### Issue: "Relation does not exist"
**Solution:**
- Ensure `ddl-auto=update` is set
- Backend will create tables automatically on startup

