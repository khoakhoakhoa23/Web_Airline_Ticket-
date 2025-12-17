package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private String id;
    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    private LocalDateTime departTime;
    private LocalDateTime arriveTime;
    private String cabinClass;
    private BigDecimal baseFare;
    private BigDecimal taxes;
    private BigDecimal totalPrice; // baseFare + taxes
    private Integer availableSeats;
    private Integer totalSeats;
    private String status;
    private String aircraftType;
    private Integer durationMinutes;
}

