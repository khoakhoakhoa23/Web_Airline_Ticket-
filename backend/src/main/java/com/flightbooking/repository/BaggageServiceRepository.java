package com.flightbooking.repository;

import com.flightbooking.entity.BaggageService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaggageServiceRepository extends JpaRepository<BaggageService, String> {
    List<BaggageService> findByPassengerId(String passengerId);
    List<BaggageService> findBySegmentId(String segmentId);
}

