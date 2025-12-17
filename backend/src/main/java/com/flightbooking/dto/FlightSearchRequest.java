package com.flightbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Flight Search Request DTO
 * 
 * Required fields:
 * - origin: Origin airport code (e.g., SGN)
 * - destination: Destination airport code (e.g., HAN)
 * - departDate: Departure date (yyyy-MM-dd)
 * 
 * Optional fields:
 * - passengers: Number of passengers (default: 1)
 * - minPrice: Minimum price filter
 * - maxPrice: Maximum price filter
 * - airline: Airline code/name filter
 * - cabinClass: ECONOMY, BUSINESS, FIRST
 * - page: Page number (default: 0)
 * - size: Page size (default: 10)
 * - sort: Sort field (default: departTime)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchRequest {
    
    // Required filters
    @NotBlank(message = "Origin is required")
    private String origin;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    @NotNull(message = "Departure date is required")
    private LocalDate departDate;
    
    // Optional filters
    @Min(value = 1, message = "At least 1 passenger required")
    @Builder.Default
    private Integer passengers = 1;
    
    /**
     * Minimum price filter (baseFare + taxes)
     * Example: 1000000 (1 triệu VND)
     */
    private BigDecimal minPrice;
    
    /**
     * Maximum price filter (baseFare + taxes)
     * Example: 5000000 (5 triệu VND)
     */
    private BigDecimal maxPrice;
    
    /**
     * Airline filter (case-insensitive)
     * Examples: "Vietnam Airlines", "VietJet Air", "Bamboo Airways"
     */
    private String airline;
    
    /**
     * Cabin class filter
     * Values: ECONOMY, BUSINESS, FIRST
     */
    private String cabinClass;
    
    // Pagination
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 10;
    
    /**
     * Sort field
     * Values: departTime, price, duration
     * Default: departTime (ascending)
     */
    @Builder.Default
    private String sort = "departTime";
}

