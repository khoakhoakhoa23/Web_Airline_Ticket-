package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSegmentDTO {
    private String id;
    private String airline;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departTime;
    private LocalDateTime arriveTime;
    private String cabinClass;
    private BigDecimal baseFare;
    private BigDecimal taxes;
    private String bookingId;
}

