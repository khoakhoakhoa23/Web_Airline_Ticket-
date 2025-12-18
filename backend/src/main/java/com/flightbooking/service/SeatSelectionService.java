package com.flightbooking.service;

import com.flightbooking.dto.BookedSeatDTO;
import com.flightbooking.dto.CreateSeatSelectionRequest;
import com.flightbooking.dto.SeatSelectionDTO;
import com.flightbooking.entity.Booking;
import com.flightbooking.entity.FlightSegment;
import com.flightbooking.entity.Passenger;
import com.flightbooking.entity.SeatSelection;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.FlightSegmentRepository;
import com.flightbooking.repository.PassengerRepository;
import com.flightbooking.repository.SeatSelectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeatSelectionService {
    
    @Autowired
    private SeatSelectionRepository seatSelectionRepository;
    
    @Autowired
    private FlightSegmentRepository flightSegmentRepository;
    
    @Autowired
    private PassengerRepository passengerRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Transactional
    public SeatSelectionDTO createSeatSelection(CreateSeatSelectionRequest request) {
        SeatSelection seatSelection = new SeatSelection();
        seatSelection.setId(UUID.randomUUID().toString());
        seatSelection.setPassengerId(request.getPassengerId());
        seatSelection.setSegmentId(request.getSegmentId());
        seatSelection.setSeatNumber(request.getSeatNumber());
        seatSelection.setSeatType(request.getSeatType());
        seatSelection.setPrice(request.getPrice());
        
        seatSelection = seatSelectionRepository.save(seatSelection);
        return convertToDTO(seatSelection);
    }
    
    public List<SeatSelectionDTO> getSeatSelectionsByPassengerId(String passengerId) {
        return seatSelectionRepository.findByPassengerId(passengerId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<SeatSelectionDTO> getSeatSelectionsBySegmentId(String segmentId) {
        return seatSelectionRepository.findBySegmentId(segmentId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get booked seats by flight number
     * Used by seat selection page to show which seats are already taken
     */
    @Transactional(readOnly = true)
    public List<BookedSeatDTO> getBookedSeatsByFlightNumber(String flightNumber) {
        // Find all flight segments with this flight number
        List<FlightSegment> segments = flightSegmentRepository.findAll().stream()
            .filter(segment -> segment.getFlightNumber() != null && 
                              segment.getFlightNumber().equals(flightNumber))
            .collect(Collectors.toList());
        
        if (segments.isEmpty()) {
            return List.of();
        }
        
        // Get segment IDs
        List<String> segmentIds = segments.stream()
            .map(FlightSegment::getId)
            .collect(Collectors.toList());
        
        // Get all seat selections for these segments
        List<SeatSelection> seatSelections = seatSelectionRepository.findAll().stream()
            .filter(ss -> ss.getSegmentId() != null && segmentIds.contains(ss.getSegmentId()))
            .collect(Collectors.toList());
        
        // Convert to DTO with customer info
        return seatSelections.stream()
            .map(seatSelection -> {
                BookedSeatDTO dto = new BookedSeatDTO();
                dto.setSeatNumber(seatSelection.getSeatNumber());
                
                if (seatSelection.getPassengerId() != null) {
                    Passenger passenger = passengerRepository.findById(seatSelection.getPassengerId()).orElse(null);
                    if (passenger != null) {
                        dto.setPassengerName(passenger.getFullName());
                        dto.setPassengerId(passenger.getId());
                        
                        if (passenger.getBookingId() != null) {
                            Booking booking = bookingRepository.findById(passenger.getBookingId()).orElse(null);
                            if (booking != null) {
                                dto.setBookingId(booking.getId());
                                dto.setBookingCode(booking.getBookingCode());
                                dto.setStatus(booking.getStatus());
                            }
                        }
                    }
                }
                
                return dto;
            })
            .filter(dto -> dto.getSeatNumber() != null)
            .collect(Collectors.toList());
    }
    
    private SeatSelectionDTO convertToDTO(SeatSelection seatSelection) {
        SeatSelectionDTO dto = new SeatSelectionDTO();
        dto.setId(seatSelection.getId());
        dto.setPassengerId(seatSelection.getPassengerId());
        dto.setSegmentId(seatSelection.getSegmentId());
        dto.setSeatNumber(seatSelection.getSeatNumber());
        dto.setSeatType(seatSelection.getSeatType());
        dto.setPrice(seatSelection.getPrice());
        return dto;
    }
}

