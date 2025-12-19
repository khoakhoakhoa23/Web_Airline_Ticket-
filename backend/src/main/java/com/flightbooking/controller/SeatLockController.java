package com.flightbooking.controller;

import com.flightbooking.dto.LockSeatRequest;
import com.flightbooking.dto.SeatLockDTO;
import com.flightbooking.service.SeatLockService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Seat Lock Controller
 * 
 * Handles seat locking/unlocking during booking process
 */
@RestController
@RequestMapping("/api/seat-selections")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class SeatLockController {
    
    private static final Logger logger = LoggerFactory.getLogger(SeatLockController.class);
    
    @Autowired
    private SeatLockService seatLockService;
    
    /**
     * Lock a seat for 15 minutes
     * POST /api/seat-selections/lock
     */
    @PostMapping("/lock")
    public ResponseEntity<SeatLockDTO> lockSeat(@Valid @RequestBody LockSeatRequest request) {
        logger.info("Locking seat: {} on flight {}", request.getSeatNumber(), request.getFlightNumber());
        
        // Get user ID from JWT token if available
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            request.setUserId(auth.getName());
        }
        
        SeatLockDTO lock = seatLockService.lockSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lock);
    }
    
    /**
     * Unlock a seat
     * POST /api/seat-selections/unlock/{lockId}
     */
    @PostMapping("/unlock/{lockId}")
    public ResponseEntity<Map<String, String>> unlockSeat(@PathVariable String lockId) {
        logger.info("Unlocking seat lock: {}", lockId);
        
        seatLockService.unlockSeat(lockId);
        
        return ResponseEntity.ok(Map.of("message", "Seat unlocked successfully"));
    }
    
    /**
     * Get active locks for current user or session
     * GET /api/seat-selections/locks?sessionId=xxx
     */
    @GetMapping("/locks")
    public ResponseEntity<List<SeatLockDTO>> getActiveLocks(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sessionId) {
        
        // Get user ID from JWT token if available
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            userId = auth.getName();
        }
        
        List<SeatLockDTO> locks = seatLockService.getActiveLocks(userId, sessionId);
        return ResponseEntity.ok(locks);
    }
    
    /**
     * Get active locks for a flight
     * GET /api/seat-selections/locks/flight/{flightNumber}
     */
    @GetMapping("/locks/flight/{flightNumber}")
    public ResponseEntity<List<SeatLockDTO>> getActiveLocksByFlight(
            @PathVariable String flightNumber) {
        
        List<SeatLockDTO> locks = seatLockService.getActiveLocksByFlight(flightNumber);
        return ResponseEntity.ok(locks);
    }
    
    /**
     * Check if seat is available
     * GET /api/seat-selections/check-availability?flightNumber=VN123&segmentId=xxx&seatNumber=12A
     */
    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Object>> checkSeatAvailability(
            @RequestParam String flightNumber,
            @RequestParam String segmentId,
            @RequestParam String seatNumber) {
        
        boolean isAvailable = seatLockService.isSeatAvailable(flightNumber, segmentId, seatNumber);
        
        return ResponseEntity.ok(Map.of(
            "available", isAvailable,
            "flightNumber", flightNumber,
            "segmentId", segmentId,
            "seatNumber", seatNumber
        ));
    }
}

