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
        
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        
        // ✅ Set role - default to "CUSTOMER" for booking system
        String userRole = request.getRole() != null && !request.getRole().trim().isEmpty() 
            ? request.getRole().trim().toUpperCase() 
            : "CUSTOMER";
        user.setRole(userRole);
        
        // ✅ Set status to ACTIVE by default
        user.setStatus("ACTIVE");
        
        logger.debug("User entity created - ID: {}, Email: {}, Role: {}, Status: {}", 
            user.getId(), user.getEmail(), user.getRole(), user.getStatus());
        
        // Save to database
        logger.info("Saving user to database. Email: {}, Phone: {}, Role: {}", 
            user.getEmail(), user.getPhone(), user.getRole());
        logger.debug("User details before save - ID: {}, Email: {}, Status: {}", 
            user.getId(), user.getEmail(), user.getStatus());
        
        try {
            // ✅ CRITICAL: Flush immediately to ensure data is persisted
            user = userRepository.save(user);
            userRepository.flush(); // Force immediate write to database
            
            logger.info("✅ User saved to database. Email: {} (ID: {})", user.getEmail(), user.getId());
            
            // ✅ CRITICAL: Verify user was actually saved (with retry)
            boolean exists = false;
            for (int i = 0; i < 3; i++) {
                exists = userRepository.existsById(user.getId());
                if (exists) {
                    break;
                }
                Thread.sleep(100); // Wait 100ms before retry
            }
            
            if (!exists) {
                logger.error("❌ CRITICAL: User was saved but does not exist in database!");
                logger.error("   User ID: {}", user.getId());
                logger.error("   Email: {}", user.getEmail());
                logger.error("   This indicates a database transaction or persistence issue");
                throw new RuntimeException("Failed to persist user to database. User was saved but not found.");
            }
            
            logger.info("✅ Verified: User exists in database. ID: {}, Email: {}", 
                user.getId(), user.getEmail());
                
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("❌ Database constraint violation when saving user");
            logger.error("   Email: {}", user.getEmail());
            logger.error("   Error: {}", e.getMessage());
            
            // Check if it's a duplicate email
            if (e.getMessage() != null && e.getMessage().contains("email")) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
                );
            }
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Failed to create user account: " + e.getMessage()
            );
        } catch (Exception e) {
            logger.error("❌ Failed to save user to database");
            logger.error("   Email: {}", user.getEmail());
            logger.error("   User ID: {}", user.getId());
            logger.error("   Error: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to create user account. Please try again."
            );
        }
        
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

