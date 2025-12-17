package com.flightbooking.repository;

import com.flightbooking.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 * 
 * Handles payment data access operations
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    /**
     * Find payment by payment intent ID (Stripe)
     */
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
    
    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Find all payments for a booking
     */
    List<Payment> findByBookingId(String bookingId);
    
    /**
     * Find payments by status
     */
    List<Payment> findByStatus(String status);
    
    /**
     * Find payments by booking ID and status
     */
    List<Payment> findByBookingIdAndStatus(String bookingId, String status);
    
    /**
     * Check if booking has successful payment
     */
    boolean existsByBookingIdAndStatus(String bookingId, String status);
}
