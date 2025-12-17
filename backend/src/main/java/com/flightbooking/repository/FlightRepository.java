package com.flightbooking.repository;

import com.flightbooking.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Flight Repository
 * 
 * Extends JpaSpecificationExecutor for dynamic query building with filters
 * - Supports complex search criteria (origin, destination, date, price, airline)
 * - Pagination and sorting
 * - Flexible filter combinations
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, String>, JpaSpecificationExecutor<Flight> {
    
    /**
     * Find flights by flight number and depart time range
     * Used for checking duplicate flights
     */
    List<Flight> findByFlightNumberAndDepartTimeBetween(
            String flightNumber, 
            LocalDateTime start, 
            LocalDateTime end
    );
    
    /**
     * Find flights by origin, destination and status
     * Used for route availability checking
     */
    List<Flight> findByOriginAndDestinationAndStatus(
            String origin, 
            String destination, 
            String status
    );
    
    /**
     * Count available flights for a route on a specific date
     * Used for analytics/dashboard
     */
    @Query("SELECT COUNT(f) FROM Flight f WHERE " +
           "f.origin = :origin AND " +
           "f.destination = :destination AND " +
           "f.departTime >= :startDate AND " +
           "f.departTime < :endDate AND " +
           "f.status = 'SCHEDULED' AND " +
           "f.availableSeats > 0")
    long countAvailableFlights(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

