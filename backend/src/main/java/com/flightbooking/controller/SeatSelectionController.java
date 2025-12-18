package com.flightbooking.controller;

import com.flightbooking.dto.BookedSeatDTO;
import com.flightbooking.dto.CreateSeatSelectionRequest;
import com.flightbooking.dto.SeatSelectionDTO;
import com.flightbooking.service.SeatSelectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seat-selections")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class SeatSelectionController {
    
    @Autowired
    private SeatSelectionService seatSelectionService;
    
    @PostMapping
    public ResponseEntity<SeatSelectionDTO> createSeatSelection(
            @Valid @RequestBody CreateSeatSelectionRequest request) {
        try {
            SeatSelectionDTO seatSelection = seatSelectionService.createSeatSelection(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(seatSelection);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<SeatSelectionDTO>> getSeatSelectionsByPassengerId(
            @PathVariable String passengerId) {
        List<SeatSelectionDTO> seatSelections = 
            seatSelectionService.getSeatSelectionsByPassengerId(passengerId);
        return ResponseEntity.ok(seatSelections);
    }
    
    @GetMapping("/segment/{segmentId}")
    public ResponseEntity<List<SeatSelectionDTO>> getSeatSelectionsBySegmentId(
            @PathVariable String segmentId) {
        List<SeatSelectionDTO> seatSelections = 
            seatSelectionService.getSeatSelectionsBySegmentId(segmentId);
        return ResponseEntity.ok(seatSelections);
    }
    
    /**
     * Get booked seats for a flight with customer information
     * GET /api/seat-selections/flight/{flightNumber}
     */
    @GetMapping("/flight/{flightNumber}")
    public ResponseEntity<List<BookedSeatDTO>> getBookedSeatsByFlightNumber(
            @PathVariable String flightNumber) {
        List<BookedSeatDTO> bookedSeats = 
            seatSelectionService.getBookedSeatsByFlightNumber(flightNumber);
        return ResponseEntity.ok(bookedSeats);
    }
}

