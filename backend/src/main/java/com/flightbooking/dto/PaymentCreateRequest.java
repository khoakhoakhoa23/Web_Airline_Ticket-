package com.flightbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment Create Request DTO
 * 
 * Request body for creating a new payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequest {
    
    /**
     * Booking ID to create payment for
     */
    @NotBlank(message = "Booking ID is required")
    private String bookingId;
    
    /**
     * Payment method/provider
     * Supported values: STRIPE, VNPAY, MOMO, BANK_TRANSFER
     * Default: STRIPE
     */
    @Pattern(regexp = "STRIPE|VNPAY|MOMO|BANK_TRANSFER", message = "Invalid payment method")
    private String paymentMethod = "STRIPE";
    
    /**
     * Success URL for redirect after payment (optional)
     * For Stripe Checkout Session
     */
    private String successUrl;
    
    /**
     * Cancel URL for redirect if payment cancelled (optional)
     * For Stripe Checkout Session
     */
    private String cancelUrl;
}

