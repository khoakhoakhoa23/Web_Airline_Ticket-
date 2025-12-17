# 🎯 ACTION PLAN - IMMEDIATE PRIORITIES
## Hệ Thống Đặt Vé Máy Bay

---

## 🚨 CRITICAL SECURITY FIXES (Làm ngay - 1-2 ngày)

### ⚠️ Priority 1: Password Security (2-3 giờ)

#### Vấn đề hiện tại:
```java
// UserService.java - Line 31
user.setPassword(request.getPassword()); // ❌ PLAIN TEXT PASSWORD!

// UserService.java - Line 44
if (!user.getPassword().equals(request.getPassword())) // ❌ PLAIN TEXT COMPARISON!
```

#### Fix ngay:

**1. Add BCrypt dependency (đã có trong Spring Security)**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**2. Create PasswordEncoder Bean**
```java
// config/SecurityConfig.java (NEW FILE)
package com.flightbooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**3. Update UserService**
```java
// service/UserService.java
@Autowired
private PasswordEncoder passwordEncoder;

@Transactional
public UserDTO register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new RuntimeException("Email already exists");
    }
    
    User user = new User();
    user.setId(UUID.randomUUID().toString());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword())); // ✅ HASH PASSWORD
    user.setPhone(request.getPhone());
    user.setRole(request.getRole() != null ? request.getRole() : "USER");
    user.setStatus("ACTIVE");
    
    user = userRepository.save(user);
    return convertToDTO(user);
}

public UserDTO login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("Invalid email or password"));
    
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) { // ✅ COMPARE HASHED
        throw new RuntimeException("Invalid email or password");
    }
    
    if (!"ACTIVE".equals(user.getStatus())) {
        throw new RuntimeException("User account is not active");
    }
    
    return convertToDTO(user);
}
```

**4. Remove password from UserDTO response**
```java
// dto/UserDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String email;
    private String phone;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // ❌ NO PASSWORD FIELD!
}

// service/UserService.java - convertToDTO method
private UserDTO convertToDTO(User user) {
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setEmail(user.getEmail());
    dto.setPhone(user.getPhone());
    dto.setRole(user.getRole());
    dto.setStatus(user.getStatus());
    dto.setCreatedAt(user.getCreatedAt());
    dto.setUpdatedAt(user.getUpdatedAt());
    // ✅ DON'T SET PASSWORD
    return dto;
}
```

**Checklist:**
- [ ] Add Spring Security dependency
- [ ] Create SecurityConfig with PasswordEncoder bean
- [ ] Update UserService.register() to hash password
- [ ] Update UserService.login() to compare hashed password
- [ ] Remove password from UserDTO
- [ ] Test register with new user
- [ ] Test login with hashed password
- [ ] Verify password not in response

---

### ⚠️ Priority 2: JWT Authentication (1 ngày)

#### Vấn đề hiện tại:
- ❌ Không có token-based authentication
- ❌ API hoàn toàn public
- ❌ Không có session management

#### Implementation Steps:

**1. Add JWT dependencies**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

**2. Create JWT Utility**
```java
// util/JwtUtil.java (NEW FILE)
package com.flightbooking.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:mySecretKeyThatIsAtLeast256BitsLongForHS256Algorithm}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 24 hours
    private long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateToken(String userId, String email, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

**3. Create JWT Filter**
```java
// filter/JwtAuthenticationFilter.java (NEW FILE)
package com.flightbooking.filter;

import com.flightbooking.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractClaims(token).get("role", String.class);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

**4. Update SecurityConfig**
```java
// config/SecurityConfig.java
package com.flightbooking.config;

import com.flightbooking.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configure(http))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**5. Update Login Response to include JWT**
```java
// dto/LoginResponse.java (NEW FILE)
package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserDTO user;
}

// service/UserService.java
@Autowired
private JwtUtil jwtUtil;

public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("Invalid email or password"));
    
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new RuntimeException("Invalid email or password");
    }
    
    if (!"ACTIVE".equals(user.getStatus())) {
        throw new RuntimeException("User account is not active");
    }
    
    String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
    
    LoginResponse response = new LoginResponse();
    response.setToken(token);
    response.setUser(convertToDTO(user));
    return response;
}

// controller/UserController.java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = userService.login(request);
    return ResponseEntity.ok(response);
}
```

