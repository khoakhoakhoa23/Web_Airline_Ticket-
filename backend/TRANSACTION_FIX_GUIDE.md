# 🔧 FIX: "Cannot commit when autoCommit is enabled"

## 📊 **ROOT CAUSE ANALYSIS**

### **Vấn đề gì xảy ra?**

Lỗi `org.postgresql.util.PSQLException: Cannot commit when autoCommit is enabled` xảy ra khi có **CONFLICT trong quản lý transaction** giữa 3 layers:

1. **HikariCP (Connection Pool)**: Mặc định `autoCommit=true`
2. **Hibernate**: Config `provider_disables_autocommit=true` 
3. **Spring @Transactional**: Cố gắng quản lý transaction (begin/commit/rollback)

### **Tại sao lại conflict?**

```
┌─────────────────────────────────────────────────────────────┐
│  JDBC Connection (HikariCP)                                 │
│  autoCommit = true (default)                                │
│  ↓ Mọi statement tự động commit ngay lập tức                │
└─────────────────────────────────────────────────────────────┘
                    ↓ CONFLICT ↓
┌─────────────────────────────────────────────────────────────┐
│  Spring @Transactional                                      │
│  Cố gắng: connection.setAutoCommit(false)                   │
│  Cố gắng: connection.commit() khi method kết thúc          │
│  ❌ NHƯNG connection vẫn ở chế độ autoCommit=true           │
└─────────────────────────────────────────────────────────────┘
                    ↓ KẾT QUẢ ↓
      PSQLException: Cannot commit when autoCommit is enabled
```

### **Anti-Patterns phổ biến gây lỗi**

❌ **SAI**: Scheduler có `@Transactional`
```java
@Component
public class MyScheduler {
    @Scheduled(fixedRate = 60000)
    @Transactional  // ❌ WRONG! Scheduler không nên có @Transactional
    public void scheduledTask() {
        repository.save(...);
    }
}
```

❌ **SAI**: Manual commit trong Spring-managed code
```java
@Service
public class MyService {
    @Autowired
    private EntityManager entityManager;
    
    public void doSomething() {
        entityManager.getTransaction().commit(); // ❌ NEVER DO THIS!
    }
}
```

❌ **SAI**: HikariCP với autoCommit=true + Hibernate provider_disables_autocommit=true
```properties
# application.properties
spring.datasource.hikari.auto-commit=true  # ❌ Default value
spring.jpa.properties.hibernate.connection.provider_disables_autocommit=true  # ❌ CONFLICT!
```

---

## ✅ **GIẢI PHÁP PRODUCTION-READY**

### **1. Fix HikariCP Configuration**

**File**: `backend/src/main/resources/application.properties`

```properties
# Connection Pool Configuration
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=3000
# ✅ CRITICAL FIX: Disable autoCommit để Spring quản lý transaction
spring.datasource.hikari.auto-commit=false
```

**Giải thích**:
- `auto-commit=false` → HikariCP sẽ tạo connection với autoCommit=false
- Spring @Transactional sẽ quản lý transaction (begin, commit, rollback)
- Hibernate `provider_disables_autocommit=true` vẫn giữ nguyên (line 16) - OK!

---

### **2. Refactor Architecture: Scheduler → Service Layer**

#### **CHUẨN Spring Boot Architecture**

```
┌──────────────────────────────────────────────────────────┐
│  @Component Scheduler                                    │
│  - Chỉ gọi service                                       │
│  - KHÔNG có @Transactional                               │
│  - KHÔNG có business logic                               │
│  - Exception handling                                    │
└──────────────────────────────────────────────────────────┘
                     ↓ Delegate
┌──────────────────────────────────────────────────────────┐
│  @Service with @Transactional                            │
│  - Chứa business logic                                   │
│  - Spring quản lý transaction                            │
│  - Dễ test, dễ reuse                                     │
└──────────────────────────────────────────────────────────┘
                     ↓ Use
┌──────────────────────────────────────────────────────────┐
│  @Repository                                             │
│  - CRUD operations                                       │
│  - Custom queries                                        │
└──────────────────────────────────────────────────────────┘
```

#### **Code Implementation**

**A. Scheduler (NO @Transactional)**

