package com.flightbooking.scheduler;

import com.flightbooking.service.BookingExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for periodic tasks related to bookings.
 * 
 * NOTE: Schedulers should NOT be @Transactional.
 * They should delegate to service layer where @Transactional is applied.
 */
@Component
public class HoldBookingScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(HoldBookingScheduler.class);
    
    @Autowired
    private BookingExpirationService bookingExpirationService;
    
    /**
     * Expire hold bookings every minute.
     * 
     * NOTE: No @Transactional here! Transaction is managed in service layer.
     * This prevents "Cannot commit when autoCommit is enabled" error.
     */
    @Scheduled(fixedRate = 60000) // Run every minute (60000ms)
    public void expireHoldBookings() {
        logger.debug("Starting scheduled task: expire hold bookings");
        
        try {
            int expiredCount = bookingExpirationService.expireHoldBookings();
            
            if (expiredCount > 0) {
                logger.info("Scheduled task completed: Expired {} hold bookings", expiredCount);
            }
        } catch (Exception e) {
            logger.error("Error in scheduled task expireHoldBookings", e);
            // Don't rethrow - let scheduler continue
        }
    }
}

