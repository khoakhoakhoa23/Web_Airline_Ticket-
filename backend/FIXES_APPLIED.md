# Root Cause Analysis & Fixes Applied

## 🔴 Root Causes Identified

### 1. **Username Case Mismatch (CRITICAL)**
- **Issue:** `application.properties` has `username=DBMaybay` but database user is `dbmaybay` (lowercase)
- **Impact:** PostgreSQL authentication fails → Hibernate cannot obtain JDBC connection → Spring Boot fails to start Tomcat
- **Fix:** Changed to `username=dbmaybay` (line 6)

### 2. **Missing Connection Validation**
- **Issue:** No connection test query configured
- **Impact:** Connection failures not detected early
- **Fix:** Added `connection-test-query=SELECT 1` and `validation-timeout=3000` (lines 23-24)

### 3. **JPA Configuration Optimization**
- **Issue:** Missing connection provider settings
- **Impact:** Potential transaction management issues
- **Fix:** Added `hibernate.connection.provider_disables_autocommit=true` and `open-in-view=false` (lines 17-18)

## ✅ Code Changes Applied

### File: `backend/src/main/resources/application.properties`

**Line 6:** Changed username
```properties
# BEFORE:
spring.datasource.username=DBMaybay

# AFTER:
spring.datasource.username=dbmaybay
```

**Lines 23-24:** Added connection validation
```properties
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=3000
```

**Lines 17-18:** Added JPA connection settings
```properties
spring.jpa.properties.hibernate.connection.provider_disables_autocommit=true
spring.jpa.open-in-view=false
```

**Line 12:** Changed show-sql to false (production-ready)
```properties
spring.jpa.show-sql=false
```

## 🔍 Verification Checklist

After applying fixes, verify:

- [ ] **Backend starts successfully**
  - Check logs for: `Started FlightBookingApplication`
  - Check logs for: `Tomcat started on port(s): 8080`
  - No "Could not open JPA EntityManager" errors

- [ ] **Database connection established**
  - Check logs for: `HikariPool-1 - Start completed`
  - No "password authentication failed" errors
  - No "connection refused" errors

- [ ] **Tomcat listening on port 8080**
  - Test: `curl http://localhost:8080/api/users` or browser
  - Should return: `[]` or list of users (not connection refused)

- [ ] **Register API works**
  - Test: `POST http://localhost:8080/api/users/register`
  - Payload: `{"email":"test@example.com","password":"password123"}`
  - Should return: `201 Created` with user data
  - Should NOT return: `400 Bad Request` or network error

- [ ] **Database user exists**
  - Verify: `psql -U dbmaybay -d flight_booking`
  - Password: `123456`
  - Should connect successfully

## 📝 Database User Verification

If user doesn't exist or has wrong password:

```sql
-- Connect as postgres superuser
psql -U postgres

-- Create/update user
CREATE USER dbmaybay WITH PASSWORD '123456';
ALTER USER dbmaybay WITH PASSWORD '123456';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE flight_booking TO dbmaybay;
\c flight_booking
GRANT ALL ON SCHEMA public TO dbmaybay;
ALTER SCHEMA public OWNER TO dbmaybay;
```

## 🚀 Expected Startup Logs

After fix, you should see:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
...
Started FlightBookingApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

## ❌ If Still Failing

1. **Check PostgreSQL is running:**
   ```powershell
   Get-Service -Name postgresql*
   ```

2. **Test database connection manually:**
   ```powershell
   psql -U dbmaybay -d flight_booking
   # Password: 123456
   ```

3. **Check backend logs for exact error message**

4. **Verify database exists:**
   ```sql
   \l  -- List databases
   ```