**6. Update application.properties**
```properties
# JWT Configuration
jwt.secret=mySecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmPleaseChangeInProduction
jwt.expiration=86400000
```

**7. Update Frontend to use JWT**
```javascript
// frontend/src/services/api.js
api.interceptors.request.use(
  (config) => {
    const user = localStorage.getItem('user');
    if (user) {
      try {
        const userData = JSON.parse(user);
        // Add JWT token
        if (userData.token) {
          config.headers.Authorization = `Bearer ${userData.token}`;
        }
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// frontend/src/contexts/AuthContext.jsx
const login = async (email, password) => {
  try {
    const response = await userService.login({ email, password });
    const loginData = response.data; // { token, user }
    localStorage.setItem('user', JSON.stringify(loginData));
    setUser(loginData.user);
    return loginData.user;
  } catch (error) {
    throw error;
  }
};

const register = async (userData) => {
  try {
    const response = await userService.register(userData);
    const userDataResponse = response.data;
    // After register, auto login
    const loginResponse = await userService.login({
      email: userData.email,
      password: userData.password
    });
    const loginData = loginResponse.data;
    localStorage.setItem('user', JSON.stringify(loginData));
    setUser(loginData.user);
    return loginData.user;
  } catch (error) {
    throw error;
  }
};
```

**Checklist:**
- [ ] Add JWT dependencies
- [ ] Create JwtUtil class
- [ ] Create JwtAuthenticationFilter
- [ ] Update SecurityConfig
- [ ] Create LoginResponse DTO
- [ ] Update UserService.login() to return token
- [ ] Update UserController.login()
- [ ] Add JWT config to application.properties
- [ ] Update frontend api.js to send token
- [ ] Update frontend AuthContext
- [ ] Test login → receive token
- [ ] Test protected endpoints with token
- [ ] Test protected endpoints without token → 401

---

## 🔥 HIGH PRIORITY FEATURES (Làm trong tuần 1-2)

### Priority 3: Flight Search API (1 ngày)

#### Vấn đề:
- ❌ Frontend có search form nhưng không có API
- ❌ Không có Flight entity riêng (chỉ có FlightSegment)

#### Solution:

**1. Create Flight Entity**
```java
// entity/Flight.java (NEW FILE)
package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {
    @Id
    private String id;
    
    @Column(name = "flight_number", nullable = false)
    private String flightNumber;
    
    @Column(nullable = false)
    private String airline;
    
    @Column(nullable = false, length = 10)
    private String origin;
    
    @Column(nullable = false, length = 10)
    private String destination;
    
    @Column(name = "depart_time", nullable = false)
    private LocalDateTime departTime;
    
    @Column(name = "arrive_time", nullable = false)
    private LocalDateTime arriveTime;
    
    @Column(name = "cabin_class", nullable = false)
    private String cabinClass;
    
    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal taxes;
    
    @Column(name = "available_seats")
    private Integer availableSeats;
    
    @Column(name = "total_seats")
    private Integer totalSeats;
    
    private String status; // SCHEDULED, DELAYED, CANCELLED, COMPLETED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**2. Create Flight Repository**
```java
// repository/FlightRepository.java (NEW FILE)
package com.flightbooking.repository;

import com.flightbooking.entity.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {
    
    @Query("SELECT f FROM Flight f WHERE " +
           "f.origin = :origin AND " +
           "f.destination = :destination AND " +
           "f.departTime >= :startDate AND " +
           "f.departTime < :endDate AND " +
           "f.availableSeats >= :passengers AND " +
           "f.status = 'SCHEDULED'")
    Page<Flight> searchFlights(String origin, 
                                String destination, 
                                LocalDateTime startDate, 
                                LocalDateTime endDate, 
                                Integer passengers, 
                                Pageable pageable);
    
    List<Flight> findByFlightNumberAndDepartTimeBetween(String flightNumber, 
                                                         LocalDateTime start, 
                                                         LocalDateTime end);
}
```

**3. Create Flight Service**
```java
// service/FlightService.java (NEW FILE)
package com.flightbooking.service;

