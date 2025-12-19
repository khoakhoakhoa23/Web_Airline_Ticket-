package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Seat Selection Input DTO
 * Used in CreateBookingRequest to specify which seats are selected for which passengers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelectionInputDTO {
    /**
     * Seat number (e.g., "12A", "6E")
     */
    private String seatNumber;
    
    /**
     * Index of passenger in passengers list (0-based)
     * Used to map seat to passenger
     */
    private Integer passengerIndex;
    
    /**
     * Seat type (WINDOW, AISLE, MIDDLE, EXIT)
     */
    private String seatType;
    
    /**
     * Price for this seat
     */
    private BigDecimal price;
}

