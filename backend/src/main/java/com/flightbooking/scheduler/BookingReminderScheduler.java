package com.flightbooking.scheduler;

import com.flightbooking.entity.Booking;
import com.flightbooking.entity.FlightSegment;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Booking Reminder Scheduler
 * 
 * Sends reminder emails to customers:
 * - 24 hours before flight departure
 * 
 * Runs every hour to check for bookings that need reminders
 */
@Component
public class BookingReminderScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingReminderScheduler.class);
    
    // Track bookings that already received reminders (to avoid duplicate emails)
    private final Set<String> remindersSent = new HashSet<>();
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Send booking reminders for flights departing in 24 hours
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void sendBookingReminders() {
        try {
            logger.info("Starting booking reminder task");
            
            // Calculate time window: 23-25 hours from now
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderWindowStart = now.plusHours(23);
            LocalDateTime reminderWindowEnd = now.plusHours(25);
            
            logger.debug("Checking for flights departing between {} and {}", 
                    reminderWindowStart, reminderWindowEnd);
            
            // Get all confirmed bookings
            List<Booking> confirmedBookings = bookingRepository.findByStatus("CONFIRMED");
            
            int remindersSentCount = 0;
            
            for (Booking booking : confirmedBookings) {
                // Check if reminder already sent
                if (remindersSent.contains(booking.getId())) {
                    continue;
                }
                
                // Check if booking has flight segments
                if (booking.getFlightSegments() == null || booking.getFlightSegments().isEmpty()) {
                    continue;
                }
                
                // Get first flight departure time
                FlightSegment firstFlight = booking.getFlightSegments().get(0);
                LocalDateTime departureTime = firstFlight.getDepartTime();
                
                // Check if departure time is within reminder window
                if (departureTime.isAfter(reminderWindowStart) && departureTime.isBefore(reminderWindowEnd)) {
                    // Send reminder email
                    try {
                        notificationService.sendBookingReminderEmail(booking.getId());
                        remindersSent.add(booking.getId());
                        remindersSentCount++;
                        logger.info("Reminder sent for booking: {}, flight departure: {}", 
                                booking.getBookingCode(), departureTime);
                    } catch (Exception e) {
                        logger.error("Failed to send reminder for booking: {}, error: {}", 
                                booking.getId(), e.getMessage());
                    }
                }
            }
            
            logger.info("Booking reminder task completed. Sent {} reminders.", remindersSentCount);
            
            // Clean up old entries from remindersSent set (bookings more than 48 hours old)
            cleanupOldReminders();
            
        } catch (Exception e) {
            logger.error("Error in booking reminder task: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Optional: Send reminders 3 hours before departure
     * Uncomment to enable
     */
    // @Scheduled(cron = "0 0 * * * *") // Every hour
    public void sendUrgentReminders() {
        try {
            logger.info("Starting urgent booking reminder task (3 hours before)");
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime urgentWindowStart = now.plusHours(2).plusMinutes(30);
            LocalDateTime urgentWindowEnd = now.plusHours(3).plusMinutes(30);
            
            List<Booking> confirmedBookings = bookingRepository.findByStatus("CONFIRMED");
            
            int urgentRemindersSent = 0;
            
            for (Booking booking : confirmedBookings) {
                String urgentKey = booking.getId() + "_urgent";
                if (remindersSent.contains(urgentKey)) {
                    continue;
                }
                
                if (booking.getFlightSegments() == null || booking.getFlightSegments().isEmpty()) {
                    continue;
                }
                
                FlightSegment firstFlight = booking.getFlightSegments().get(0);
                LocalDateTime departureTime = firstFlight.getDepartTime();
                
                if (departureTime.isAfter(urgentWindowStart) && departureTime.isBefore(urgentWindowEnd)) {
                    try {
                        // You could create a separate urgent reminder template
                        notificationService.sendBookingReminderEmail(booking.getId());
                        remindersSent.add(urgentKey);
                        urgentRemindersSent++;
                        logger.info("Urgent reminder sent for booking: {}", booking.getBookingCode());
                    } catch (Exception e) {
                        logger.error("Failed to send urgent reminder for booking: {}", booking.getId());
                    }
                }
            }
            
            logger.info("Urgent reminder task completed. Sent {} urgent reminders.", urgentRemindersSent);
            
        } catch (Exception e) {
            logger.error("Error in urgent reminder task: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up reminders set to prevent memory issues
     * Remove bookings older than 48 hours
     */
    private void cleanupOldReminders() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(48);
            List<Booking> oldBookings = bookingRepository.findByStatus("CONFIRMED");
            
            int removedCount = 0;
            for (Booking booking : oldBookings) {
                if (booking.getFlightSegments() != null && !booking.getFlightSegments().isEmpty()) {
                    FlightSegment firstFlight = booking.getFlightSegments().get(0);
                    if (firstFlight.getDepartTime().isBefore(cutoffTime)) {
                        remindersSent.remove(booking.getId());
                        remindersSent.remove(booking.getId() + "_urgent");
                        removedCount++;
                    }
                }
            }
            
            if (removedCount > 0) {
                logger.debug("Cleaned up {} old reminder entries", removedCount);
            }
        } catch (Exception e) {
            logger.error("Error cleaning up old reminders: {}", e.getMessage());
        }
    }
}

