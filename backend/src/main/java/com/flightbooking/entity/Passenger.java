package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "passengers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {
    @Id
    private String id;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    private String gender;
    
    @Column(name = "document_type")
    private String documentType;
    
    @Column(name = "document_number")
    private String documentNumber;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "booking_id")
    private String bookingId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
    
    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL)
    private List<SeatSelection> seatSelections;
    
    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL)
    private List<BaggageService> baggageServices;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