```java
@Component
public class HoldBookingScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(HoldBookingScheduler.class);
    
    @Autowired
    private BookingExpirationService bookingExpirationService;
    
    /**
     * ✅ CORRECT: No @Transactional on scheduler method
     */
    @Scheduled(fixedRate = 60000)
    public void expireHoldBookings() {
        logger.debug("Starting scheduled task: expire hold bookings");
        
        try {
            // ✅ Delegate to service layer
            int expiredCount = bookingExpirationService.expireHoldBookings();
            
            if (expiredCount > 0) {
                logger.info("Expired {} hold bookings", expiredCount);
            }
        } catch (Exception e) {
            logger.error("Error in scheduled task", e);
            // ✅ Don't rethrow - let scheduler continue
        }
    }
}
```

**B. Service Layer (@Transactional)**

```java
@Service
public class BookingExpirationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingExpirationService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * ✅ CORRECT: @Transactional on service method
     * Spring will:
     * 1. Begin transaction (connection.setAutoCommit(false))
     * 2. Execute business logic
     * 3. Commit on success (connection.commit())
     * 4. Rollback on exception (connection.rollback())
     */
    @Transactional
    public int expireHoldBookings() {
        LocalDateTime now = LocalDateTime.now();
        
        // ✅ Use custom query - better performance than findAll()
        List<Booking> expiredBookings = bookingRepository
            .findByStatusInAndHoldExpiresAtBefore(
                List.of("PENDING", "HOLD"), 
                now
            );
        
        if (expiredBookings.isEmpty()) {
            return 0;
        }
        
        logger.info("Found {} expired bookings", expiredBookings.size());
        
        // ✅ Update entities - no manual commit needed!
        for (Booking booking : expiredBookings) {
            booking.setStatus("EXPIRED");
            bookingRepository.save(booking);
        }
        
        logger.info("Successfully expired {} bookings", expiredBookings.size());
        return expiredBookings.size();
    }
}
```

**C. Repository (Custom Query)**

```java
@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    
    /**
     * ✅ Custom query for better performance
     * Instead of: findAll() + stream().filter()
     */
    List<Booking> findByStatusInAndHoldExpiresAtBefore(
        List<String> statuses, 
        LocalDateTime expirationTime
    );
}
```

---

## 🎯 **TẠI SAO FIX NÀY HOẠT ĐỘNG?**

### **Before (❌ Broken)**

```
1. HikariCP tạo connection với autoCommit=true (default)
2. Scheduler method có @Transactional
3. Spring cố gọi connection.setAutoCommit(false) → FAIL hoặc IGNORED
4. Spring cố commit() → PSQLException: Cannot commit when autoCommit is enabled
```

### **After (✅ Working)**

```
1. HikariCP tạo connection với autoCommit=false (explicit config)
2. Scheduler method KHÔNG có @Transactional
3. Scheduler gọi Service method (có @Transactional)
4. Spring quản lý transaction trên Service:
   - Begin: connection already has autoCommit=false ✓
   - Execute business logic
   - Commit: connection.commit() → SUCCESS ✓
5. Transaction hoàn thành sạch sẽ
```

---

## 📋 **CHECKLIST: ĐẢM BẢO KHÔNG TÁI DIỄN**

### **✅ Configuration Checklist**

- [ ] `spring.datasource.hikari.auto-commit=false` trong `application.properties`
- [ ] `spring.jpa.properties.hibernate.connection.provider_disables_autocommit=true` (optional, nhưng OK nếu có)
- [ ] `spring.jpa.open-in-view=false` (best practice - tránh lazy loading issues)

### **✅ Code Checklist**

- [ ] Scheduler methods: **NO @Transactional**
- [ ] Scheduler methods: **Chỉ gọi service**, không chứa business logic
- [ ] Service methods: **@Transactional** on public methods
- [ ] **NO MANUAL COMMIT**: Không có `connection.commit()`, `entityManager.getTransaction().commit()`
- [ ] Repository: Custom queries thay vì `findAll().stream().filter()`
- [ ] Logging: Use SLF4J Logger thay vì `System.out.println`

### **✅ Best Practices**

