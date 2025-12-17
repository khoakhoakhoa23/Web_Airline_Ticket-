package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private String id;
    private String bookingCode;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime holdExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;
    private List<FlightSegmentDTO> flightSegments;
    private List<PassengerDTO> passengers;
}

