package com.flightbooking.controller;

import com.flightbooking.dto.BookingDTO;
import com.flightbooking.dto.DashboardStats;
import com.flightbooking.dto.FlightDTO;
import com.flightbooking.dto.UserDTO;
import com.flightbooking.service.AdminService;
import com.flightbooking.service.BookingService;
import com.flightbooking.service.FlightService;
import com.flightbooking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin Controller
 * 
 * Handles all admin-specific operations
 * All endpoints require ROLE_ADMIN
 * 
 * Base path: /api/admin
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FlightService flightService;
    
    /**
     * Get dashboard statistics
     * GET /api/admin/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        logger.info("Admin: Fetching dashboard statistics");
        DashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get all bookings (with pagination and filters)
     * GET /api/admin/bookings?page=0&size=20&status=CONFIRMED
     */
    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingDTO>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        
        logger.info("Admin: Fetching all bookings - page: {}, size: {}, status: {}", page, size, status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        
        Page<BookingDTO> bookings;
        if (status != null && !status.isEmpty()) {
            bookings = bookingService.getBookingsByStatus(status, pageable);
        } else {
            bookings = bookingService.getAllBookings(pageable);
        }
        
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Get booking by ID (admin can see any booking)
     * GET /api/admin/bookings/{id}
     */
    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable String id) {
        logger.info("Admin: Fetching booking: {}", id);
        BookingDTO booking = bookingService.getAdminBookingById(id);
        return ResponseEntity.ok(booking);
    }
    
    /**
     * Approve booking (admin only)
     * PUT /api/admin/bookings/{id}/approve
     * 
     * Approves a booking by changing status to CONFIRMED
     * Can approve from PENDING or PENDING_PAYMENT status
     */
    @PutMapping("/bookings/{id}/approve")
    public ResponseEntity<Map<String, String>> approveBooking(@PathVariable String id) {
        logger.info("Admin: Approving booking: {}", id);
        BookingDTO booking = bookingService.adminApproveBooking(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Booking approved successfully");
        response.put("bookingId", id);
        response.put("status", booking.getStatus());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel booking (admin override)
     * PUT /api/admin/bookings/{id}/cancel
     */
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable String id) {
        logger.info("Admin: Cancelling booking: {}", id);
        bookingService.adminCancelBooking(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Booking cancelled successfully");
        response.put("bookingId", id);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all users (with pagination)
     * GET /api/admin/users?page=0&size=20
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Admin: Fetching all users - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserDTO> users = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String id) {
        logger.info("Admin: Fetching user: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Update user role
     * PUT /api/admin/users/{id}/role
     * Body: { "role": "ROLE_ADMIN" }
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, String>> updateUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        
        String newRole = request.get("role");
        logger.info("Admin: Updating user {} role to {}", id, newRole);
        
        userService.updateUserRole(id, newRole);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User role updated successfully");
        response.put("userId", id);
        response.put("newRole", newRole);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Disable/Enable user
     * PUT /api/admin/users/{id}/status
     * Body: { "status": "INACTIVE" or "ACTIVE" }
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<Map<String, String>> updateUserStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        
        String newStatus = request.get("status");
        logger.info("Admin: Updating user {} status to {}", id, newStatus);
        
        userService.updateUserStatus(id, newStatus);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User status updated successfully");
        response.put("userId", id);
        response.put("newStatus", newStatus);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all flights (admin view - all flights)
     * GET /api/admin/flights?page=0&size=20
     */
    @GetMapping("/flights")
    public ResponseEntity<Page<FlightDTO>> getAllFlights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Admin: Fetching all flights - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("departTime").descending());
        Page<FlightDTO> flights = flightService.getAllFlightsPaged(pageable);
        
        return ResponseEntity.ok(flights);
    }
    
    /**
     * Create new flight
     * POST /api/admin/flights
     */
    @PostMapping("/flights")
    public ResponseEntity<FlightDTO> createFlight(@RequestBody FlightDTO flightDTO) {
        logger.info("Admin: Creating new flight");
        FlightDTO created = flightService.createFlight(flightDTO);
        return ResponseEntity.ok(created);
    }
    
    /**
     * Update flight
     * PUT /api/admin/flights/{id}
     */
    @PutMapping("/flights/{id}")
    public ResponseEntity<FlightDTO> updateFlight(
            @PathVariable String id,
            @RequestBody FlightDTO flightDTO) {
        
        logger.info("Admin: Updating flight: {}", id);
        FlightDTO updated = flightService.updateFlight(id, flightDTO);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Delete flight
     * DELETE /api/admin/flights/{id}
     */
    @DeleteMapping("/flights/{id}")
    public ResponseEntity<Map<String, String>> deleteFlight(@PathVariable String id) {
        logger.info("Admin: Deleting flight: {}", id);
        flightService.deleteFlight(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Flight deleted successfully");
        response.put("flightId", id);
        
        return ResponseEntity.ok(response);
    }
}
