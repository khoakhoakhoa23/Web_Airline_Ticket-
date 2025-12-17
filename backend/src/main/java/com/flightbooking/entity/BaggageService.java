package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "baggage_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaggageService {
    @Id
    private String id;
    
    @Column(name = "passenger_id")
    private String passengerId;
    
    @Column(name = "segment_id")
    private String segmentId;
    
    @Column(name = "weight_kg")
    private Integer weightKg;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", insertable = false, updatable = false)
    private Passenger passenger;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", insertable = false, updatable = false)
    private FlightSegment segment;
}

