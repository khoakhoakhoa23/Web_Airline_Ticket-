package com.flightbooking.exception;

/**
 * Business Exception
 * 
 * Thrown when business rule validation fails
 * Examples:
 * - Booking flight with no available seats
 * - Paying for already confirmed booking
 * - Searching flights in the past
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_RULE_VIOLATION";
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

