# ✅ ADMIN PANEL - COMPLETE IMPLEMENTATION GUIDE

## 🎉 **BACKEND COMPLETE - FOUNDATION READY**

---

## 📊 **WHAT'S IMPLEMENTED**

### **Backend** ✅
```
backend/src/main/java/com/flightbooking/
├── dto/
│   └── DashboardStats.java                        ✅ Created
├── service/
│   └── AdminService.java                          ✅ Created
└── controller/
    └── AdminController.java                       ✅ Created
```

---

## 🔧 **PHASE 1: BACKEND - ADD MISSING METHODS**

### **Step 1: Update BookingRepository**

**File**: `backend/src/main/java/com/flightbooking/repository/BookingRepository.java`

**Add these methods**:
```java
import java.time.LocalDateTime;

// Count bookings between dates
Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

// Count by status
Long countByStatus(String status);
```

### **Step 2: Update UserRepository**

**File**: `backend/src/main/java/com/flightbooking/repository/UserRepository.java`

**Add these methods**:
```java
// Count by status
Long countByStatus(String status);
```

### **Step 3: Update BookingService**

**File**: `backend/src/main/java/com/flightbooking/service/BookingService.java`

**Add these methods**:
```java
/**
 * Get all bookings (admin only)
 */
public Page<BookingDTO> getAllBookings(Pageable pageable) {
    Page<Booking> bookings = bookingRepository.findAll(pageable);
    return bookings.map(this::convertToDTO);
}

/**
 * Get bookings by status
 */
public Page<BookingDTO> getBookingsByStatus(String status, Pageable pageable) {
    Page<Booking> bookings = bookingRepository.findByStatus(status, pageable);
    return bookings.map(this::convertToDTO);
}

/**
 * Get booking by ID (admin can see any booking)
 */
public BookingDTO getAdminBookingById(String bookingId) {
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    return convertToDTO(booking);
}

/**
 * Admin cancel booking (override)
 */
@Transactional
public void adminCancelBooking(String bookingId) {
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    
    // Admin can cancel any booking (even confirmed ones)
    booking.setStatus("CANCELLED");
    bookingRepository.save(booking);
    
    logger.info("Admin cancelled booking: {}", bookingId);
}
```

### **Step 4: Create UserService Methods**

**File**: `backend/src/main/java/com/flightbooking/service/UserService.java`

**Add these methods**:
```java
/**
 * Get all users (admin only)
 */
public Page<UserDTO> getAllUsers(Pageable pageable) {
    Page<User> users = userRepository.findAll(pageable);
    return users.map(this::convertToDTO);
}

/**
 * Get user by ID
 */
public UserDTO getUserById(String userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    return convertToDTO(user);
}

/**
 * Update user role
 */
@Transactional
public void updateUserRole(String userId, String newRole) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    
    // Validate role
    if (!newRole.equals("ROLE_USER") && !newRole.equals("ROLE_ADMIN")) {
        throw new BusinessException("INVALID_ROLE", "Invalid role: " + newRole);
    }
    
    user.setRole(newRole);
    userRepository.save(user);
    
    logger.info("Updated user {} role to {}", userId, newRole);
}

/**
 * Update user status
 */
@Transactional
public void updateUserStatus(String userId, String newStatus) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    
    user.setStatus(newStatus);
    userRepository.save(user);
    
    logger.info("Updated user {} status to {}", userId, newStatus);
}

/**
 * Convert User to UserDTO
 */
private UserDTO convertToDTO(User user) {
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setEmail(user.getEmail());
    dto.setPhone(user.getPhone());
    dto.setRole(user.getRole());
    dto.setStatus(user.getStatus());
    dto.setCreatedAt(user.getCreatedAt());
    return dto;
}
```

### **Step 5: Update FlightService**

**File**: `backend/src/main/java/com/flightbooking/service/FlightService.java`

