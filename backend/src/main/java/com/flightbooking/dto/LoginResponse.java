package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Response DTO
 * 
 * Returns JWT access token after successful authentication
 * 
 * Example response:
 * {
 *   "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyLWlkIiwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3MDM0MDAwMDAsImV4cCI6MTcwMzQ4NjQwMH0..."
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /**
     * JWT access token
     * - Contains: userId (subject), email, role
     * - Expires in 24 hours (configurable)
     * - Use in Authorization header: "Bearer <accessToken>"
     */
    private String accessToken;
}

