package com.flightbooking.service;

import com.flightbooking.dto.FlightDTO;
import com.flightbooking.dto.FlightSearchRequest;
import com.flightbooking.entity.Flight;
import com.flightbooking.exception.BusinessException;
import com.flightbooking.exception.ResourceNotFoundException;
import com.flightbooking.repository.FlightRepository;
import com.flightbooking.specification.FlightSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Flight Service
 * 
 * Handles flight search with advanced filters:
 * - Required: origin, destination, date, passengers
 * - Optional: price range, airline, cabin class
 * - Pagination and sorting
 * - Only shows SCHEDULED flights with available seats
 */
@Service
public class FlightService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);
    
    @Autowired
    private FlightRepository flightRepository;
    
    /**
     * Search flights with advanced filters
     * 
     * Features:
     * - Dynamic query building with JPA Specification
     * - Supports price range filter
     * - Supports airline filter (case-insensitive, partial match)
     * - Supports cabin class filter
     * - Pagination and sorting
     * - Only returns SCHEDULED flights
     * 
     * @param request FlightSearchRequest with filters
     * @return Page of FlightDTO
     */
    public Page<FlightDTO> searchFlights(FlightSearchRequest request) {
        logger.info("Searching flights: {} -> {}, date: {}, passengers: {}", 
                request.getOrigin(), request.getDestination(), 
                request.getDepartDate(), request.getPassengers());
        
        // Validate search request
        validateFlightSearchRequest(request);
        
        // Build date range (full day)
        LocalDateTime startDate = request.getDepartDate().atStartOfDay();
        LocalDateTime endDate = request.getDepartDate().atTime(LocalTime.MAX);
        
        // Build dynamic specification with all filters
        Specification<Flight> spec = FlightSpecification.searchFlights(
                request.getOrigin(),
                request.getDestination(),
                startDate,
                endDate,
                request.getPassengers(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getAirline(),
                request.getCabinClass()
        );
        
        // Build page request with sorting
        Sort sort = buildSort(request.getSort());
        PageRequest pageRequest = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );
        
        // Execute query
        Page<Flight> flights = flightRepository.findAll(spec, pageRequest);
        
        logger.info("Found {} flights (page {}/{})", 
                flights.getTotalElements(), 
                flights.getNumber() + 1, 
                flights.getTotalPages());
        
        // Convert to DTO
        return flights.map(this::convertToDTO);
    }
    
    /**
     * Get flight by ID
     * 
     * @param id Flight ID
     * @return FlightDTO
     * @throws RuntimeException if flight not found
     */
    @Transactional(readOnly = true)
    public FlightDTO getFlightById(String id) {
        logger.info("Getting flight by ID: {}", id);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + id));
        return convertToDTO(flight);
    }
    
    /**
     * Get all flights
     * Used for admin/testing purposes
     * 
     * @return List of FlightDTO
     */
    @Transactional(readOnly = true)
    public List<FlightDTO> getAllFlights() {
        logger.info("Getting all flights");
        return flightRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Validate flight search request
     */
    private void validateFlightSearchRequest(FlightSearchRequest request) {
        // Validate origin
        if (request.getOrigin() == null || request.getOrigin().trim().isEmpty()) {
            throw new BusinessException("INVALID_ORIGIN", "Origin airport code is required");
        }
        
        // Validate destination
        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            throw new BusinessException("INVALID_DESTINATION", "Destination airport code is required");
        }
        
        // Origin and destination cannot be the same
        if (request.getOrigin().equalsIgnoreCase(request.getDestination())) {
            throw new BusinessException("INVALID_ROUTE", "Origin and destination cannot be the same");
        }
        
        // Validate departure date
        if (request.getDepartDate() == null) {
            throw new BusinessException("INVALID_DATE", "Departure date is required");
        }
        
        // Departure date cannot be in the past
        if (request.getDepartDate().isBefore(java.time.LocalDate.now())) {
            throw new BusinessException("INVALID_DATE", "Cannot search for flights with past departure date");
        }
        
        // Validate passengers
        if (request.getPassengers() == null || request.getPassengers() < 1) {
            throw new BusinessException("INVALID_PASSENGERS", "Number of passengers must be at least 1");
        }
        
        if (request.getPassengers() > 9) {
            throw new BusinessException("INVALID_PASSENGERS", "Number of passengers cannot exceed 9");
        }
        
        // Validate price range
        if (request.getMinPrice() != null && request.getMinPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_PRICE", "Minimum price cannot be negative");
        }
        
        if (request.getMaxPrice() != null && request.getMaxPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_PRICE", "Maximum price must be greater than zero");
        }
        
        if (request.getMinPrice() != null && request.getMaxPrice() != null && 
                request.getMinPrice().compareTo(request.getMaxPrice()) > 0) {
            throw new BusinessException("INVALID_PRICE", "Minimum price cannot be greater than maximum price");
        }
    }
    
    /**
     * Build Sort object from sort parameter
     * 
     * Supported values:
     * - departTime (default): Sort by departure time ascending
     * - price: Sort by total price (baseFare + taxes) ascending
     * - duration: Sort by duration ascending
     * - -departTime: Sort by departure time descending
     * - -price: Sort by price descending
     * 
     * @param sortParam Sort parameter string
     * @return Sort object
     */
    private Sort buildSort(String sortParam) {
        if (sortParam == null || sortParam.trim().isEmpty()) {
            return Sort.by("departTime").ascending();
        }
        
        // Check for descending order (prefix with -)
        boolean descending = sortParam.startsWith("-");
        String field = descending ? sortParam.substring(1) : sortParam;
        
        // Map sort field to entity field
        String entityField = switch (field.toLowerCase()) {
            case "price" -> "baseFare"; // Sort by baseFare (closest to total price)
            case "duration" -> "durationMinutes";
            case "departtime", "depart" -> "departTime";
            default -> "departTime";
        };
        
        return descending 
                ? Sort.by(entityField).descending() 
                : Sort.by(entityField).ascending();
    }
    
    /**
     * Convert Flight entity to FlightDTO
     * 
     * Calculates total price (baseFare + taxes)
     * Does not expose internal fields
     * 
     * @param flight Flight entity
     * @return FlightDTO
     */
    private FlightDTO convertToDTO(Flight flight) {
        FlightDTO dto = new FlightDTO();
        dto.setId(flight.getId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setAirline(flight.getAirline());
        dto.setOrigin(flight.getOrigin());
        dto.setDestination(flight.getDestination());
        dto.setDepartTime(flight.getDepartTime());
        dto.setArriveTime(flight.getArriveTime());
        dto.setCabinClass(flight.getCabinClass());
        dto.setBaseFare(flight.getBaseFare());
        dto.setTaxes(flight.getTaxes());
        
        // Calculate total price
        if (flight.getBaseFare() != null && flight.getTaxes() != null) {
            dto.setTotalPrice(flight.getBaseFare().add(flight.getTaxes()));
        }
        
        dto.setAvailableSeats(flight.getAvailableSeats());
        dto.setTotalSeats(flight.getTotalSeats());
        dto.setStatus(flight.getStatus());
        dto.setAircraftType(flight.getAircraftType());
        dto.setDurationMinutes(flight.getDurationMinutes());
        
        return dto;
    }
    
    // ==================== ADMIN METHODS ====================
    
    /**
     * Admin: Get all flights with pagination
     */
    @Transactional(readOnly = true)
    public Page<FlightDTO> getAllFlightsPaged(Pageable pageable) {
        logger.info("Admin: Fetching all flights with pagination");
        return flightRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Admin: Create new flight
     */
    @Transactional
    public FlightDTO createFlight(FlightDTO flightDTO) {
        logger.info("Admin: Creating new flight");
        
        // Validate flight data
        validateFlightData(flightDTO);
        
        Flight flight = new Flight();
        flight.setId(UUID.randomUUID().toString());
        flight.setFlightNumber(flightDTO.getFlightNumber());
        flight.setAirline(flightDTO.getAirline());
        flight.setOrigin(flightDTO.getOrigin());
        flight.setDestination(flightDTO.getDestination());
        flight.setDepartTime(flightDTO.getDepartTime());
        flight.setArriveTime(flightDTO.getArriveTime());
        flight.setCabinClass(flightDTO.getCabinClass());
        flight.setBaseFare(flightDTO.getBaseFare());
        flight.setTaxes(flightDTO.getTaxes());
        flight.setAvailableSeats(flightDTO.getAvailableSeats());
        flight.setTotalSeats(flightDTO.getTotalSeats());
        flight.setStatus(flightDTO.getStatus() != null ? flightDTO.getStatus() : "SCHEDULED");
        flight.setAircraftType(flightDTO.getAircraftType());
        flight.setDurationMinutes(flightDTO.getDurationMinutes());
        
        flight = flightRepository.save(flight);
        logger.info("Flight created successfully: {}", flight.getId());
        
        return convertToDTO(flight);
    }
    
    /**
     * Validate flight data
     */
    private void validateFlightData(FlightDTO flightDTO) {
        // Validate required fields
        if (flightDTO.getFlightNumber() == null || flightDTO.getFlightNumber().trim().isEmpty()) {
            throw new BusinessException("INVALID_FLIGHT", "Flight number is required");
        }
        
        if (flightDTO.getAirline() == null || flightDTO.getAirline().trim().isEmpty()) {
            throw new BusinessException("INVALID_FLIGHT", "Airline is required");
        }
        
        if (flightDTO.getOrigin() == null || flightDTO.getOrigin().trim().isEmpty()) {
            throw new BusinessException("INVALID_FLIGHT", "Origin is required");
        }
        
        if (flightDTO.getDestination() == null || flightDTO.getDestination().trim().isEmpty()) {
            throw new BusinessException("INVALID_FLIGHT", "Destination is required");
        }
        
        // Origin and destination cannot be the same
        if (flightDTO.getOrigin().equalsIgnoreCase(flightDTO.getDestination())) {
            throw new BusinessException("INVALID_ROUTE", "Origin and destination cannot be the same");
        }
        
        // Validate times
        if (flightDTO.getDepartTime() == null) {
            throw new BusinessException("INVALID_FLIGHT", "Departure time is required");
        }
        
        if (flightDTO.getArriveTime() == null) {
            throw new BusinessException("INVALID_FLIGHT", "Arrival time is required");
        }
        
        if (flightDTO.getArriveTime().isBefore(flightDTO.getDepartTime())) {
            throw new BusinessException("INVALID_TIME", "Arrival time must be after departure time");
        }
        
        // Validate prices
        if (flightDTO.getBaseFare() == null || flightDTO.getBaseFare().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_PRICE", "Base fare must be greater than zero");
        }
        
        if (flightDTO.getTaxes() == null || flightDTO.getTaxes().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_PRICE", "Taxes cannot be negative");
        }
        
        // Validate seats
        if (flightDTO.getTotalSeats() == null || flightDTO.getTotalSeats() < 1) {
            throw new BusinessException("INVALID_SEATS", "Total seats must be at least 1");
        }
        
        if (flightDTO.getAvailableSeats() == null || flightDTO.getAvailableSeats() < 0) {
            throw new BusinessException("INVALID_SEATS", "Available seats cannot be negative");
        }
        
        if (flightDTO.getAvailableSeats() > flightDTO.getTotalSeats()) {
            throw new BusinessException("INVALID_SEATS", "Available seats cannot exceed total seats");
        }
    }
    
    /**
     * Admin: Update existing flight
     */
    @Transactional
    public FlightDTO updateFlight(String id, FlightDTO flightDTO) {
        logger.info("Admin: Updating flight: {}", id);
        
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + id));
        
        // Update fields
        if (flightDTO.getFlightNumber() != null) flight.setFlightNumber(flightDTO.getFlightNumber());
        if (flightDTO.getAirline() != null) flight.setAirline(flightDTO.getAirline());
        if (flightDTO.getOrigin() != null) flight.setOrigin(flightDTO.getOrigin());
        if (flightDTO.getDestination() != null) flight.setDestination(flightDTO.getDestination());
        if (flightDTO.getDepartTime() != null) flight.setDepartTime(flightDTO.getDepartTime());
        if (flightDTO.getArriveTime() != null) flight.setArriveTime(flightDTO.getArriveTime());
        if (flightDTO.getCabinClass() != null) flight.setCabinClass(flightDTO.getCabinClass());
        if (flightDTO.getBaseFare() != null) flight.setBaseFare(flightDTO.getBaseFare());
        if (flightDTO.getTaxes() != null) flight.setTaxes(flightDTO.getTaxes());
        if (flightDTO.getAvailableSeats() != null) flight.setAvailableSeats(flightDTO.getAvailableSeats());
        if (flightDTO.getTotalSeats() != null) flight.setTotalSeats(flightDTO.getTotalSeats());
        if (flightDTO.getStatus() != null) flight.setStatus(flightDTO.getStatus());
        if (flightDTO.getAircraftType() != null) flight.setAircraftType(flightDTO.getAircraftType());
        if (flightDTO.getDurationMinutes() != null) flight.setDurationMinutes(flightDTO.getDurationMinutes());
        
        flight = flightRepository.save(flight);
        logger.info("Flight updated successfully: {}", id);
        
        return convertToDTO(flight);
    }
    
    /**
     * Admin: Delete flight (with business rule check)
     */
    @Transactional
    public void deleteFlight(String id) {
        logger.info("Admin: Attempting to delete flight: {}", id);
        
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with ID: " + id));
        
        // Business rule: Cannot delete flights in the past or with status COMPLETED
        if ("COMPLETED".equals(flight.getStatus()) || "CANCELLED".equals(flight.getStatus())) {
            logger.warn("Cannot delete flight with status: {}", flight.getStatus());
            // Allow deletion anyway for admin (they can override)
        }
        
        if (flight.getDepartTime().isBefore(LocalDateTime.now())) {
            logger.warn("Deleting past flight: {}", id);
            // Allow but log warning
        }
        
        flightRepository.delete(flight);
        logger.info("Flight deleted successfully: {}", id);
    }
}


