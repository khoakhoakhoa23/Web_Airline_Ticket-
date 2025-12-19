package com.flightbooking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockSeatRequest {
    
    @NotBlank(message = "Flight number is required")
    private String flightNumber;
    
    @NotBlank(message = "Segment ID is required")
    private String segmentId;
    
    @NotBlank(message = "Seat number is required")
    private String seatNumber;
    
    private String userId; // Optional, for logged-in users
    
    private String sessionId; // Optional, for anonymous users
}

