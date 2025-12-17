package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    private String userId;
    private List<FlightSegmentDTO> flightSegments;
    private List<PassengerDTO> passengers;
    private String currency;
}

