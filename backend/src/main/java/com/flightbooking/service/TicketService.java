package com.flightbooking.service;

import com.flightbooking.dto.TicketDTO;
import com.flightbooking.entity.Booking;
import com.flightbooking.entity.Ticket;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Transactional
    public TicketDTO issueTicket(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking must be CONFIRMED before issuing ticket");
        }
        
        // Check if ticket already exists
        List<Ticket> existingTickets = ticketRepository.findByBookingId(bookingId);
        if (!existingTickets.isEmpty()) {
            throw new RuntimeException("Ticket already issued for this booking");
        }
        
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID().toString());
        ticket.setBookingId(bookingId);
        ticket.setPnr(generatePNR());
        ticket.setEticketNumber(generateETicketNumber());
        ticket.setStatus("ISSUED");
        ticket.setIssuedAt(LocalDateTime.now());
        
        ticket = ticketRepository.save(ticket);
        
        // Update booking status to TICKETED
        booking.setStatus("TICKETED");
        bookingRepository.save(booking);
        
        return convertToDTO(ticket);
    }
    
    public TicketDTO getTicketById(String id) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return convertToDTO(ticket);
    }
    
    public TicketDTO getTicketByBookingId(String bookingId) {
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        if (tickets.isEmpty()) {
            throw new RuntimeException("Ticket not found for this booking");
        }
        return convertToDTO(tickets.get(0));
    }
    
    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private String generatePNR() {
        // Generate 6-character PNR
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder pnr = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            pnr.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return pnr.toString();
    }
    
    private String generateETicketNumber() {
        // Generate 13-digit e-ticket number
        return String.format("%013d", System.currentTimeMillis() % 10000000000000L);
    }
    
    private TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setBookingId(ticket.getBookingId());
        dto.setPnr(ticket.getPnr());
        dto.setEticketNumber(ticket.getEticketNumber());
        dto.setStatus(ticket.getStatus());
        dto.setIssuedAt(ticket.getIssuedAt());
        return dto;
    }
}