import com.flightbooking.dto.FlightDTO;
import com.flightbooking.dto.FlightSearchRequest;
import com.flightbooking.entity.Flight;
import com.flightbooking.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlightService {
    
    @Autowired
    private FlightRepository flightRepository;
    
    public Page<FlightDTO> searchFlights(FlightSearchRequest request) {
        LocalDateTime startDate = request.getDepartDate().atStartOfDay();
        LocalDateTime endDate = request.getDepartDate().atTime(LocalTime.MAX);
        
        PageRequest pageRequest = PageRequest.of(
            request.getPage(), 
            request.getSize(), 
            Sort.by("departTime").ascending()
        );
        
        Page<Flight> flights = flightRepository.searchFlights(
            request.getOrigin(),
            request.getDestination(),
            startDate,
            endDate,
            request.getPassengers(),
            pageRequest
        );
        
        return flights.map(this::convertToDTO);
    }
    
    private FlightDTO convertToDTO(Flight flight) {
        FlightDTO dto = new FlightDTO();
        dto.setId(flight.getId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setAirline(flight.getAirline());
        dto.setOrigin(flight.getOrigin());
        dto.setDestination(flight.getDestination());
        dto.setDepartTime(flight.getDepartTime());
        dto.setArriveTime(flight.getArriveTime());
        dto.setCabinClass(flight.getCabinClass());
        dto.setBaseFare(flight.getBaseFare());
        dto.setTaxes(flight.getTaxes());
        dto.setAvailableSeats(flight.getAvailableSeats());
        dto.setStatus(flight.getStatus());
        return dto;
    }
}
```

**4. Create Flight Controller**
```java
// controller/FlightController.java (NEW FILE)
package com.flightbooking.controller;

import com.flightbooking.dto.FlightDTO;
import com.flightbooking.dto.FlightSearchRequest;
import com.flightbooking.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class FlightController {
    
    @Autowired
    private FlightService flightService;
    
    @PostMapping("/search")
    public ResponseEntity<Page<FlightDTO>> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        Page<FlightDTO> flights = flightService.searchFlights(request);
        return ResponseEntity.ok(flights);
    }
}
```

**5. Create DTOs**
```java
// dto/FlightSearchRequest.java (NEW FILE)
package com.flightbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {
    @NotBlank(message = "Origin is required")
    private String origin;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @NotNull(message = "Depart date is required")
    private LocalDate departDate;
    
    @Min(value = 1, message = "At least 1 passenger required")
    private Integer passengers = 1;
    
    private String cabinClass; // ECONOMY, BUSINESS, FIRST
    
    private int page = 0;
    private int size = 20;
}

// dto/FlightDTO.java (NEW FILE)
package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private String id;
    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    private LocalDateTime departTime;
    private LocalDateTime arriveTime;
    private String cabinClass;
    private BigDecimal baseFare;
    private BigDecimal taxes;
    private Integer availableSeats;
    private String status;
}
```

**6. Create seed data script**
```sql
-- database/seed-flights.sql (NEW FILE)
INSERT INTO flights (id, flight_number, airline, origin, destination, depart_time, arrive_time, cabin_class, base_fare, taxes, available_seats, total_seats, status, created_at, updated_at)
VALUES
-- HAN -> SGN
('flight-001', 'VN123', 'Vietnam Airlines', 'HAN', 'SGN', '2025-01-20 06:00:00', '2025-01-20 08:15:00', 'ECONOMY', 2000000, 500000, 150, 180, 'SCHEDULED', NOW(), NOW()),
('flight-002', 'VJ456', 'VietJet Air', 'HAN', 'SGN', '2025-01-20 08:30:00', '2025-01-20 10:45:00', 'ECONOMY', 1500000, 400000, 120, 180, 'SCHEDULED', NOW(), NOW()),
('flight-003', 'BB789', 'Bamboo Airways', 'HAN', 'SGN', '2025-01-20 14:00:00', '2025-01-20 16:15:00', 'ECONOMY', 1800000, 450000, 100, 150, 'SCHEDULED', NOW(), NOW()),

