package com.flightbooking.repository;

import com.flightbooking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByUserId(String userId);
    List<Booking> findByStatus(String status);
    
    /**
     * Find bookings with status in given list and holdExpiresAt before given time.
     * Used for expiring hold bookings.
     */
    List<Booking> findByStatusInAndHoldExpiresAtBefore(List<String> statuses, LocalDateTime expirationTime);
    
    // Admin & Statistics methods (with pagination)
    Page<Booking> findByStatus(String status, Pageable pageable);
    Long countByStatus(String status);
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