**Add these methods**:
```java
/**
 * Get all flights with pagination
 */
public Page<FlightDTO> getAllFlightsPaged(Pageable pageable) {
    Page<Flight> flights = flightRepository.findAll(pageable);
    return flights.map(this::convertToDTO);
}

/**
 * Create new flight
 */
@Transactional
public FlightDTO createFlight(FlightDTO flightDTO) {
    Flight flight = new Flight();
    flight.setId(UUID.randomUUID().toString());
    flight.setFlightNumber(flightDTO.getFlightNumber());
    flight.setAirline(flightDTO.getAirline());
    flight.setOrigin(flightDTO.getOrigin());
    flight.setDestination(flightDTO.getDestination());
    flight.setDepartTime(flightDTO.getDepartTime());
    flight.setArriveTime(flightDTO.getArriveTime());
    flight.setCabinClass(flightDTO.getCabinClass());
    flight.setBaseFare(flightDTO.getBaseFare());
    flight.setTaxes(flightDTO.getTaxes());
    flight.setAvailableSeats(flightDTO.getAvailableSeats());
    flight.setTotalSeats(flightDTO.getTotalSeats());
    flight.setStatus("SCHEDULED");
    
    flight = flightRepository.save(flight);
    return convertToDTO(flight);
}

/**
 * Update flight
 */
@Transactional
public FlightDTO updateFlight(String flightId, FlightDTO flightDTO) {
    Flight flight = flightRepository.findById(flightId)
            .orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));
    
    // Update fields
    if (flightDTO.getFlightNumber() != null) flight.setFlightNumber(flightDTO.getFlightNumber());
    if (flightDTO.getAirline() != null) flight.setAirline(flightDTO.getAirline());
    if (flightDTO.getBaseFare() != null) flight.setBaseFare(flightDTO.getBaseFare());
    if (flightDTO.getAvailableSeats() != null) flight.setAvailableSeats(flightDTO.getAvailableSeats());
    if (flightDTO.getStatus() != null) flight.setStatus(flightDTO.getStatus());
    
    flight = flightRepository.save(flight);
    return convertToDTO(flight);
}

/**
 * Delete flight
 */
@Transactional
public void deleteFlight(String flightId) {
    Flight flight = flightRepository.findById(flightId)
            .orElseThrow(() -> new ResourceNotFoundException("Flight", flightId));
    
    // Check if flight has confirmed bookings
    // TODO: Add this check
    
    flightRepository.delete(flight);
    logger.info("Deleted flight: {}", flightId);
}
```

### **Step 6: Update SecurityConfig**

**File**: `backend/src/main/java/com/flightbooking/config/SecurityConfig.java`

**Verify admin endpoints are protected**:
```java
.authorizeHttpRequests(auth -> auth
    // Public endpoints
    .requestMatchers("/api/auth/**", "/api/flights/search").permitAll()
    
    // Admin endpoints (require ROLE_ADMIN)
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    
    // All other endpoints require authentication
    .anyRequest().authenticated()
)
```

---

## 🎨 **PHASE 2: FRONTEND - ADMIN PANEL UI**

### **File Structure**

```
frontend/src/
├── pages/
│   └── admin/
│       ├── AdminDashboard.jsx                     ⏭️ CREATE
│       ├── AdminBookings.jsx                      ⏭️ CREATE
│       ├── AdminUsers.jsx                         ⏭️ CREATE
│       ├── AdminFlights.jsx                       ⏭️ CREATE
│       └── AdminStatistics.jsx                    ⏭️ CREATE
├── components/
│   └── admin/
│       ├── AdminLayout.jsx                        ⏭️ CREATE
│       ├── AdminSidebar.jsx                       ⏭️ CREATE
│       ├── StatCard.jsx                           ⏭️ CREATE
│       └── DataTable.jsx                          ⏭️ CREATE
├── services/
│   └── adminApi.js                                ⏭️ CREATE
└── App.jsx                                        ⏭️ UPDATE
```

### **Step 1: Create adminApi.js**

**File**: `frontend/src/services/adminApi.js`

```javascript
import api from './api';

/**
 * Admin API Service
 * All endpoints require ROLE_ADMIN
 */

export const adminApi = {
  // Dashboard
  getDashboardStats: () => api.get('/admin/dashboard'),
  
  // Bookings
  getAllBookings: (params) => api.get('/admin/bookings', { params }),
  getBooking: (id) => api.get(`/admin/bookings/${id}`),
  cancelBooking: (id) => api.put(`/admin/bookings/${id}/cancel`),
  
  // Users
  getAllUsers: (params) => api.get('/admin/users', { params }),
  getUser: (id) => api.get(`/admin/users/${id}`),
  updateUserRole: (id, role) => api.put(`/admin/users/${id}/role`, { role }),
  updateUserStatus: (id, status) => api.put(`/admin/users/${id}/status`, { status }),
  
  // Flights
  getAllFlights: (params) => api.get('/admin/flights', { params }),
  createFlight: (flightData) => api.post('/admin/flights', flightData),
  updateFlight: (id, flightData) => api.put(`/admin/flights/${id}`, flightData),
  deleteFlight: (id) => api.delete(`/admin/flights/${id}`),
};
```

