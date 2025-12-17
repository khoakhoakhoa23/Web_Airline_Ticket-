# 🚀 FRONTEND ↔ BACKEND CONNECTION GUIDE
## React + Spring Boot + JWT Authentication

---

## 📊 **SYSTEM ARCHITECTURE**

```
┌─────────────────────────────────────────────────────────────────┐
│  FRONTEND (React)                                               │
│  http://localhost:3000                                          │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  User Login → AuthContext.login()                       │   │
│  │  ↓                                                       │   │
│  │  api.js → userService.login({ email, password })        │   │
│  │  ↓                                                       │   │
│  │  POST /api/users/login                                  │   │
│  │  { "email": "...", "password": "..." }                  │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                            ↓ HTTP Request
┌─────────────────────────────────────────────────────────────────┐
│  BACKEND (Spring Boot)                                          │
│  http://localhost:8080                                          │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  SecurityConfig → CORS + JWT Filter                     │   │
│  │  ↓                                                       │   │
│  │  UserController.login()                                 │   │
│  │  ↓                                                       │   │
│  │  UserService → Validate password (BCrypt)               │   │
│  │  ↓                                                       │   │
│  │  JwtUtil.generateToken(email)                           │   │
│  │  ↓                                                       │   │
│  │  Response: { "token": "jwt...", "user": {...} }         │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                            ↓ HTTP Response
┌─────────────────────────────────────────────────────────────────┐
│  FRONTEND (React)                                               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  localStorage.setItem('token', token)                   │   │
│  │  localStorage.setItem('user', JSON.stringify(user))     │   │
│  │  ↓                                                       │   │
│  │  Navigate to dashboard                                  │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                            ↓ Subsequent Requests
┌─────────────────────────────────────────────────────────────────┐
│  FRONTEND (React)                                               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  api.interceptors.request → Attach token                │   │
│  │  Authorization: Bearer <token>                          │   │
│  │  ↓                                                       │   │
│  │  GET /api/bookings (Protected endpoint)                 │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                            ↓ HTTP Request
┌─────────────────────────────────────────────────────────────────┐
│  BACKEND (Spring Boot)                                          │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  JwtAuthenticationFilter → Validate token               │   │
│  │  ↓                                                       │   │
│  │  Token valid? → Set Authentication in SecurityContext   │   │
│  │  ↓                                                       │   │
│  │  Controller method executes                             │   │
│  │  ↓                                                       │   │
│  │  Response: { data... }                                  │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔧 **BACKEND CONFIGURATION**

### **1. SecurityConfig.java** (Production-Ready)

```java
package com.flightbooking.config;

