package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedSeatDTO {
    private String seatNumber;
    private String passengerName;
    private String passengerId;
    private String bookingId;
    private String bookingCode;
    private String status; // CONFIRMED, PENDING, etc.
}