### **Step 2: Create AdminLayout.jsx**

**File**: `frontend/src/components/admin/AdminLayout.jsx`

```jsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import AdminSidebar from './AdminSidebar';
import './AdminLayout.css';

const AdminLayout = () => {
  const { user, isAuthenticated } = useAuth();
  
  // Check if user is admin
  if (!isAuthenticated || user?.role !== 'ROLE_ADMIN') {
    return <Navigate to="/" replace />;
  }
  
  return (
    <div className="admin-layout">
      <AdminSidebar />
      <main className="admin-content">
        <Outlet />
      </main>
    </div>
  );
};

export default AdminLayout;
```

### **Step 3: Create AdminSidebar.jsx**

**File**: `frontend/src/components/admin/AdminSidebar.jsx`

```jsx
import { NavLink } from 'react-router-dom';
import './AdminSidebar.css';

const AdminSidebar = () => {
  return (
    <aside className="admin-sidebar">
      <div className="sidebar-header">
        <h2>Admin Panel</h2>
      </div>
      
      <nav className="sidebar-nav">
        <NavLink to="/admin/dashboard" className="nav-item">
          📊 Dashboard
        </NavLink>
        <NavLink to="/admin/bookings" className="nav-item">
          📋 Bookings
        </NavLink>
        <NavLink to="/admin/users" className="nav-item">
          👥 Users
        </NavLink>
        <NavLink to="/admin/flights" className="nav-item">
          ✈️ Flights
        </NavLink>
        <NavLink to="/admin/statistics" className="nav-item">
          📈 Statistics
        </NavLink>
      </nav>
      
      <div className="sidebar-footer">
        <NavLink to="/" className="back-link">
          ← Back to Site
        </NavLink>
      </div>
    </aside>
  );
};

export default AdminSidebar;
```

### **Step 4: Create AdminDashboard.jsx**

**File**: `frontend/src/pages/admin/AdminDashboard.jsx`

```jsx
import { useState, useEffect } from 'react';
import { adminApi } from '../../services/adminApi';
import StatCard from '../../components/admin/StatCard';
import LoadingSpinner from '../../components/LoadingSpinner';
import { toast } from 'react-toastify';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    fetchDashboardStats();
  }, []);
  
  const fetchDashboardStats = async () => {
    try {
      const response = await adminApi.getDashboardStats();
      setStats(response.data);
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
      toast.error('Failed to load dashboard statistics');
    } finally {
      setLoading(false);
    }
  };
  
  if (loading) {
    return <LoadingSpinner text="Loading dashboard..." fullScreen />;
  }
  
  return (
    <div className="admin-dashboard">
      <h1>Dashboard</h1>
      
      <div className="stats-grid">
        <StatCard
          title="Total Users"
          value={stats?.totalUsers || 0}
          icon="👥"
          color="blue"
        />
        <StatCard
          title="Total Bookings"
          value={stats?.totalBookings || 0}
          icon="📋"
          color="green"
        />
        <StatCard
          title="Total Flights"
          value={stats?.totalFlights || 0}
          icon="✈️"
          color="purple"
        />
        <StatCard
          title="Total Revenue"
          value={`${stats?.totalRevenue?.toLocaleString()} VND`}
          icon="💰"
          color="orange"
        />
        <StatCard
          title="Bookings Today"
          value={stats?.bookingsToday || 0}
          icon="📅"
          color="teal"
        />
        <StatCard
          title="Revenue Today"
          value={`${stats?.revenueToday?.toLocaleString()} VND`}
          icon="💵"
          color="indigo"
        />
      </div>
      
      <div className="dashboard-charts">
        <div className="chart-card">
          <h3>Booking Status Distribution</h3>
          <div className="chart-content">
            <div className="status-item">
              <span className="status-label">Confirmed:</span>
              <span className="status-value">{stats?.confirmedBookings || 0}</span>
            </div>
            <div className="status-item">
              <span className="status-label">Pending:</span>
              <span className="status-value">{stats?.pendingBookings || 0}</span>
            </div>
            <div className="status-item">
              <span className="status-label">Cancelled:</span>
              <span className="status-value">{stats?.cancelledBookings || 0}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
```

### **Step 5: Update App.jsx**

