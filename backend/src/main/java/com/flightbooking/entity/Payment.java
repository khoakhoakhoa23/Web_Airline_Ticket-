package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Entity
 * 
 * Represents a payment transaction for a booking.
 * Supports multiple payment providers (Stripe, VNPay, Momo, etc.)
 * 
 * Status Lifecycle:
 * - PENDING: Payment created, awaiting user action
 * - PROCESSING: Payment being processed by provider
 * - SUCCESS: Payment successful, booking confirmed
 * - FAILED: Payment failed
 * - CANCELLED: Payment cancelled by user or system
 * - REFUNDED: Payment refunded
 * 
 * Provider Types:
 * - STRIPE: International card payments
 * - VNPAY: Vietnamese payment gateway
 * - MOMO: Vietnamese e-wallet
 * - BANK_TRANSFER: Direct bank transfer
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    private String id;
    
    /**
     * Payment provider/method
     * Values: STRIPE, VNPAY, MOMO, BANK_TRANSFER
     */
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    
    /**
     * Payment amount in the specified currency
     */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    /**
     * Currency code (ISO 4217)
     * Examples: USD, VND, EUR
     */
    @Column(length = 3, nullable = false)
    private String currency;
    
    /**
     * Transaction ID from payment provider
     * For Stripe: payment_intent_id or charge_id
     * For VNPay: vnp_TransactionNo
     */
    @Column(name = "transaction_id")
    private String transactionId;
    
    /**
     * Payment Intent ID (Stripe specific)
     * Used to track payment intent lifecycle
     */
    @Column(name = "payment_intent_id")
    private String paymentIntentId;
    
    /**
     * Payment status
     * Values: PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, REFUNDED
     */
    @Column(nullable = false)
    private String status;
    
    /**
     * Payment description/notes
     */
    @Column(length = 500)
    private String description;
    
    /**
     * Provider-specific response data (JSON)
     * Stores raw response from payment provider
     */
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;
    
    /**
     * Failure reason if payment failed
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Reference to booking
     */
    @Column(name = "booking_id", nullable = false)
    private String bookingId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
    
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentWebhook> paymentWebhooks;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

