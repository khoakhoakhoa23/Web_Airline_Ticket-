package com.flightbooking.controller;

import com.flightbooking.dto.TicketDTO;
import com.flightbooking.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class TicketController {
    
    @Autowired
    private TicketService ticketService;
    
    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<TicketDTO> issueTicket(@PathVariable String bookingId) {
        try {
            TicketDTO ticket = ticketService.issueTicket(bookingId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable String id) {
        try {
            TicketDTO ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<TicketDTO> getTicketByBookingId(@PathVariable String bookingId) {
        try {
            TicketDTO ticket = ticketService.getTicketByBookingId(bookingId);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        List<TicketDTO> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }
}