-- SGN -> HAN
('flight-004', 'VN124', 'Vietnam Airlines', 'SGN', 'HAN', '2025-01-20 09:00:00', '2025-01-20 11:15:00', 'ECONOMY', 2000000, 500000, 140, 180, 'SCHEDULED', NOW(), NOW()),
('flight-005', 'VJ457', 'VietJet Air', 'SGN', 'HAN', '2025-01-20 11:30:00', '2025-01-20 13:45:00', 'ECONOMY', 1500000, 400000, 110, 180, 'SCHEDULED', NOW(), NOW()),

-- HAN -> DAD
('flight-006', 'VN234', 'Vietnam Airlines', 'HAN', 'DAD', '2025-01-20 07:00:00', '2025-01-20 08:30:00', 'ECONOMY', 1200000, 300000, 130, 150, 'SCHEDULED', NOW(), NOW()),
('flight-007', 'VJ567', 'VietJet Air', 'HAN', 'DAD', '2025-01-20 15:00:00', '2025-01-20 16:30:00', 'ECONOMY', 1000000, 250000, 100, 150, 'SCHEDULED', NOW(), NOW());
```

**7. Update Frontend**
```javascript
// frontend/src/services/api.js
export const flightService = {
  searchFlights: (searchData) => {
    return api.post('/flights/search', searchData);
  },
};

// frontend/src/pages/Home.jsx
import { flightService } from '../services/api';

const handleSearch = async (e) => {
  e.preventDefault();
  setLoading(true);
  setError('');
  
  try {
    const searchData = {
      origin: formData.from,
      destination: formData.to,
      departDate: formData.departDate,
      passengers: parseInt(formData.passengers) || 1,
      cabinClass: formData.class,
      page: 0,
      size: 20
    };
    
    const response = await flightService.searchFlights(searchData);
    // Store search results and navigate
    sessionStorage.setItem('searchResults', JSON.stringify(response.data));
    sessionStorage.setItem('searchParams', JSON.stringify(searchData));
    navigate('/flight-selection');
  } catch (err) {
    setError(err.message || 'Failed to search flights');
  } finally {
    setLoading(false);
  }
};

// frontend/src/pages/FlightSelection.jsx
useEffect(() => {
  const results = sessionStorage.getItem('searchResults');
  if (results) {
    const data = JSON.parse(results);
    setFlights(data.content); // Page<FlightDTO> returns content array
    setTotalPages(data.totalPages);
  }
}, []);
```

**Checklist:**
- [ ] Create Flight entity
- [ ] Create FlightRepository
- [ ] Create FlightService
- [ ] Create FlightController
- [ ] Create FlightSearchRequest DTO
- [ ] Create FlightDTO
- [ ] Run seed-flights.sql
- [ ] Update frontend flightService
- [ ] Update Home page search
- [ ] Update FlightSelection page
- [ ] Test search flow end-to-end

---

## 📊 SUMMARY

### Timeline:
- **Day 1:** Password hashing + Remove password from response
- **Day 2:** JWT authentication implementation
- **Day 3:** Flight search API + seed data
- **Day 4:** Connect frontend to flight search
- **Day 5:** Testing & bug fixes

### Expected Results:
✅ Secure password storage  
✅ JWT-based authentication  
✅ Protected API endpoints  
✅ Working flight search  
✅ End-to-end booking flow (search → select → book)

### Next Phase:
After completing these immediate priorities, move to:
1. Payment gateway integration
2. Email notifications
3. Admin panel
4. Comprehensive testing

---

**Document Version:** 1.0  
**Last Updated:** December 17, 2025  
**Priority:** CRITICAL - START IMMEDIATELY

