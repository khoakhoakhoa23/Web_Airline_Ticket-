package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {
    @Id
    private String id;
    
    @Column(name = "flight_number", nullable = false)
    private String flightNumber;
    
    @Column(nullable = false)
    private String airline;
    
    @Column(nullable = false, length = 10)
    private String origin;
    
    @Column(nullable = false, length = 10)
    private String destination;
    
    @Column(name = "depart_time", nullable = false)
    private LocalDateTime departTime;
    
    @Column(name = "arrive_time", nullable = false)
    private LocalDateTime arriveTime;
    
    @Column(name = "cabin_class", nullable = false)
    private String cabinClass;
    
    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal taxes;
    
    @Column(name = "available_seats")
    private Integer availableSeats;
    
    @Column(name = "total_seats")
    private Integer totalSeats;
    
    private String status; // SCHEDULED, DELAYED, CANCELLED, COMPLETED
    
    @Column(name = "aircraft_type")
    private String aircraftType;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

