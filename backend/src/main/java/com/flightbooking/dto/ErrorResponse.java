package com.flightbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized Error Response
 * 
 * Used for all error responses across the API
 * Provides consistent error format for frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Timestamp when error occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * HTTP status code (400, 401, 403, 404, 500, etc.)
     */
    private int status;
    
    /**
     * Error type/category
     * Examples: VALIDATION_ERROR, BUSINESS_ERROR, NOT_FOUND, UNAUTHORIZED
     */
    private String error;
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Error code for programmatic handling
     * Examples: INVALID_EMAIL, FLIGHT_FULL, BOOKING_ALREADY_PAID
     */
    private String errorCode;
    
    /**
     * Request path that caused the error
     */
    private String path;
    
    /**
     * Field-level validation errors
     * Map of field name to error message
     * Example: { "email": "Invalid email format", "password": "Password is required" }
     */
    private Map<String, String> fieldErrors;
    
    /**
     * Additional details (for development/debugging)
     */
    private List<String> details;
    
    /**
     * Create error response with minimal info
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
    
    /**
     * Create error response with field errors
     */
    public static ErrorResponse withFieldErrors(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
    }
}

