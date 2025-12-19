package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelectionDTO {
    private String id;
    private String bookingId;
    private String passengerId;
    private String segmentId;
    private String seatNumber;
    private String seatType;
    private BigDecimal price;
    private String status; // RESERVED, CONFIRMED, CANCELLED
}

