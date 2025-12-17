package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "seat_selections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelection {
    @Id
    private String id;
    
    @Column(name = "passenger_id")
    private String passengerId;
    
    @Column(name = "seat_number")
    private String seatNumber;
    
    @Column(name = "seat_type")
    private String seatType;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "segment_id")
    private String segmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", insertable = false, updatable = false)
    private Passenger passenger;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", insertable = false, updatable = false)
    private FlightSegment segment;
}

