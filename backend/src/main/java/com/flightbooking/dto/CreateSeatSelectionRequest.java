package com.flightbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeatSelectionRequest {
    @NotBlank(message = "Passenger ID is required")
    private String passengerId;
    
    @NotBlank(message = "Segment ID is required")
    private String segmentId;
    
    @NotBlank(message = "Seat number is required")
    private String seatNumber;
    
    @NotBlank(message = "Seat type is required")
    private String seatType;
    
    @NotNull(message = "Price is required")
    private BigDecimal price;
}

