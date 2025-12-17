package com.flightbooking.service;

import com.flightbooking.dto.DashboardStats;
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
     * Calculate total revenue from all successful payments
     */
    private BigDecimal calculateTotalRevenue() {
        try {
            return paymentRepository.findByStatus("SUCCESS")
                    .stream()
                    .map(payment -> payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            logger.error("Error calculating total revenue", e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate revenue between two dates
     */
    private BigDecimal calculateRevenueBetween(LocalDateTime start, LocalDateTime end) {
        try {
            return paymentRepository.findByStatus("SUCCESS")
                    .stream()
                    .filter(payment -> payment.getCreatedAt() != null &&
                            payment.getCreatedAt().isAfter(start) &&
                            payment.getCreatedAt().isBefore(end))
                    .map(payment -> payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            logger.error("Error calculating revenue between dates", e);
            return BigDecimal.ZERO;
        }
    }
}
