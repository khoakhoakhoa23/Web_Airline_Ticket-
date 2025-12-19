package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDTO {
    private String id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String documentType;
    private String documentNumber;
    private String bookingId;
    /**
     * Seat number assigned to this passenger (e.g., "6E", "12A")
     * This is populated from seat_selections table when loading booking
     */
    private String seatNumber;
}

