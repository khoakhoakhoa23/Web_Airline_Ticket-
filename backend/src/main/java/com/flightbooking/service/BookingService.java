package com.flightbooking.service;

import com.flightbooking.dto.BookingDTO;
import com.flightbooking.dto.CreateBookingRequest;
import com.flightbooking.dto.FlightSegmentDTO;
import com.flightbooking.dto.PassengerDTO;
import com.flightbooking.entity.Booking;
import com.flightbooking.entity.FlightSegment;
import com.flightbooking.entity.Passenger;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.FlightSegmentRepository;
import com.flightbooking.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private FlightSegmentRepository flightSegmentRepository;
    
    @Autowired
    private PassengerRepository passengerRepository;
    
    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request) {
        // Create booking
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setBookingCode(generateBookingCode());
        booking.setStatus("PENDING");
        booking.setUserId(request.getUserId());
        booking.setCurrency(request.getCurrency() != null ? request.getCurrency() : "VND");
        
        // Calculate total amount
        BigDecimal totalAmount = request.getFlightSegments().stream()
            .map(segment -> segment.getBaseFare().add(segment.getTaxes()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        booking.setTotalAmount(totalAmount);
        
        booking.setHoldExpiresAt(LocalDateTime.now().plusHours(24));
        
        booking = bookingRepository.save(booking);
        
        // Store booking ID in final variable for use in lambda expressions
        final String bookingId = booking.getId();
        
        // Create flight segments
        List<FlightSegment> segments = request.getFlightSegments().stream()
            .map(dto -> {
                FlightSegment segment = new FlightSegment();
                segment.setId(UUID.randomUUID().toString());
                segment.setBookingId(bookingId);
                segment.setAirline(dto.getAirline());
                segment.setFlightNumber(dto.getFlightNumber());
                segment.setOrigin(dto.getOrigin());
                segment.setDestination(dto.getDestination());
                segment.setDepartTime(dto.getDepartTime());
                segment.setArriveTime(dto.getArriveTime());
                segment.setCabinClass(dto.getCabinClass());
                segment.setBaseFare(dto.getBaseFare());
                segment.setTaxes(dto.getTaxes());
                return segment;
            })
            .collect(Collectors.toList());
        flightSegmentRepository.saveAll(segments);
        
        // Create passengers
        List<Passenger> passengers = request.getPassengers().stream()
            .map(dto -> {
                Passenger passenger = new Passenger();
                passenger.setId(UUID.randomUUID().toString());
                passenger.setBookingId(bookingId);
                passenger.setFullName(dto.getFullName());
                passenger.setDateOfBirth(dto.getDateOfBirth());
                passenger.setGender(dto.getGender());
                passenger.setDocumentType(dto.getDocumentType());
                passenger.setDocumentNumber(dto.getDocumentNumber());
                return passenger;
            })
            .collect(Collectors.toList());
        passengerRepository.saveAll(passengers);
        
        return convertToDTO(booking);
    }
    
    public BookingDTO getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        return convertToDTO(booking);
    }
    
    public BookingDTO getBookingByCode(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        return convertToDTO(booking);
    }
    
    public List<BookingDTO> getBookingsByUserId(String userId) {
        return bookingRepository.findByUserId(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public BookingDTO updateBookingStatus(String id, String status) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        return convertToDTO(booking);
    }
    
    @Transactional
    public BookingDTO finalizeBooking(String id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking must be CONFIRMED before finalizing");
        }
        
        // Finalize booking - confirm seats and baggage
        booking.setStatus("FINALIZED");
        booking = bookingRepository.save(booking);
        
        return convertToDTO(booking);
    }
    
    private String generateBookingCode() {
        return "BK" + System.currentTimeMillis();
    }
    
    public BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingCode(booking.getBookingCode());
        dto.setStatus(booking.getStatus());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setCurrency(booking.getCurrency());
        dto.setHoldExpiresAt(booking.getHoldExpiresAt());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        dto.setUserId(booking.getUserId());
        
        if (booking.getFlightSegments() != null) {
            dto.setFlightSegments(booking.getFlightSegments().stream()
                .map(segment -> {
                    FlightSegmentDTO segmentDTO = new FlightSegmentDTO();
                    segmentDTO.setId(segment.getId());
                    segmentDTO.setAirline(segment.getAirline());
                    segmentDTO.setFlightNumber(segment.getFlightNumber());
                    segmentDTO.setOrigin(segment.getOrigin());
                    segmentDTO.setDestination(segment.getDestination());
                    segmentDTO.setDepartTime(segment.getDepartTime());
                    segmentDTO.setArriveTime(segment.getArriveTime());
                    segmentDTO.setCabinClass(segment.getCabinClass());
                    segmentDTO.setBaseFare(segment.getBaseFare());
                    segmentDTO.setTaxes(segment.getTaxes());
                    segmentDTO.setBookingId(segment.getBookingId());
                    return segmentDTO;
                })
                .collect(Collectors.toList()));
        }
        
        if (booking.getPassengers() != null) {
            dto.setPassengers(booking.getPassengers().stream()
                .map(passenger -> {
                    PassengerDTO passengerDTO = new PassengerDTO();
                    passengerDTO.setId(passenger.getId());
                    passengerDTO.setFullName(passenger.getFullName());
                    passengerDTO.setDateOfBirth(passenger.getDateOfBirth());
                    passengerDTO.setGender(passenger.getGender());
                    passengerDTO.setDocumentType(passenger.getDocumentType());
                    passengerDTO.setDocumentNumber(passenger.getDocumentNumber());
                    passengerDTO.setBookingId(passenger.getBookingId());
                    return passengerDTO;
                })
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    // ==================== ADMIN METHODS ====================
    
    /**
     * Admin: Get all bookings with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookingDTO> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Admin: Get bookings by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookingDTO> getBookingsByStatus(String status, Pageable pageable) {
        return bookingRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Admin: Get booking by ID (no ownership check)
     */
    @Transactional(readOnly = true)
    public BookingDTO getAdminBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        return convertToDTO(booking);
    }
    
    /**
     * Admin: Cancel any booking (override business rules if needed)
     */
    @Transactional
    public void adminCancelBooking(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        
        // Admin can cancel any booking except already cancelled ones
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is already cancelled");
        }
        
        booking.setStatus("CANCELLED");
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }
}

