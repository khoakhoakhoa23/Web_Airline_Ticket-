package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "seat_selections",
       indexes = {
           @Index(name = "idx_seat_selections_booking_id", columnList = "booking_id"),
           @Index(name = "idx_seat_selections_segment_seat", columnList = "segment_id,seat_number"),
           @Index(name = "idx_seat_selections_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelection {
    @Id
    private String id;
    
    @Column(name = "booking_id", nullable = false)
    private String bookingId;
    
    @Column(name = "passenger_id", nullable = false)
    private String passengerId;
    
    @Column(name = "segment_id", nullable = false)
    private String segmentId;
    
    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;
    
    @Column(name = "seat_type", length = 20)
    private String seatType; // WINDOW, AISLE, MIDDLE, EXIT
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // RESERVED, CONFIRMED, CANCELLED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", insertable = false, updatable = false)
    private Passenger passenger;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", insertable = false, updatable = false)
    private FlightSegment segment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
}

