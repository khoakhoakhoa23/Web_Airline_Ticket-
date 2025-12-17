package com.flightbooking.repository;

import com.flightbooking.entity.FlightSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightSegmentRepository extends JpaRepository<FlightSegment, String> {
    List<FlightSegment> findByBookingId(String bookingId);
    List<FlightSegment> findByOriginAndDestination(String origin, String destination);
}

