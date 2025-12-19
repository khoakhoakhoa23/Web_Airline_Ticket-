package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    // ✅ userId removed - Backend will extract from JWT token (SecurityContext)
    private List<FlightSegmentDTO> flightSegments;
    private List<PassengerDTO> passengers;
    private String currency;
    private java.math.BigDecimal seatPrice; // Phí chọn ghế
    
    /**
     * Seat selections - which seats are selected for which passengers
     * Each entry maps a seat to a passenger by passengerIndex
     */
    private List<SeatSelectionInputDTO> seatSelections;
}

