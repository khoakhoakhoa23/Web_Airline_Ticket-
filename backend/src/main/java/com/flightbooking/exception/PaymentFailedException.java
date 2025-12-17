package com.flightbooking.exception;

/**
 * Payment Failed Exception
 * 
 * Thrown when payment processing fails
 * Examples:
 * - Stripe API error
 * - Insufficient funds
 * - Card declined
 */
public class PaymentFailedException extends RuntimeException {
    
    private final String paymentProvider;
    private final String errorCode;
    
    public PaymentFailedException(String message) {
        super(message);
        this.paymentProvider = null;
        this.errorCode = "PAYMENT_FAILED";
    }
    
    public PaymentFailedException(String paymentProvider, String errorCode, String message) {
        super(message);
        this.paymentProvider = paymentProvider;
        this.errorCode = errorCode;
    }
    
    public String getPaymentProvider() {
        return paymentProvider;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

