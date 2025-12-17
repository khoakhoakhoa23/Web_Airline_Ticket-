package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dashboard Statistics DTO
 * 
 * Contains system-wide statistics for admin dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    
    /**
     * Total number of users in system
     */
    private Long totalUsers;
    
    /**
     * Total number of bookings
     */
    private Long totalBookings;
    
    /**
     * Total number of flights
     */
    private Long totalFlights;
    
    /**
     * Total revenue (all time)
     */
    private BigDecimal totalRevenue;
    
    /**
     * Number of bookings today
     */
    private Long bookingsToday;
    
    /**
     * Revenue today
     */
    private BigDecimal revenueToday;
    
    /**
     * Number of confirmed bookings
     */
    private Long confirmedBookings;
    
    /**
     * Number of pending bookings
     */
    private Long pendingBookings;
    
    /**
     * Number of cancelled bookings
     */
    private Long cancelledBookings;
    
    /**
     * Number of active users
     */
    private Long activeUsers;
}