- [ ] **Separation of Concerns**: Scheduler → Service → Repository
- [ ] **Transaction Boundaries**: @Transactional chỉ ở service layer
- [ ] **Exception Handling**: Try-catch trong scheduler, không throw ra ngoài
- [ ] **Performance**: Custom queries thay vì load toàn bộ data rồi filter
- [ ] **Logging**: INFO cho events quan trọng, DEBUG cho chi tiết

---

## 🧪 **TESTING**

### **Test 1: Scheduled Task Hoạt Động**

1. Start backend: `mvn spring-boot:run`
2. Quan sát logs:

```
2025-01-20 10:00:00.123 DEBUG HoldBookingScheduler : Starting scheduled task: expire hold bookings
2025-01-20 10:00:00.234 INFO  BookingExpirationService : Found 3 expired bookings
2025-01-20 10:00:00.345 INFO  BookingExpirationService : Successfully expired 3 bookings
2025-01-20 10:00:00.456 INFO  HoldBookingScheduler : Scheduled task completed: Expired 3 hold bookings
```

3. **Không có exception** → ✅ SUCCESS

### **Test 2: Transaction Rollback**

Tạo lỗi giả để test rollback:

```java
@Transactional
public int expireHoldBookings() {
    // ... expire logic ...
    
    if (expiredBookings.size() > 0) {
        throw new RuntimeException("Test rollback"); // Giả lập lỗi
    }
    
    return expiredBookings.size();
}
```

**Expected**: Không có booking nào bị update (transaction rollback) → ✅ Confirm transaction works

### **Test 3: Database Verification**

```sql
-- Tạo test data
INSERT INTO bookings (id, booking_code, status, hold_expires_at, total_amount, user_id)
VALUES 
    ('test-1', 'BK001', 'PENDING', NOW() - INTERVAL '1 hour', 1000000, 'user-1'),
    ('test-2', 'BK002', 'HOLD', NOW() - INTERVAL '30 minutes', 2000000, 'user-2');

-- Đợi scheduler chạy (1 phút)
-- Verify
SELECT id, booking_code, status, hold_expires_at 
FROM bookings 
WHERE id IN ('test-1', 'test-2');
```

**Expected**: Cả 2 booking có status = 'EXPIRED' → ✅ Logic works

---

## 🚨 **COMMON MISTAKES TO AVOID**

### **1. Scheduler với @Transactional**

```java
// ❌ WRONG
@Scheduled(fixedRate = 60000)
@Transactional
public void scheduledTask() { ... }

// ✅ CORRECT
@Scheduled(fixedRate = 60000)
public void scheduledTask() {
    myService.doWorkInTransaction(); // Service có @Transactional
}
```

### **2. Manual Transaction Management**

```java
// ❌ WRONG
@Service
public class MyService {
    @PersistenceContext
    private EntityManager em;
    
    public void doWork() {
        em.getTransaction().begin();  // ❌ NO!
        // ... work ...
        em.getTransaction().commit(); // ❌ NO!
    }
}

// ✅ CORRECT
@Service
public class MyService {
    @Autowired
    private MyRepository repository;
    
    @Transactional
    public void doWork() {
        // Spring manages transaction
        repository.save(...);
    }
}
```

### **3. findAll() + Stream Filter**

```java
// ❌ BAD PERFORMANCE
List<Booking> expired = bookingRepository.findAll().stream()
    .filter(b -> "PENDING".equals(b.getStatus()))
    .filter(b -> b.getHoldExpiresAt().isBefore(now))
    .toList();

// ✅ CORRECT - Database-level filtering
List<Booking> expired = bookingRepository
    .findByStatusInAndHoldExpiresAtBefore(List.of("PENDING", "HOLD"), now);
```

---

## 📚 **TÀI LIỆU THAM KHẢO**

- [Spring @Transactional Best Practices](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Hibernate Connection Handling](https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#database-connectionprovider)

---

## ✅ **SUMMARY**

| Vấn đề | Giải pháp |
|--------|-----------|
| `autoCommit=true` conflict | Set `spring.datasource.hikari.auto-commit=false` |
| Scheduler có `@Transactional` | Loại bỏ `@Transactional` khỏi scheduler |
| Business logic trong scheduler | Tạo service layer với `@Transactional` |
| `findAll()` + filter | Custom repository query |
| Manual commit | Để Spring quản lý transaction |

**Kết quả**: Transaction management sạch sẽ, không lỗi, production-ready! 🚀

