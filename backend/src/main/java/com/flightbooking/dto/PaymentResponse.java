package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Response DTO
 * 
 * Response for payment operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    
    /**
     * Payment ID
     */
    private String paymentId;
    
    /**
     * Booking ID
     */
    private String bookingId;
    
    /**
     * Payment amount
     */
    private BigDecimal amount;
    
    /**
     * Currency code
     */
    private String currency;
    
    /**
     * Payment method
     */
    private String paymentMethod;
    
    /**
     * Payment status
     */
    private String status;
    
    /**
     * Payment Intent ID (Stripe)
     */
    private String paymentIntentId;
    
    /**
     * Client Secret for Stripe Payment Intent
     * Used by frontend to confirm payment
     */
    private String clientSecret;
    
    /**
     * Checkout URL (for hosted checkout pages)
     * For Stripe Checkout Session or VNPay payment URL
     */
    private String checkoutUrl;
    
    /**
     * Payment description
     */
    private String description;
    
    /**
     * Created timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Message for user (success/error)
     */
    private String message;
}

