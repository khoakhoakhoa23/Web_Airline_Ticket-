package com.flightbooking.exception;

/**
 * Unauthorized Action Exception
 * 
 * Thrown when user attempts action they're not authorized for
 * Examples:
 * - Accessing another user's booking
 * - Cancelling someone else's booking
 * - Admin-only action by regular user
 */
public class UnauthorizedActionException extends RuntimeException {
    
    public UnauthorizedActionException(String message) {
        super(message);
    }
    
    public UnauthorizedActionException(String action, String resource) {
        super(String.format("Not authorized to %s %s", action, resource));
    }
}

