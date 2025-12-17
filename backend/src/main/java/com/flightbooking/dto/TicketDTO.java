package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {
    private String id;
    private String bookingId;
    private String pnr;
    private String eticketNumber;
    private String status;
    private LocalDateTime issuedAt;
}

