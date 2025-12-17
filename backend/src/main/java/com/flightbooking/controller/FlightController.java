package com.flightbooking.controller;

import com.flightbooking.dto.FlightDTO;
import com.flightbooking.dto.FlightSearchRequest;
import com.flightbooking.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Flight Controller
 * 
 * Endpoints:
 * - GET /api/flights/search - Search flights with filters (query params)
 * - POST /api/flights/search - Search flights (JSON body)
 * - GET /api/flights/{id} - Get flight by ID
 * - GET /api/flights - Get all flights
 */
@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class FlightController {
    
    @Autowired
    private FlightService flightService;
    
    /**
     * Search flights with query parameters (GET method)
     * 
     * GET /api/flights/search?origin=SGN&destination=HAN&departureDate=2025-12-20&page=0&size=10
     * 
     * Optional filters:
     * - minPrice: Minimum price filter
     * - maxPrice: Maximum price filter
     * - airline: Airline code/name filter
     * 
     * @param origin Origin airport code (required)
     * @param destination Destination airport code (required)
     * @param departureDate Departure date (required, format: yyyy-MM-dd)
     * @param passengers Number of passengers (default: 1)
     * @param minPrice Minimum price filter (optional)
     * @param maxPrice Maximum price filter (optional)
     * @param airline Airline filter (optional)
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sort Sort field (default: departTime)
     * @return Page of FlightDTO
     */
    @GetMapping("/search")
    public ResponseEntity<Page<FlightDTO>> searchFlightsWithQueryParams(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(defaultValue = "1") Integer passengers,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String airline,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "departTime") String sort) {
        
        // Build search request from query params
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin(origin)
                .destination(destination)
                .departDate(departureDate)
                .passengers(passengers)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .airline(airline)
                .page(page)
                .size(size)
                .sort(sort)
                .build();
        
        Page<FlightDTO> flights = flightService.searchFlights(request);
        return ResponseEntity.ok(flights);
    }
    
    /**
     * Search flights with JSON body (POST method)
     * 
     * POST /api/flights/search
     * Body: { "origin": "SGN", "destination": "HAN", ... }
     * 
     * @param request FlightSearchRequest
     * @return Page of FlightDTO
     */
    @PostMapping("/search")
    public ResponseEntity<Page<FlightDTO>> searchFlightsWithBody(@Valid @RequestBody FlightSearchRequest request) {
        Page<FlightDTO> flights = flightService.searchFlights(request);
        return ResponseEntity.ok(flights);
    }
    
    /**
     * Get flight by ID
     * 
     * GET /api/flights/{id}
     * 
     * @param id Flight ID
     * @return FlightDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getFlightById(@PathVariable String id) {
        FlightDTO flight = flightService.getFlightById(id);
        return ResponseEntity.ok(flight);
    }
    
    /**
     * Get all flights (for admin/testing)
     * 
     * GET /api/flights
     * 
     * @return List of FlightDTO
     */
    @GetMapping
    public ResponseEntity<List<FlightDTO>> getAllFlights() {
        List<FlightDTO> flights = flightService.getAllFlights();
        return ResponseEntity.ok(flights);
    }
}

