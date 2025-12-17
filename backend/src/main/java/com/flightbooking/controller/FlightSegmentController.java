package com.flightbooking.controller;

import com.flightbooking.dto.FlightSegmentDTO;
import com.flightbooking.service.FlightSegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flight-segments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class FlightSegmentController {
    
    @Autowired
    private FlightSegmentService flightSegmentService;
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<FlightSegmentDTO>> getSegmentsByBookingId(@PathVariable String bookingId) {
        List<FlightSegmentDTO> segments = flightSegmentService.getSegmentsByBookingId(bookingId);
        return ResponseEntity.ok(segments);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<FlightSegmentDTO>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination) {
        List<FlightSegmentDTO> segments = flightSegmentService.searchFlights(origin, destination);
        return ResponseEntity.ok(segments);
    }
}