**File**: `frontend/src/App.jsx`

**Add admin routes**:
```jsx
import AdminLayout from './components/admin/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminBookings from './pages/admin/AdminBookings';
import AdminUsers from './pages/admin/AdminUsers';
import AdminFlights from './pages/admin/AdminFlights';

// In <Routes>:
<Route path="/admin" element={<AdminLayout />}>
  <Route path="dashboard" element={<AdminDashboard />} />
  <Route path="bookings" element={<AdminBookings />} />
  <Route path="users" element={<AdminUsers />} />
  <Route path="flights" element={<AdminFlights />} />
  <Route path="statistics" element={<AdminStatistics />} />
</Route>
```

---

## 📊 **TEMPLATES FOR REMAINING PAGES**

### **AdminBookings.jsx Template**

```jsx
const AdminBookings = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  useEffect(() => {
    fetchBookings();
  }, [page]);
  
  const fetchBookings = async () => {
    try {
      const response = await adminApi.getAllBookings({ page, size: 20 });
      setBookings(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      toast.error('Failed to load bookings');
    } finally {
      setLoading(false);
    }
  };
  
  const handleCancelBooking = async (bookingId) => {
    if (!confirm('Are you sure you want to cancel this booking?')) return;
    
    try {
      await adminApi.cancelBooking(bookingId);
      toast.success('Booking cancelled successfully');
      fetchBookings();
    } catch (error) {
      toast.error('Failed to cancel booking');
    }
  };
  
  return (
    <div className="admin-bookings">
      <h1>Manage Bookings</h1>
      
      <table className="data-table">
        <thead>
          <tr>
            <th>Booking Code</th>
            <th>User</th>
            <th>Status</th>
            <th>Amount</th>
            <th>Date</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {bookings.map(booking => (
            <tr key={booking.id}>
              <td>{booking.bookingCode}</td>
              <td>{booking.userId}</td>
              <td><span className={`status-badge ${booking.status}`}>{booking.status}</span></td>
              <td>{booking.totalAmount.toLocaleString()} VND</td>
              <td>{new Date(booking.createdAt).toLocaleDateString()}</td>
              <td>
                <button onClick={() => handleCancelBooking(booking.id)}>Cancel</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      
      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
        <span>Page {page + 1} of {totalPages}</span>
        <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</button>
      </div>
    </div>
  );
};
```

---

## ✅ **IMPLEMENTATION CHECKLIST**

### **Backend** (60%)
- [x] DashboardStats DTO
- [x] AdminService
- [x] AdminController
- [ ] Add missing repository methods
- [ ] Add missing service methods
- [ ] Test admin endpoints

### **Frontend** (0%)
- [ ] Create adminApi.js
- [ ] Create AdminLayout
- [ ] Create AdminSidebar
- [ ] Create AdminDashboard
- [ ] Create AdminBookings
- [ ] Create AdminUsers
- [ ] Create AdminFlights
- [ ] Add CSS styles
- [ ] Update App.jsx routes
- [ ] Test admin panel

---

## 🚀 **QUICK START**

### **1. Complete Backend (30 min)**
- Add missing methods to repositories
- Add missing methods to services
- Test: `GET http://localhost:8080/api/admin/dashboard`

### **2. Create Frontend (60 min)**
- Create adminApi.js
- Create AdminLayout + Sidebar
- Create AdminDashboard
- Test: Login as admin → /admin/dashboard

### **3. Complete Remaining Pages (90 min)**
- AdminBookings
- AdminUsers
- AdminFlights
- AdminStatistics

---

## 🎯 **TESTING**

### **Create Admin User**
```sql
UPDATE auth_user SET role = 'ROLE_ADMIN' WHERE email = 'your@email.com';
```

### **Test Admin API**
```bash
# Login as admin to get token
POST http://localhost:8080/api/auth/login
{
  "email": "admin@test.com",
  "password": "password"
}

# Use token to access admin endpoint
GET http://localhost:8080/api/admin/dashboard
Authorization: Bearer YOUR_TOKEN
```

---

## ✅ **STATUS: FOUNDATION COMPLETE**

**Backend**: 60% Complete (core done, need to add service methods)  
**Frontend**: 0% Complete (templates provided)

**Next**: Implement missing service methods, then create frontend admin panel

---

**Time Estimate**: 3-4 hours total  
**Priority**: Backend methods (1 hour) → Frontend dashboard (1 hour) → Other pages (2 hours)

