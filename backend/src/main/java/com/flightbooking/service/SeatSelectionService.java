package com.flightbooking.service;

import com.flightbooking.dto.CreateSeatSelectionRequest;
import com.flightbooking.dto.SeatSelectionDTO;
import com.flightbooking.entity.SeatSelection;
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

