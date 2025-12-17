package com.flightbooking.service;

import com.flightbooking.dto.LoginRequest;
import com.flightbooking.dto.LoginResponse;
import com.flightbooking.dto.RegisterRequest;
import com.flightbooking.dto.UserDTO;
import com.flightbooking.entity.User;
import com.flightbooking.repository.UserRepository;
import com.flightbooking.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Authentication Service
 * 
 * Handles:
 * - User registration with BCrypt password hashing
 * - User login with password verification
 * - JWT token generation
 * 
 * Security Features:
 * - Passwords are BCrypt hashed (strength 10)
 * - Passwords are NEVER exposed in responses
 * - Email uniqueness validation
 * - Account status checking (ACTIVE/INACTIVE)
 * - 401 Unauthorized on invalid credentials
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Register new user
     * 
     * Process:
     * 1. Check if email already exists
     * 2. Hash password with BCrypt
     * 3. Set default role (USER) if not provided
     * 4. Set status to ACTIVE
     * 5. Save to database
     * 6. Return UserDTO (WITHOUT password)
     * 
     * @param request RegisterRequest (email, password, phone, role)
     * @return UserDTO (id, email, phone, role, status, timestamps) - NO PASSWORD
     * @throws ResponseStatusException 409 if email already exists
     */
    @Transactional
    public UserDTO register(RegisterRequest request) {
        logger.info("Attempting to register user with email: {}", request.getEmail());
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Email already exists"
            );
        }
        
        // Create user entity
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        
        // ✅ CRITICAL: Hash password with BCrypt
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setStatus("ACTIVE");
        
        // Save to database
        user = userRepository.save(user);
        
        logger.info("User registered successfully: {} (ID: {})", user.getEmail(), user.getId());
        
        // ✅ CRITICAL: Convert to DTO (password excluded)
        return convertToDTO(user);
    }
    
    /**
     * Login user and generate JWT token
     * 
     * Process:
     * 1. Find user by email
     * 2. Verify password with BCrypt
     * 3. Check account status (must be ACTIVE)
     * 4. Generate JWT token with userId, email, role
     * 5. Return accessToken ONLY
     * 
     * @param request LoginRequest (email, password)
     * @return LoginResponse (accessToken)
     * @throws BadCredentialsException 401 if credentials invalid
     * @throws ResponseStatusException 401 if account not active
     */
    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                logger.warn("Login failed: User not found: {}", request.getEmail());
                return new BadCredentialsException("Invalid email or password");
            });
        
        // ✅ CRITICAL: Compare password with BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Invalid password for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
        
        // Check account status
        if (!"ACTIVE".equals(user.getStatus())) {
            logger.warn("Login failed: Account not active: {}", request.getEmail());
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, 
                "Account is not active"
            );
        }
        
        // ✅ Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        
        logger.info("Login successful for email: {} (ID: {})", user.getEmail(), user.getId());
        
        // ✅ Return ONLY accessToken (as per production requirement)
        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        
        return response;
    }
    
    /**
     * Convert User entity to UserDTO
     * 
     * ✅ CRITICAL: Password is NEVER included in DTO
     * 
     * @param user User entity
     * @return UserDTO (id, email, phone, role, status, timestamps) - NO PASSWORD
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        // ✅ Password is NEVER set in DTO
        return dto;
    }
}

