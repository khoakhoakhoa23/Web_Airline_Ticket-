package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Email Context
 * 
 * Contains all information needed to send an email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailContext {
    
    /**
     * Recipient email address
     */
    private String to;
    
    /**
     * Email subject
     */
    private String subject;
    
    /**
     * Template name (e.g., "booking-confirmation", "payment-receipt")
     */
    private String template;
    
    /**
     * Template variables
     * Key-value pairs to be used in the email template
     */
    private Map<String, Object> variables;
}

