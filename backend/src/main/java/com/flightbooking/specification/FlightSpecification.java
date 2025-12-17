package com.flightbooking.specification;

import com.flightbooking.entity.Flight;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Flight Specification
 * 
 * Builds dynamic JPA queries for flight search with multiple filters
 * 
 * Features:
 * - Required filters: origin, destination, date range, available seats
 * - Optional filters: price range, airline, cabin class
 * - Case-insensitive airline search
 * - Only shows SCHEDULED flights
 */
public class FlightSpecification {
    
    /**
     * Build specification for flight search
     * 
     * @param origin Origin airport code (required)
     * @param destination Destination airport code (required)
     * @param startDate Start of departure date range (required)
     * @param endDate End of departure date range (required)
     * @param passengers Minimum available seats (required)
     * @param minPrice Minimum total price (optional)
     * @param maxPrice Maximum total price (optional)
     * @param airline Airline filter (optional, case-insensitive)
     * @param cabinClass Cabin class filter (optional)
     * @return Specification for query building
     */
    public static Specification<Flight> searchFlights(
            String origin,
            String destination,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer passengers,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String airline,
            String cabinClass) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Required filters
            
            // Origin (case-insensitive, exact match)
            predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("origin")),
                    origin.toUpperCase()
            ));
            
            // Destination (case-insensitive, exact match)
            predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("destination")),
                    destination.toUpperCase()
            ));
            
            // Departure time range
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("departTime"),
                    startDate
            ));
            predicates.add(criteriaBuilder.lessThan(
                    root.get("departTime"),
                    endDate
            ));
            
            // Available seats (must have enough seats for passengers)
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("availableSeats"),
                    passengers
            ));
            
            // Status (only SCHEDULED flights)
            predicates.add(criteriaBuilder.equal(
                    root.get("status"),
                    "SCHEDULED"
            ));
            
            // Optional filters
            
            // Price range filter (baseFare + taxes)
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.sum(root.get("baseFare"), root.get("taxes")),
                        minPrice
                ));
            }
            
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.sum(root.get("baseFare"), root.get("taxes")),
                        maxPrice
                ));
            }
            
            // Airline filter (case-insensitive, partial match)
            if (airline != null && !airline.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("airline")),
                        "%" + airline.toUpperCase() + "%"
                ));
            }
            
            // Cabin class filter (exact match)
            if (cabinClass != null && !cabinClass.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("cabinClass"),
                        cabinClass.toUpperCase()
                ));
            }
            
            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Specification for finding flights by airline
     * Used for airline-specific searches
     */
    public static Specification<Flight> hasAirline(String airline) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("airline")),
                        "%" + airline.toUpperCase() + "%"
                );
    }
    
    /**
     * Specification for finding flights within price range
     * Used for price-based filtering
     */
    public static Specification<Flight> hasPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.sum(root.get("baseFare"), root.get("taxes")),
                        minPrice
                ));
            }
            
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.sum(root.get("baseFare"), root.get("taxes")),
                        maxPrice
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Specification for finding available flights (has available seats)
     * Used for booking availability checks
     */
    public static Specification<Flight> hasAvailableSeats(Integer minSeats) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("availableSeats"),
                        minSeats
                );
    }
    
    /**
     * Specification for finding scheduled flights only
     * Excludes CANCELLED, DELAYED, COMPLETED flights
     */
    public static Specification<Flight> isScheduled() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), "SCHEDULED");
    }
}

