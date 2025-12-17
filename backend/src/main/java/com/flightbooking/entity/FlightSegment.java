package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flight_segments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSegment {
    @Id
    private String id;
    
    private String airline;
    
    @Column(name = "flight_number")
    private String flightNumber;
    
    private String origin;
    
    private String destination;
    
    @Column(name = "depart_time")
    private LocalDateTime departTime;
    
    @Column(name = "arrive_time")
    private LocalDateTime arriveTime;
    
    @Column(name = "cabin_class")
    private String cabinClass;
    
    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal taxes;
    
    @Column(name = "booking_id")
    private String bookingId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
    
    @OneToMany(mappedBy = "segment", cascade = CascadeType.ALL)
    private List<SeatSelection> seatSelections;
    
    @OneToMany(mappedBy = "segment", cascade = CascadeType.ALL)
    private List<BaggageService> baggageServices;
}

