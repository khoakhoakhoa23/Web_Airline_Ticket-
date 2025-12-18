package com.flightbooking.controller;

import com.flightbooking.dto.BookingDTO;
import com.flightbooking.dto.CreateBookingRequest;
import com.flightbooking.dto.UpdateBookingStatusRequest;
import com.flightbooking.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * Booking Controller
 * 
 * Handles booking-related endpoints with proper:
 * - Authentication/Authorization checks
 * - Validation
 * - Error handling (delegated to GlobalExceptionHandler)
 * - Logging
 * 
 * All endpoints require authentication except where noted
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class BookingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private com.flightbooking.util.JwtUtil jwtUtil;
    
    /**
     * Create new booking
     * POST /api/bookings
     * 
     * ✅ PRODUCTION STANDARD:
     * - userId is extracted from JWT token (SecurityContext), NOT from request body
     * - Requires authentication (JWT token in Authorization header)
     * - Returns 401 if user not authenticated or not found
     * 
     * @param request CreateBookingRequest with flight segments and passengers (NO userId)
     * @return Created booking with 201 status
     */
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        logger.info("🔍 === CREATE BOOKING REQUEST ===");
        
        // ✅ CRITICAL: Get userId from JWT token (SecurityContext)
        // JwtAuthenticationFilter sets principal = userId, so auth.getName() returns userId
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        logger.info("🔍 Step 1: Checking Authentication from SecurityContext...");
        logger.info("   Authentication object: {}", auth != null ? auth.getClass().getName() : "NULL");
        logger.info("   Is authenticated: {}", auth != null ? auth.isAuthenticated() : "NULL");
        logger.info("   Principal: {}", auth != null ? auth.getPrincipal() : "NULL");
        logger.info("   Principal type: {}", auth != null && auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "NULL");
        logger.info("   auth.getName(): {}", auth != null ? auth.getName() : "NULL");
        
        if (auth == null) {
            logger.error("❌ CRITICAL: Authentication is NULL!");
            logger.error("   This means JWT filter did not set authentication or token was invalid");
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                "User not authenticated. Please login to create booking.");
        }
        
        if (!auth.isAuthenticated()) {
            logger.error("❌ CRITICAL: Authentication exists but is NOT authenticated!");
            logger.error("   Principal: {}", auth.getPrincipal());
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                "User not authenticated. Please login to create booking.");
        }
        
        String userId = auth.getName(); // ✅ userId from JWT token (set by JwtAuthenticationFilter as principal)
        
        if (userId == null || userId.trim().isEmpty()) {
            logger.error("❌ CRITICAL: auth.getName() returned null or empty!");
            logger.error("   Principal: {}", auth.getPrincipal());
            logger.error("   Principal type: {}", auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "NULL");
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                "User ID not found in authentication. Please login again.");
        }
        
        logger.info("✅ Step 2: User ID extracted from JWT token: {}", userId);
        logger.info("   User ID type: {}", userId.getClass().getName());
        logger.info("   User ID length: {}", userId.length());
        
        // ✅ Verify userId is a valid UUID format (optional but helpful for debugging)
        try {
            java.util.UUID.fromString(userId);
            logger.info("   ✅ User ID is valid UUID format");
        } catch (IllegalArgumentException e) {
            logger.warn("   ⚠️ User ID is not a valid UUID format: {}", userId);
        }
        
        // ✅ Get email from JWT token for auto-create user if needed
        String email = null;
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                jakarta.servlet.http.HttpServletRequest httpRequest = attributes.getRequest();
                String authHeader = httpRequest.getHeader("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    email = jwtUtil.extractEmail(token);
                    logger.info("✅ Extracted email from JWT token: {}", email);
                } else {
                    logger.warn("⚠️ Authorization header not found or invalid format");
                }
            } else {
                logger.warn("⚠️ RequestContextHolder attributes is null");
            }
        } catch (Exception e) {
            logger.error("❌ Failed to extract email from JWT token: {}", e.getMessage(), e);
        }
        
        logger.info("Creating booking - User ID: {}, Email: {}", userId, email != null ? email : "NULL");
        
        // Service will throw exception if validation fails
        // GlobalExceptionHandler will catch and return proper error response
        BookingDTO booking = bookingService.createBooking(request, userId, email);
        
        logger.info("Booking created successfully: {} (code: {})", booking.getId(), booking.getBookingCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }
    
    /**
     * Get booking by ID
     * GET /api/bookings/{id}
     * 
     * Checks ownership: User can only view their own bookings
     * 
     * @param id Booking ID
     * @return Booking details
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable String id) {
        logger.info("Fetching booking by ID: {}", id);
        
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth != null ? auth.getName() : null;
        
        // Service will check ownership and throw exception if unauthorized
        BookingDTO booking = bookingService.getBookingById(id, currentUserEmail);
        
        logger.info("Booking retrieved: {}", id);
        return ResponseEntity.ok(booking);
    }
    
    /**
     * Get booking by booking code
     * GET /api/bookings/code/{bookingCode}
     * 
     * Checks ownership: User can only view their own bookings
     * 
     * @param bookingCode Booking code (e.g., BK1234567890)
     * @return Booking details
     */
    @GetMapping("/code/{bookingCode}")
    public ResponseEntity<BookingDTO> getBookingByCode(@PathVariable String bookingCode) {
        logger.info("Fetching booking by code: {}", bookingCode);
        
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth != null ? auth.getName() : null;
        
        // Service will check ownership and throw exception if unauthorized
        BookingDTO booking = bookingService.getBookingByCode(bookingCode, currentUserEmail);
        
        logger.info("Booking retrieved by code: {}", bookingCode);
        return ResponseEntity.ok(booking);
    }
    
    /**
     * Get all bookings for a user
     * GET /api/bookings/user/{userId}
     * 
     * Checks ownership: User can only view their own bookings
     * 
     * @param userId User ID
     * @return List of bookings
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(@PathVariable String userId) {
        logger.info("Fetching bookings for user: {}", userId);
        
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth != null ? auth.getName() : null;
        
        // Service will check ownership and throw exception if unauthorized
        List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId, currentUserEmail);
        
        logger.info("Found {} bookings for user: {}", bookings.size(), userId);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Update booking status
     * PUT /api/bookings/{id}/status
     * 
     * Checks ownership: User can only update their own bookings
     * Business rules: Only certain status transitions are allowed
     * 
     * @param id Booking ID
     * @param request Status update request
     * @return Updated booking
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<BookingDTO> updateBookingStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        logger.info("Updating booking {} status to: {}", id, request.getStatus());
        
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth != null ? auth.getName() : null;
        
        // Service will validate status transition and check ownership
        BookingDTO booking = bookingService.updateBookingStatus(id, request.getStatus(), currentUserEmail);
        
        logger.info("Booking {} status updated to: {}", id, request.getStatus());
        return ResponseEntity.ok(booking);
    }
    
    /**
     * Cancel booking
     * DELETE /api/bookings/{id}
     * 
     * Checks ownership: User can only cancel their own bookings
     * Business rules: Cannot cancel already confirmed/completed bookings
     * 
     * @param id Booking ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable String id) {
        logger.info("Cancelling booking: {}", id);
        
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth != null ? auth.getName() : null;
        
        // Service will check ownership and business rules
        bookingService.cancelBooking(id, currentUserEmail);
        
        logger.info("Booking cancelled: {}", id);
        return ResponseEntity.noContent().build();
    }
}

