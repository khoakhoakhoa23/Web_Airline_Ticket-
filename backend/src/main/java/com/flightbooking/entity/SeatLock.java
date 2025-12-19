package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Seat Lock Entity
 * 
 * Manages temporary seat locks during booking process
 * Prevents multiple users from selecting the same seat simultaneously
 * 
 * Lock duration: 15 minutes
 * Status: LOCKED, RELEASED, CONFIRMED
 */
@Entity
@Table(name = "seat_locks", 
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_seat_lock_active",
               columnNames = {"flight_number", "seat_number"}
           )
       },
       indexes = {
           @Index(name = "idx_seat_locks_flight_seat", columnList = "flight_number,seat_number"),
           @Index(name = "idx_seat_locks_expires_at", columnList = "expires_at"),
           @Index(name = "idx_seat_locks_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatLock {
    
    @Id
    private String id;
    
    @Column(name = "flight_number", nullable = false)
    private String flightNumber;
    
    @Column(name = "segment_id")
    private String segmentId;
    
    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "session_id")
    private String sessionId; // For anonymous users
    
    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // LOCKED, RELEASED, CONFIRMED
    
    @Column(name = "booking_id")
    private String bookingId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", insertable = false, updatable = false)
    private FlightSegment segment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (lockedAt == null) {
            lockedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "LOCKED";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if lock is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if lock is active (not expired and status is LOCKED)
     */
    public boolean isActive() {
        return "LOCKED".equals(status) && !isExpired();
    }
}

