package com.flightbooking.service;

import com.flightbooking.dto.BaggageServiceDTO;
import com.flightbooking.entity.BaggageService;
import com.flightbooking.repository.BaggageServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BaggageServiceService {
    
    @Autowired
    private BaggageServiceRepository baggageServiceRepository;
    
    @Transactional
    public BaggageServiceDTO createBaggageService(String passengerId, String segmentId, 
                                                   Integer weightKg, BigDecimal price) {
        BaggageService baggageService = new BaggageService();
        baggageService.setId(UUID.randomUUID().toString());
        baggageService.setPassengerId(passengerId);
        baggageService.setSegmentId(segmentId);
        baggageService.setWeightKg(weightKg);
        baggageService.setPrice(price);
        
        baggageService = baggageServiceRepository.save(baggageService);
        return convertToDTO(baggageService);
    }
    
    public List<BaggageServiceDTO> getBaggageServicesByPassengerId(String passengerId) {
        return baggageServiceRepository.findByPassengerId(passengerId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public List<BaggageServiceDTO> getBaggageServicesBySegmentId(String segmentId) {
        return baggageServiceRepository.findBySegmentId(segmentId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private BaggageServiceDTO convertToDTO(BaggageService baggageService) {
        BaggageServiceDTO dto = new BaggageServiceDTO();
        dto.setId(baggageService.getId());
        dto.setPassengerId(baggageService.getPassengerId());
        dto.setSegmentId(baggageService.getSegmentId());
        dto.setWeightKg(baggageService.getWeightKg());
        dto.setPrice(baggageService.getPrice());
        return dto;
    }
}

