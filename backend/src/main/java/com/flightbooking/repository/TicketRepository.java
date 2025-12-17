package com.flightbooking.repository;

import com.flightbooking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    List<Ticket> findByBookingId(String bookingId);
    Optional<Ticket> findByPnr(String pnr);
    Optional<Ticket> findByEticketNumber(String eticketNumber);
}

