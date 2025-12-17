package com.flightbooking.controller;

import com.flightbooking.dto.LoginRequest;
import com.flightbooking.dto.LoginResponse;
import com.flightbooking.dto.RegisterRequest;
import com.flightbooking.dto.UserDTO;
import com.flightbooking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Endpoints:
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - Login and get JWT token
 * 
 * Security:
 * - Password hashed with BCrypt before saving
 * - Password never exposed in response
 * - JWT token returned on successful login
 * - Returns 401 on invalid credentials
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Register new user
     * 
     * POST /api/auth/register
     * Request: { "email": "...", "password": "...", "phone": "..." }
     * Response: { "id": "...", "email": "...", "role": "USER", "status": "ACTIVE" }
     * 
     * Security:
     * - Password is BCrypt hashed before saving
     * - Password is NOT returned in response
     * - Email must be unique
     * 
     * @param request RegisterRequest (email, password, phone, role)
     * @return UserDTO (id, email, phone, role, status) - NO PASSWORD
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * Login user and get JWT token
     * 
     * POST /api/auth/login
     * Request: { "email": "...", "password": "..." }
     * Response: { "accessToken": "eyJhbGciOiJIUzUxMiJ9..." }
     * 
     * Security:
     * - Password compared with BCrypt hashed password
     * - Returns 401 if credentials invalid
     * - JWT token contains userId, email, role
     * - Token expires in 24 hours (configurable)
     * 
     * @param request LoginRequest (email, password)
     * @return LoginResponse (accessToken)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

