package com.flightbooking.repository;

import com.flightbooking.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatLockRepository extends JpaRepository<SeatLock, String> {
    
    /**
     * Find active lock for a specific seat
     */
    @Query("SELECT sl FROM SeatLock sl WHERE sl.flightNumber = :flightNumber " +
           "AND sl.seatNumber = :seatNumber AND sl.status = 'LOCKED' " +
           "AND sl.expiresAt > :now")
    Optional<SeatLock> findActiveLock(@Param("flightNumber") String flightNumber,
                                      @Param("seatNumber") String seatNumber,
                                      @Param("now") LocalDateTime now);
    
    /**
     * Find all active locks for a flight
     */
    @Query("SELECT sl FROM SeatLock sl WHERE sl.flightNumber = :flightNumber " +
           "AND sl.status = 'LOCKED' AND sl.expiresAt > :now")
    List<SeatLock> findActiveLocksByFlight(@Param("flightNumber") String flightNumber,
                                           @Param("now") LocalDateTime now);
    
    /**
     * Find expired locks
     */
    @Query("SELECT sl FROM SeatLock sl WHERE sl.status = 'LOCKED' " +
           "AND sl.expiresAt <= :now")
    List<SeatLock> findExpiredLocks(@Param("now") LocalDateTime now);
    
    /**
     * Find locks by user ID
     */
    List<SeatLock> findByUserIdAndStatus(String userId, String status);
    
    /**
     * Find locks by session ID
     */
    List<SeatLock> findBySessionIdAndStatus(String sessionId, String status);
    
    /**
     * Find locks by booking ID
     */
    List<SeatLock> findByBookingId(String bookingId);
    
    /**
     * Release expired locks
     */
    @Modifying
    @Query("UPDATE SeatLock sl SET sl.status = 'RELEASED', sl.updatedAt = :now " +
           "WHERE sl.status = 'LOCKED' AND sl.expiresAt <= :now")
    int releaseExpiredLocks(@Param("now") LocalDateTime now);
    
    /**
     * Confirm locks for a booking
     */
    @Modifying
    @Query("UPDATE SeatLock sl SET sl.status = 'CONFIRMED', sl.updatedAt = :now " +
           "WHERE sl.bookingId = :bookingId AND sl.status = 'LOCKED'")
    int confirmLocksForBooking(@Param("bookingId") String bookingId,
                              @Param("now") LocalDateTime now);
}

