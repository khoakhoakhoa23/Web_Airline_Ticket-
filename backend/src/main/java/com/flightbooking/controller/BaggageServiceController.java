package com.flightbooking.controller;

import com.flightbooking.dto.BaggageServiceDTO;
import com.flightbooking.service.BaggageServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/baggage-services")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class BaggageServiceController {
    
    @Autowired
    private BaggageServiceService baggageServiceService;
    
    @PostMapping
    public ResponseEntity<BaggageServiceDTO> createBaggageService(
            @RequestParam String passengerId,
            @RequestParam String segmentId,
            @RequestParam Integer weightKg,
            @RequestParam BigDecimal price) {
        try {
            BaggageServiceDTO baggageService = 
                baggageServiceService.createBaggageService(passengerId, segmentId, weightKg, price);
            return ResponseEntity.status(HttpStatus.CREATED).body(baggageService);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<BaggageServiceDTO>> getBaggageServicesByPassengerId(
            @PathVariable String passengerId) {
        List<BaggageServiceDTO> services = 
            baggageServiceService.getBaggageServicesByPassengerId(passengerId);
        return ResponseEntity.ok(services);
    }
    
    @GetMapping("/segment/{segmentId}")
    public ResponseEntity<List<BaggageServiceDTO>> getBaggageServicesBySegmentId(
            @PathVariable String segmentId) {
        List<BaggageServiceDTO> services = 
            baggageServiceService.getBaggageServicesBySegmentId(segmentId);
        return ResponseEntity.ok(services);
    }
}

