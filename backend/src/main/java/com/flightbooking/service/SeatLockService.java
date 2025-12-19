package com.flightbooking.service;

import com.flightbooking.dto.LockSeatRequest;
import com.flightbooking.dto.SeatLockDTO;
import com.flightbooking.entity.SeatLock;
import com.flightbooking.exception.BusinessException;
import com.flightbooking.repository.SeatLockRepository;
import com.flightbooking.repository.SeatSelectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Seat Lock Service
 * 
 * Manages temporary seat locks during booking process
 * Prevents race conditions when multiple users select the same seat
 */
@Service
public class SeatLockService {
    
    private static final Logger logger = LoggerFactory.getLogger(SeatLockService.class);
    
    private static final int LOCK_DURATION_MINUTES = 15;
    
    @Autowired
    private SeatLockRepository seatLockRepository;
    
    @Autowired
    private SeatSelectionRepository seatSelectionRepository;
    
    /**
     * Lock a seat for a specific duration (15 minutes)
     * 
     * Uses SERIALIZABLE isolation to prevent race conditions
     * 
     * @param request Lock seat request
     * @return SeatLockDTO
     * @throws BusinessException if seat is already locked or booked
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public SeatLockDTO lockSeat(LockSeatRequest request) {
        logger.info("Locking seat: {} on flight {}", request.getSeatNumber(), request.getFlightNumber());
        
        // Check if seat is already booked (confirmed seat selection)
        boolean isBooked = seatSelectionRepository.existsBySegmentIdAndSeatNumberAndStatus(
            request.getSegmentId(), 
            request.getSeatNumber(), 
            "CONFIRMED"
        );
        
        if (isBooked) {
            throw new BusinessException("SEAT_ALREADY_BOOKED", 
                "Seat " + request.getSeatNumber() + " is already booked");
        }
        
        // Check if seat is already locked (active lock)
        LocalDateTime now = LocalDateTime.now();
        Optional<SeatLock> existingLock = seatLockRepository.findActiveLock(
            request.getFlightNumber(),
            request.getSeatNumber(),
            now
        );
        
        if (existingLock.isPresent()) {
            SeatLock lock = existingLock.get();
            // Check if it's the same user trying to re-lock
            String lockUserId = lock.getUserId();
            String requestUserId = request.getUserId();
            if (requestUserId != null && requestUserId.equals(lockUserId)) {
                // Extend lock duration
                lock.setExpiresAt(now.plusMinutes(LOCK_DURATION_MINUTES));
                lock = seatLockRepository.save(lock);
                logger.info("Extended lock for seat {} by user {}", request.getSeatNumber(), request.getUserId());
                return convertToDTO(lock);
            } else {
                throw new BusinessException("SEAT_ALREADY_LOCKED", 
                    "Seat " + request.getSeatNumber() + " is currently locked by another user");
            }
        }
        
        // Create new lock
        SeatLock lock = new SeatLock();
        lock.setId(UUID.randomUUID().toString());
        lock.setFlightNumber(request.getFlightNumber());
        lock.setSegmentId(request.getSegmentId());
        lock.setSeatNumber(request.getSeatNumber());
        lock.setUserId(request.getUserId());
        lock.setSessionId(request.getSessionId());
        lock.setLockedAt(now);
        lock.setExpiresAt(now.plusMinutes(LOCK_DURATION_MINUTES));
        lock.setStatus("LOCKED");
        
        lock = seatLockRepository.save(lock);
        logger.info("Seat {} locked successfully until {}", request.getSeatNumber(), lock.getExpiresAt());
        
        return convertToDTO(lock);
    }
    
    /**
     * Unlock a seat
     * 
     * @param lockId Lock ID
     */
    @Transactional
    public void unlockSeat(String lockId) {
        logger.info("Unlocking seat lock: {}", lockId);
        
        SeatLock lock = seatLockRepository.findById(lockId)
            .orElseThrow(() -> new BusinessException("LOCK_NOT_FOUND", "Seat lock not found"));
        
        String lockStatus = lock.getStatus();
        if (lockStatus == null || !"LOCKED".equals(lockStatus)) {
            throw new BusinessException("LOCK_NOT_ACTIVE", "Seat lock is not active");
        }
        
        lock.setStatus("RELEASED");
        seatLockRepository.save(lock);
        
        logger.info("Seat lock {} released", lockId);
    }
    
