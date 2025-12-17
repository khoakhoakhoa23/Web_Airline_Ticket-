package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    private String id;
    
    @Column(name = "booking_id")
    private String bookingId;
    
    @Column(unique = true)
    private String pnr;
    
    @Column(name = "eticket_number", unique = true)
    private String eticketNumber;
    
    private String status;
    
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
}

