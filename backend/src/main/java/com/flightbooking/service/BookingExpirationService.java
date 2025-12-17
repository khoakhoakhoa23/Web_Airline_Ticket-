package com.flightbooking.service;

import com.flightbooking.entity.Booking;
import com.flightbooking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to handle booking expiration logic.
 * Separated from Scheduler for better transaction management and testability.
 */
@Service
public class BookingExpirationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingExpirationService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * Expire all hold bookings that have passed their hold expiration time.
     * This method is transactional - Spring will manage begin/commit/rollback.
     * 
     * @return Number of bookings expired
     */
    @Transactional
    public int expireHoldBookings() {
        LocalDateTime now = LocalDateTime.now();
        
        // Use custom query for better performance instead of findAll()
        List<Booking> expiredBookings = bookingRepository
            .findByStatusInAndHoldExpiresAtBefore(
                List.of("PENDING", "HOLD"), 
                now
            );
        
        if (expiredBookings.isEmpty()) {
            logger.debug("No expired bookings found at {}", now);
            return 0;
        }
        
        logger.info("Found {} expired bookings to process", expiredBookings.size());
        
        // Update all expired bookings
        for (Booking booking : expiredBookings) {
            logger.debug("Expiring booking: {} (code: {}, holdExpiresAt: {})", 
                booking.getId(), booking.getBookingCode(), booking.getHoldExpiresAt());
            
            booking.setStatus("EXPIRED");
            bookingRepository.save(booking);
            
            // TODO: Release seats and baggage
            // TODO: Send notification email to user
        }
        
        logger.info("Successfully expired {} hold bookings", expiredBookings.size());
        return expiredBookings.size();
    }
}