    /**
     * Get active locks for a user or session
     * 
     * @param userId User ID (optional)
     * @param sessionId Session ID (optional)
     * @return List of active locks
     */
    @Transactional(readOnly = true)
    public List<SeatLockDTO> getActiveLocks(String userId, String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        List<SeatLock> locks;
        
        if (userId != null) {
            locks = seatLockRepository.findByUserIdAndStatus(userId, "LOCKED");
        } else if (sessionId != null) {
            locks = seatLockRepository.findBySessionIdAndStatus(sessionId, "LOCKED");
        } else {
            return List.of();
        }
        
        // Filter out expired locks
        return locks.stream()
            .filter(lock -> lock.getExpiresAt().isAfter(now))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get active locks for a flight
     * 
     * @param flightNumber Flight number
     * @return List of active locks
     */
    @Transactional(readOnly = true)
    public List<SeatLockDTO> getActiveLocksByFlight(String flightNumber) {
        LocalDateTime now = LocalDateTime.now();
        List<SeatLock> locks = seatLockRepository.findActiveLocksByFlight(flightNumber, now);
        return locks.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Confirm locks for a booking (after payment success)
     * 
     * @param bookingId Booking ID
     */
    @Transactional
    public void confirmLocksForBooking(String bookingId) {
        logger.info("Confirming locks for booking: {}", bookingId);
        
        List<SeatLock> locks = seatLockRepository.findByBookingId(bookingId);
        int confirmedCount = 0;
        
        for (SeatLock lock : locks) {
            if ("LOCKED".equals(lock.getStatus())) {
                lock.setStatus("CONFIRMED");
                seatLockRepository.save(lock);
                confirmedCount++;
            }
        }
        
        logger.info("Confirmed {} locks for booking {}", confirmedCount, bookingId);
    }
    
    /**
     * Release locks for a booking (when booking is cancelled or expired)
     * 
     * @param bookingId Booking ID
     */
    @Transactional
    public void releaseLocksForBooking(String bookingId) {
        logger.info("Releasing locks for booking: {}", bookingId);
        
        List<SeatLock> locks = seatLockRepository.findByBookingId(bookingId);
        int releasedCount = 0;
        
        for (SeatLock lock : locks) {
            if ("LOCKED".equals(lock.getStatus()) || "RESERVED".equals(lock.getStatus())) {
                lock.setStatus("RELEASED");
                seatLockRepository.save(lock);
                releasedCount++;
            }
        }
        
        logger.info("Released {} locks for booking {}", releasedCount, bookingId);
    }
    
    /**
     * Cleanup expired locks
     * Scheduled job runs every minute
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void cleanupExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();
        List<SeatLock> expiredLocks = seatLockRepository.findExpiredLocks(now);
        
        if (expiredLocks.isEmpty()) {
            return;
        }
        
        logger.info("Cleaning up {} expired seat locks", expiredLocks.size());
        
        for (SeatLock lock : expiredLocks) {
            lock.setStatus("RELEASED");
            seatLockRepository.save(lock);
            
            // If booking exists and is still PENDING, expire it
            if (lock.getBookingId() != null) {
                // This will be handled by BookingExpirationService
                logger.debug("Lock {} expired for booking {}", lock.getId(), lock.getBookingId());
            }
        }
        
        logger.info("Cleaned up {} expired locks", expiredLocks.size());
    }
    
    /**
     * Check if seat is available (not locked and not booked)
     * 
     * @param flightNumber Flight number
     * @param segmentId Segment ID
     * @param seatNumber Seat number
     * @return true if available
     */
    @Transactional(readOnly = true)
    public boolean isSeatAvailable(String flightNumber, String segmentId, String seatNumber) {
        // Check if booked
        boolean isBooked = seatSelectionRepository.existsBySegmentIdAndSeatNumberAndStatus(
            segmentId, seatNumber, "CONFIRMED"
        );
        
        if (isBooked) {
            return false;
        }
        
        // Check if locked
        LocalDateTime now = LocalDateTime.now();
        Optional<SeatLock> activeLock = seatLockRepository.findActiveLock(
            flightNumber, seatNumber, now
        );
        
        return activeLock.isEmpty();
    }
    
    /**
     * Convert entity to DTO
     */
    private SeatLockDTO convertToDTO(SeatLock lock) {
        return SeatLockDTO.builder()
            .id(lock.getId())
            .flightNumber(lock.getFlightNumber())
            .segmentId(lock.getSegmentId())
            .seatNumber(lock.getSeatNumber())
            .userId(lock.getUserId())
            .sessionId(lock.getSessionId())
            .lockedAt(lock.getLockedAt())
            .expiresAt(lock.getExpiresAt())
            .status(lock.getStatus())
            .bookingId(lock.getBookingId())
            .createdAt(lock.getCreatedAt())
            .updatedAt(lock.getUpdatedAt())
            .expired(lock.isExpired())
            .active(lock.isActive())
            .build();
    }
}

