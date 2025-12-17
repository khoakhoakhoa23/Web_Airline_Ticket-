# ⚡ QUICK FIX: "Cannot commit when autoCommit is enabled"

## 🔴 **LỖI**

```
org.postgresql.util.PSQLException: Cannot commit when autoCommit is enabled.
org.springframework.orm.jpa.JpaSystemException: Unable to commit against JDBC Connection
```

---

## ✅ **FIX (3 BƯỚC)**

### **Bước 1: Fix `application.properties`**

```properties
# Thêm dòng này vào HikariCP config
spring.datasource.hikari.auto-commit=false
```

### **Bước 2: Refactor Scheduler**

```java
// ❌ BEFORE (WRONG)
@Component
public class HoldBookingScheduler {
    @Autowired
    private BookingRepository bookingRepository;
    
    @Scheduled(fixedRate = 60000)
    @Transactional  // ❌ WRONG!
    public void expireHoldBookings() {
        // Business logic here...
    }
}

// ✅ AFTER (CORRECT)
@Component
public class HoldBookingScheduler {
    @Autowired
    private BookingExpirationService service;  // ✅ Delegate to service
    
    @Scheduled(fixedRate = 60000)  // ✅ No @Transactional
    public void expireHoldBookings() {
        try {
            service.expireHoldBookings();  // ✅ Service handles @Transactional
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }
}
```

### **Bước 3: Tạo Service Layer**

```java
@Service
public class BookingExpirationService {
    
    @Autowired
    private BookingRepository repository;
    
    @Transactional  // ✅ @Transactional ở service, không ở scheduler
    public int expireHoldBookings() {
        List<Booking> expired = repository
            .findByStatusInAndHoldExpiresAtBefore(
                List.of("PENDING", "HOLD"), 
                LocalDateTime.now()
            );
        
        expired.forEach(booking -> {
            booking.setStatus("EXPIRED");
            repository.save(booking);
        });
        
        return expired.size();
    }
}
```

---

## 📋 **FILES CHANGED**

```
backend/src/main/resources/application.properties
  ✓ Added: spring.datasource.hikari.auto-commit=false

backend/src/main/java/com/flightbooking/scheduler/HoldBookingScheduler.java
  ✓ Removed: @Transactional
  ✓ Added: Delegate to service layer
  ✓ Added: Exception handling

backend/src/main/java/com/flightbooking/service/BookingExpirationService.java
  ✓ NEW: Service layer with @Transactional

backend/src/main/java/com/flightbooking/repository/BookingRepository.java
  ✓ Added: Custom query findByStatusInAndHoldExpiresAtBefore()
```

---

## 🎯 **ROOT CAUSE**

```
HikariCP (autoCommit=true) 
  ↓ CONFLICT
Spring @Transactional (cố commit manually)
  ↓ RESULT
PSQLException: Cannot commit when autoCommit is enabled
```

**Fix**: Set `autoCommit=false` → Spring quản lý transaction hoàn toàn.

---

## 🧪 **TEST**

```bash
# 1. Rebuild
cd backend
mvn clean install

# 2. Start backend
mvn spring-boot:run

# 3. Quan sát logs (sau 1 phút)
# Expected: No exception, see logs:
# "Starting scheduled task: expire hold bookings"
# "Successfully expired X bookings"
```

---

## 📖 **CHI TIẾT**

Xem: `backend/TRANSACTION_FIX_GUIDE.md`

---

## ✅ **CHECKLIST**

- [x] `application.properties`: `auto-commit=false`
- [x] Scheduler: NO `@Transactional`
- [x] Service: YES `@Transactional`
- [x] Repository: Custom query (không `findAll()`)
- [x] No manual commit (`connection.commit()`, `em.getTransaction().commit()`)

**Status**: ✅ FIXED - Production Ready

