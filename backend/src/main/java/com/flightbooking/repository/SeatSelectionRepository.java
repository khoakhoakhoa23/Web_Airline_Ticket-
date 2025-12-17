package com.flightbooking.repository;

import com.flightbooking.entity.SeatSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatSelectionRepository extends JpaRepository<SeatSelection, String> {
    List<SeatSelection> findByPassengerId(String passengerId);
    List<SeatSelection> findBySegmentId(String segmentId);
}

