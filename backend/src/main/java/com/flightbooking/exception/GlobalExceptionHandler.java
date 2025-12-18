package com.flightbooking.exception;

import com.flightbooking.dto.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 * 
 * Catches all exceptions thrown by controllers and services
 * Returns standardized error responses
 * 
 * Handles:
 * - Validation errors (Bean Validation)
 * - Business rule violations
 * - Authentication/Authorization errors
 * - Resource not found
 * - Payment failures
 * - Generic exceptions
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle Bean Validation errors (DTO validation)
     * Triggered by @Valid annotation on controller methods
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        logger.warn("Validation error on {}: {}", request.getRequestURI(), ex.getMessage());
        
        // Extract field errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.withFieldErrors(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Validation failed for one or more fields",
                request.getRequestURI(),
                fieldErrors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle constraint violation errors
     * Triggered by @Validated on method parameters
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        
        logger.warn("Constraint violation on {}: {}", request.getRequestURI(), ex.getMessage());
        
        // Extract constraint violations
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));
        
        ErrorResponse errorResponse = ErrorResponse.withFieldErrors(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Constraint validation failed",
                request.getRequestURI(),
                fieldErrors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle business rule violations
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        
        logger.warn("Business rule violation on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("BUSINESS_ERROR")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * ✅ CRITICAL: Explicitly handle AuthenticationCredentialsNotFoundException FIRST
     * 
     * ⚠️ MUST be placed BEFORE ResourceNotFoundException handler to ensure 401 is returned
     * This ensures that AuthenticationCredentialsNotFoundException is ALWAYS caught
     * and returns 401, not 404
     */
    @ExceptionHandler(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationCredentialsNotFoundException(
            org.springframework.security.authentication.AuthenticationCredentialsNotFoundException ex,
            HttpServletRequest request) {
        
        logger.error("❌ AuthenticationCredentialsNotFoundException on {} {}: {}", 
            request.getMethod(), request.getRequestURI(), ex.getMessage());
        logger.error("Stack trace:", ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                ex.getMessage() != null ? ex.getMessage() : "User not authenticated. Please login to continue.",
                request.getRequestURI()
        );
        
        logger.error("✅ Returning 401 UNAUTHORIZED for AuthenticationCredentialsNotFoundException");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * ✅ Handle authentication exceptions (401 Unauthorized)
     * 
     * Catches:
     * - AuthenticationCredentialsNotFoundException (handled above, but this is a fallback)
     * - Other Spring Security authentication exceptions
     * 
     * ⚠️ CRITICAL: This handler MUST catch AuthenticationCredentialsNotFoundException
     * to return 401 instead of 404
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex,
            HttpServletRequest request) {
        
        logger.error("❌ Authentication error on {} {}: {}", 
            request.getMethod(), request.getRequestURI(), ex.getMessage());
        logger.error("Exception type: {}", ex.getClass().getName());
        logger.error("Stack trace:", ex);
        
        // ✅ CRITICAL: Always return 401 for authentication exceptions
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                ex.getMessage() != null ? ex.getMessage() : "User not authenticated. Please login to continue.",
                request.getRequestURI()
        );
        
        logger.error("✅ Returning 401 UNAUTHORIZED for authentication error");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle bad credentials (login failure)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request) {
        
        logger.warn("Bad credentials on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Invalid email or password",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle resource not found
     * 
     * ⚠️ NOTE: This should NOT catch AuthenticationCredentialsNotFoundException
     * Authentication exceptions are handled above
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        logger.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle unauthorized actions
     */
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedActionException(
            UnauthorizedActionException ex,
            HttpServletRequest request) {
        
        logger.warn("Unauthorized action on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Handle Spring Security access denied
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        logger.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                "You do not have permission to access this resource",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Handle JWT errors
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            JwtException ex,
            HttpServletRequest request) {
        
        logger.warn("JWT error on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Invalid or expired token. Please login again.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle payment failures
     */
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailedException(
            PaymentFailedException ex,
            HttpServletRequest request) {
        
        logger.error("Payment failed on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.PAYMENT_REQUIRED.value())
                .error("PAYMENT_FAILED")
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(errorResponse);
    }
    
    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        logger.warn("Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * ✅ CRITICAL: Handle database integrity violations (foreign key constraints, unique constraints, etc.)
     * 
     * This prevents 500 errors from database constraint violations.
     * Common cases:
     * - Foreign key constraint: User ID not found in users table
     * - Unique constraint: Duplicate booking code
     * - Not null constraint: Required field is null
     * 
     * Returns 400 (BAD_REQUEST) or 404 (NOT_FOUND) instead of 500
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        
        String errorMessage = ex.getMessage();
        String userFriendlyMessage = "Data validation failed. Please check your input.";
        String errorCode = "DATA_INTEGRITY_ERROR";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        // ✅ Parse specific constraint violations
        if (errorMessage != null) {
            logger.error("Data integrity violation on {}: {}", request.getRequestURI(), errorMessage);
            
            // Foreign key constraint violations
            if (errorMessage.contains("user_id") && errorMessage.contains("not present")) {
                errorCode = "USER_NOT_FOUND";
                userFriendlyMessage = "User account not found. Please register or login again.";
                status = HttpStatus.NOT_FOUND;
            } else if (errorMessage.contains("foreign key constraint")) {
                errorCode = "FOREIGN_KEY_VIOLATION";
                userFriendlyMessage = "Referenced resource not found. Please check your data.";
                status = HttpStatus.BAD_REQUEST;
            } 
            // Unique constraint violations
            else if (errorMessage.contains("unique constraint") || errorMessage.contains("duplicate key")) {
                errorCode = "DUPLICATE_ENTRY";
                userFriendlyMessage = "This record already exists. Please use a different value.";
                status = HttpStatus.CONFLICT;
            }
            // Not null constraint violations
            else if (errorMessage.contains("not-null") || errorMessage.contains("null value")) {
                errorCode = "NULL_CONSTRAINT_VIOLATION";
                userFriendlyMessage = "Required field is missing. Please fill in all required fields.";
                status = HttpStatus.BAD_REQUEST;
            }
        } else {
            logger.error("Data integrity violation on {}: (no message)", request.getRequestURI());
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(status.value())
                .error("DATA_INTEGRITY_ERROR")
                .errorCode(errorCode)
                .message(userFriendlyMessage)
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Handle all other exceptions (fallback)
     * 
     * ⚠️ This should be the last handler to catch any unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        // ✅ Enhanced logging with stack trace for debugging
        logger.error("Unexpected error on {} {}: {}", 
            request.getMethod(), 
            request.getRequestURI(), 
            ex.getMessage(), 
            ex);
        
        // ✅ Log request details for debugging
        logger.error("Request details - Method: {}, URI: {}, Query: {}, RemoteAddr: {}", 
            request.getMethod(),
            request.getRequestURI(),
            request.getQueryString(),
            request.getRemoteAddr());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