import com.flightbooking.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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
    
    /**
     * ✅ CORS Configuration for React Frontend
     * Allows requests from localhost:3000, 5173, 5174
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow React dev servers
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:5174"
        ));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow all headers (including Authorization)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Expose headers to frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type"
        ));
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * ✅ Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless JWT API)
            .csrf(csrf -> csrf.disable())
            
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Stateless session (JWT-based)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // ✅ Public endpoints (no JWT required)
                .requestMatchers(
                    "/api/users/register",
                    "/api/users/login",
                    "/api/flights/search"
                ).permitAll()
                
                // ✅ Admin endpoints (require ADMIN role)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // ✅ All other endpoints require JWT authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT filter
            .addFilterBefore(
                jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class
            );
        
        return http.build();
    }
}
```

**Key Points**:
- ✅ CSRF disabled (stateless REST API)
- ✅ CORS enabled for `localhost:3000` (React)
- ✅ Public endpoints: `/api/users/register`, `/api/users/login`, `/api/flights/search`
- ✅ Protected endpoints: All others require JWT
- ✅ Stateless session (no server-side session storage)

---

### **2. UserController.java** (Login Endpoint)

```java
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * ✅ LOGIN ENDPOINT
     * POST /api/users/login
     * Request: { "email": "...", "password": "..." }
     * Response: { "token": "jwt...", "user": { id, email, role, ... } }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * ✅ REGISTER ENDPOINT
     * POST /api/users/register
     * Request: { "email": "...", "password": "...", "phone": "..." }
     * Response: { id, email, role, status, ... } (NO password)
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        UserDTO user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * ✅ PROTECTED ENDPOINT (Requires JWT)
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
```

---

### **3. LoginResponse.java** (DTO)

```java
package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;  // JWT token
    private UserDTO user;  // User info (NO password)
}
```

---

## ⚛️ **FRONTEND CONFIGURATION**

### **1. api.js** (Axios Instance + Interceptors)

```javascript
import axios from 'axios';
import { API_CONFIG } from '../config/api.config';

// ✅ Create Axios instance
const api = axios.create({
  baseURL: 'http://localhost:8080/api',  // Backend URL
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,  // 10 seconds
});

// ✅ REQUEST INTERCEPTOR: Attach JWT token to every request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// ✅ RESPONSE INTERCEPTOR: Handle errors (401, 403, etc.)
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (!error.response) {
      // Network error
      error.message = 'Không thể kết nối đến server.';
      return Promise.reject(error);
    }

    const status = error.response.status;
    const data = error.response.data;

    if (status === 401) {
      // Unauthorized - Clear auth data and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      
      error.message = 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.';
    } else if (status === 403) {
      // Forbidden - User doesn't have permission
      error.message = 'Bạn không có quyền truy cập tài nguyên này.';
    } else if (status === 400) {
      // Validation error
      error.message = data?.message || 'Dữ liệu không hợp lệ.';
    }

    return Promise.reject(error);
  }
);

// ✅ USER SERVICE
export const userService = {
  register: (userData) => {
    return api.post('/users/register', userData);
  },
  
  login: (credentials) => {
    return api.post('/users/login', credentials);
  },
  
  getUserById: (id) => {
    return api.get(`/users/${id}`);
  },
};

// ✅ BOOKING SERVICE (Protected - requires JWT)
export const bookingService = {
  createBooking: (bookingData) => {
    return api.post('/bookings', bookingData);
  },
  
  getBookingsByUserId: (userId) => {
    return api.get(`/bookings/user/${userId}`);
  },
};

export default api;
```

**Key Points**:
- ✅ Axios instance with `baseURL: http://localhost:8080/api`
- ✅ Request interceptor: Attach `Authorization: Bearer <token>` header
- ✅ Response interceptor: Handle 401 (redirect to login), 403, 400
- ✅ Token stored in `localStorage` with key `'token'`

---

### **2. AuthContext.jsx** (Authentication State Management)

```javascript
import { createContext, useContext, useState, useEffect } from 'react';
import { userService } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // ✅ Initialize: Load user from localStorage on mount
  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('token');
    
    if (storedUser && storedToken) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (error) {
        console.error('Error parsing user data:', error);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
      }
    }
    
    setLoading(false);
  }, []);

  // ✅ LOGIN: email + password → JWT token + user data
  const login = async (email, password) => {
    try {
      const response = await userService.login({ email, password });
      const { token, user: userData } = response.data;
      
      // Store token and user separately
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userData));
      
      setUser(userData);
      return userData;
    } catch (error) {
      throw error;
    }
  };

  // ✅ REGISTER: Auto-login after registration
  const register = async (userData) => {
    try {
      // Step 1: Register
      await userService.register(userData);
      
      // Step 2: Auto-login to get JWT token
      const loginResponse = await userService.login({
        email: userData.email,
        password: userData.password
      });
      
      const { token, user: userDataResponse } = loginResponse.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userDataResponse));
      
      setUser(userDataResponse);
      return userDataResponse;
    } catch (error) {
      throw error;
    }
  };

  // ✅ LOGOUT: Clear auth data
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const value = {
    user,
    login,
    register,
    logout,
    loading,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
```

**Key Points**:
- ✅ Store `token` and `user` **separately** in `localStorage`
- ✅ `login()`: Get token from backend → Store locally → Set user state
- ✅ `register()`: Auto-login after registration
- ✅ `logout()`: Clear token and user from localStorage
- ✅ Auto-load user on app startup

---

### **3. Login Component Example**

```javascript
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // ✅ Call login from AuthContext
      await login(email, password);
      
      // ✅ Success: Navigate to dashboard
      navigate('/dashboard');
    } catch (err) {
      // ✅ Error: Display error message
      setError(err.message || 'Đăng nhập thất bại. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleSubmit}>
        <h2>Đăng Nhập</h2>
        
        {error && <div className="error">{error}</div>}
        
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        
        <input
          type="password"
          placeholder="Mật khẩu"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        
        <button type="submit" disabled={loading}>
          {loading ? 'Đang đăng nhập...' : 'Đăng Nhập'}
        </button>
      </form>
    </div>
  );
};

export default Login;
```

---

### **4. Protected Component Example**

```javascript
import { useEffect, useState } from 'react';
import { bookingService } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

const MyBookings = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const { user } = useAuth();

  useEffect(() => {
    const fetchBookings = async () => {
      try {
        // ✅ Call protected API (JWT automatically attached by interceptor)
        const response = await bookingService.getBookingsByUserId(user.id);
        setBookings(response.data);
      } catch (err) {
        setError(err.message || 'Không thể tải dữ liệu.');
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      fetchBookings();
    }
  }, [user]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h2>Booking của tôi</h2>
      {bookings.map(booking => (
        <div key={booking.id}>{booking.bookingCode}</div>
      ))}
    </div>
  );
};

export default MyBookings;
```

---

## 🔄 **COMPLETE FLOW: LOGIN → PROTECTED API CALL**

### **Step-by-Step**

1. **User enters credentials and clicks "Login"**
   ```javascript
   await login('user@example.com', 'password123');
   ```

2. **Frontend sends POST request to backend**
   ```
   POST http://localhost:8080/api/users/login
   Content-Type: application/json
   
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```

3. **Backend validates credentials**
   - `UserService.login()` → Check email exists
   - `BCryptPasswordEncoder.matches()` → Verify password
   - `JwtUtil.generateToken()` → Create JWT token

4. **Backend returns JWT token + user data**
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "user": {
       "id": "uuid-123",
       "email": "user@example.com",
       "role": "USER",
       "status": "ACTIVE"
     }
   }
   ```

5. **Frontend stores token and user**
   ```javascript
   localStorage.setItem('token', token);
   localStorage.setItem('user', JSON.stringify(user));
   setUser(user);
   ```

6. **User navigates to protected page (e.g., My Bookings)**

7. **Frontend makes API call to protected endpoint**
   ```javascript
   bookingService.getBookingsByUserId(user.id);
   ```

8. **Axios interceptor attaches JWT token**
   ```
   GET http://localhost:8080/api/bookings/user/uuid-123
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

9. **Backend validates JWT token**
   - `JwtAuthenticationFilter.doFilterInternal()`
   - Extract token from `Authorization: Bearer <token>`
   - `JwtUtil.validateToken()` → Verify signature, expiration
   - Set `Authentication` in `SecurityContextHolder`

10. **Backend processes request and returns data**
    ```json
    [
      {
        "id": "booking-1",
        "bookingCode": "BK123456",
        "status": "CONFIRMED",
        ...
      }
    ]
    ```

11. **Frontend displays data to user** ✅

---

## ✅ **CHECKLIST: ENSURE EVERYTHING WORKS**

### **Backend**
- [ ] `SecurityConfig.java` has proper CORS configuration
- [ ] Public endpoints: `/api/users/register`, `/api/users/login`, `/api/flights/search`
- [ ] Protected endpoints require JWT
- [ ] CSRF disabled (stateless API)
- [ ] Session management: STATELESS
- [ ] `JwtAuthenticationFilter` validates JWT on protected endpoints
- [ ] `LoginResponse` returns `{ token, user }`
- [ ] `UserDTO` does NOT include password

### **Frontend**
- [ ] Axios instance has `baseURL: http://localhost:8080/api`
- [ ] Request interceptor attaches `Authorization: Bearer <token>`
- [ ] Response interceptor handles 401 (redirect to login)
- [ ] Token stored in `localStorage` with key `'token'`
- [ ] User data stored in `localStorage` with key `'user'`
- [ ] `AuthContext` loads user on app startup
- [ ] `login()` stores token and user separately
- [ ] `logout()` clears token and user
- [ ] Protected components use `useAuth()` hook

---

## 🚨 **COMMON ISSUES & FIXES**

### **Issue 1: CORS Error**
```
Access to XMLHttpRequest at 'http://localhost:8080/api/users/login' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Fix**: Ensure `SecurityConfig.corsConfigurationSource()` includes `http://localhost:3000`

---

### **Issue 2: 401 Unauthorized on Protected Endpoint**
```
GET /api/bookings/user/123 → 401 Unauthorized
```

**Possible Causes**:
1. JWT token not sent in request
2. JWT token expired
3. JWT token invalid

**Fix**:
- Check `localStorage.getItem('token')` → Should return valid JWT
- Check Network tab → `Authorization: Bearer <token>` header present
- Check backend logs → JWT validation errors

---

### **Issue 3: 403 Forbidden**
```
GET /api/admin/users → 403 Forbidden
```

**Fix**: User doesn't have required role. Check:
- Backend: `.requestMatchers("/api/admin/**").hasRole("ADMIN")`
- User role in database: `role = 'ADMIN'` (not `'ROLE_ADMIN'`)

---

### **Issue 4: Token Not Attached**
**Symptom**: Protected API calls fail with 401, but token exists in localStorage

**Fix**: Check Axios interceptor is running
```javascript
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  console.log('Token:', token);  // Debug: Should print token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

---

## 🎯 **TESTING GUIDE**

### **Test 1: Register User**
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

**Expected**: `201 Created` + User data (no password)

---

### **Test 2: Login**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Expected**: `200 OK` + `{ "token": "...", "user": {...} }`

---

### **Test 3: Protected Endpoint (No Token)**
```bash
curl -X GET http://localhost:8080/api/users/123
```

**Expected**: `401 Unauthorized`

---

### **Test 4: Protected Endpoint (With Token)**
```bash
TOKEN="<paste_jwt_token_here>"

curl -X GET http://localhost:8080/api/users/123 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: `200 OK` + User data

---

## ✅ **SUMMARY**

| Component | Configuration | Purpose |
|-----------|---------------|---------|
| **Backend - SecurityConfig** | CORS + JWT Filter | Allow React requests, validate JWT |
| **Backend - UserController** | `/login`, `/register` | Public endpoints, return JWT |
| **Frontend - api.js** | Axios interceptors | Attach JWT to requests, handle errors |
| **Frontend - AuthContext** | State management | Store/load token, login/logout |

**Flow**:
1. User login → Backend validates → Return JWT
2. Frontend stores JWT in localStorage
3. Axios interceptor attaches JWT to all requests
4. Backend validates JWT → Allow/deny access

**Result**: ✅ Secure, stateless, production-ready FE ↔ BE connection

