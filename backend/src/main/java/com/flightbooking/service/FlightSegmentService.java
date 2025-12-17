package com.flightbooking.service;

import com.flightbooking.dto.FlightSegmentDTO;
import com.flightbooking.entity.FlightSegment;
import com.flightbooking.repository.FlightSegmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlightSegmentService {
    
    @Autowired
    private FlightSegmentRepository flightSegmentRepository;
    
    public List<FlightSegmentDTO> getSegmentsByBookingId(String bookingId) {
        return flightSegmentRepository.findByBookingId(bookingId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<FlightSegmentDTO> searchFlights(String origin, String destination) {
        return flightSegmentRepository.findByOriginAndDestination(origin, destination).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private FlightSegmentDTO convertToDTO(FlightSegment segment) {
        FlightSegmentDTO dto = new FlightSegmentDTO();
        dto.setId(segment.getId());
        dto.setAirline(segment.getAirline());
        dto.setFlightNumber(segment.getFlightNumber());
        dto.setOrigin(segment.getOrigin());
        dto.setDestination(segment.getDestination());
        dto.setDepartTime(segment.getDepartTime());
        dto.setArriveTime(segment.getArriveTime());
        dto.setCabinClass(segment.getCabinClass());
        dto.setBaseFare(segment.getBaseFare());
        dto.setTaxes(segment.getTaxes());
        dto.setBookingId(segment.getBookingId());
        return dto;
    }
}

