package com.flightbooking.service;

import com.flightbooking.dto.DashboardStats;
import com.flightbooking.entity.Booking;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.FlightRepository;
import com.flightbooking.repository.PaymentRepository;
import com.flightbooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Admin Service
 * 
 * Handles admin-specific operations:
 * - Dashboard statistics
 * - System-wide queries
 * - Reports
 */
@Service
public class AdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    /**
     * Get dashboard statistics
     * 
     * @return Dashboard stats with all key metrics
     */
    public DashboardStats getDashboardStats() {
        logger.info("Fetching dashboard statistics");
        
        // Total counts
        Long totalUsers = userRepository.count();
        Long totalBookings = bookingRepository.count();
        Long totalFlights = flightRepository.count();
        
        // Total revenue (sum of all successful payments)
        BigDecimal totalRevenue = calculateTotalRevenue();
        
        // Today's stats
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        Long bookingsToday = bookingRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        BigDecimal revenueToday = calculateRevenueBetween(startOfDay, endOfDay);
        
        // Bookings by status
        Long confirmedBookings = bookingRepository.countByStatus("CONFIRMED");
        Long pendingBookings = bookingRepository.countByStatus("PENDING_PAYMENT");
        Long cancelledBookings = bookingRepository.countByStatus("CANCELLED");
        
        // Active users (could be enhanced with last login tracking)
        Long activeUsers = userRepository.countByStatus("ACTIVE");
        
        return DashboardStats.builder()
                .totalUsers(totalUsers)
                .totalBookings(totalBookings)
                .totalFlights(totalFlights)
                .totalRevenue(totalRevenue)
                .bookingsToday(bookingsToday)
                .revenueToday(revenueToday)
                .confirmedBookings(confirmedBookings)
                .pendingBookings(pendingBookings)
                .cancelledBookings(cancelledBookings)
                .activeUsers(activeUsers != null ? activeUsers : totalUsers)
                .build();
    }
    
    /**
     * Calculate total revenue from all confirmed bookings
     * Uses booking.totalAmount to ensure accuracy and avoid double counting
     * Only counts bookings with status CONFIRMED (which means payment was successful)
     */
    private BigDecimal calculateTotalRevenue() {
        try {
            List<Booking> confirmedBookings = bookingRepository.findByStatus("CONFIRMED");
            logger.debug("Found {} confirmed bookings for revenue calculation", confirmedBookings.size());
            
            BigDecimal total = confirmedBookings.stream()
                    .filter(booking -> booking.getTotalAmount() != null)
                    .map(Booking::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            logger.info("Total revenue calculated: {} VND", total);
            return total;
        } catch (Exception e) {
            logger.error("Error calculating total revenue", e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate revenue between two dates
     * Uses confirmed bookings that were confirmed (updatedAt) within the date range
     * Only counts bookings with status CONFIRMED
     * 
     * Note: We use updatedAt to determine when booking was confirmed (after payment)
     * This is more accurate than createdAt which is when booking was initially created
     */
    private BigDecimal calculateRevenueBetween(LocalDateTime start, LocalDateTime end) {
        try {
            // Get all confirmed bookings
            List<Booking> confirmedBookings = bookingRepository.findByStatus("CONFIRMED");
            
            // Filter by date range - use updatedAt (when booking was confirmed) for accuracy
            BigDecimal revenue = confirmedBookings.stream()
                    .filter(booking -> {
                        // Use updatedAt to determine when booking was confirmed
                        // If updatedAt is null, fallback to createdAt
                        LocalDateTime checkDate = booking.getUpdatedAt() != null 
                            ? booking.getUpdatedAt() 
                            : booking.getCreatedAt();
                        
                        if (checkDate == null) {
                            return false;
                        }
                        
                        // Check if booking was confirmed within the date range (inclusive)
                        // Include bookings confirmed at start or end time
                        return (checkDate.isEqual(start) || checkDate.isAfter(start)) 
                            && (checkDate.isEqual(end) || checkDate.isBefore(end));
                    })
                    .filter(booking -> booking.getTotalAmount() != null)
                    .map(Booking::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long count = confirmedBookings.stream()
                    .filter(booking -> {
                        LocalDateTime checkDate = booking.getUpdatedAt() != null 
                            ? booking.getUpdatedAt() 
                            : booking.getCreatedAt();
                        return checkDate != null 
                            && (checkDate.isEqual(start) || checkDate.isAfter(start)) 
                            && (checkDate.isEqual(end) || checkDate.isBefore(end));
                    })
                    .count();
            
            logger.info("Revenue between {} and {}: {} VND (from {} confirmed bookings)", 
                start, end, revenue, count);
            return revenue;
        } catch (Exception e) {
            logger.error("Error calculating revenue between dates", e);
            return BigDecimal.ZERO;
        }
    }
}
