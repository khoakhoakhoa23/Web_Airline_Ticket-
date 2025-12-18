package com.flightbooking.service;

import com.flightbooking.dto.LoginRequest;
import com.flightbooking.dto.LoginResponse;
import com.flightbooking.dto.RegisterRequest;
import com.flightbooking.dto.UserDTO;
import com.flightbooking.entity.User;
import com.flightbooking.exception.BusinessException;
import com.flightbooking.exception.ResourceNotFoundException;
import com.flightbooking.repository.UserRepository;
import com.flightbooking.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Transactional
    public UserDTO register(RegisterRequest request) {
        logger.info("Registering user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new BusinessException("EMAIL_EXISTS", "Email already exists");
        }
        
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // ✅ Hash password with BCrypt
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setStatus("ACTIVE");
        
        user = userRepository.save(user);
        logger.info("User registered successfully: {} (ID: {})", user.getEmail(), user.getId());
        return convertToDTO(user);
    }
    
    /**
     * @deprecated Use AuthService.login() instead
     * This method is kept for backward compatibility with old UserController
     * It now uses the new LoginResponse format: { accessToken }
     */
    @Deprecated
    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                logger.warn("Login failed: User not found: {}", request.getEmail());
                return new BadCredentialsException("Invalid email or password");
            });
        
        // ✅ Compare hashed password with BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Invalid password for email: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
        
        if (!"ACTIVE".equals(user.getStatus())) {
            logger.warn("Login failed: Account not active: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account is not active");
        }
        
        // ✅ Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        
        logger.info("Login successful for email: {} (ID: {})", user.getEmail(), user.getId());
        
        // ✅ NEW: Return accessToken only (production format)
        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserById(String id) {
        logger.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return convertToDTO(user);
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return convertToDTO(user);
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public UserDTO updateUser(String id, UserDTO userDTO) {
        logger.info("Updating user: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getStatus() != null) {
            validateStatus(userDTO.getStatus());
            user.setStatus(userDTO.getStatus());
        }
        if (userDTO.getRole() != null) {
            validateRole(userDTO.getRole());
            user.setRole(userDTO.getRole());
        }
        
        user = userRepository.save(user);
        logger.info("User updated successfully: {}", id);
        return convertToDTO(user);
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
    
    // ==================== ADMIN METHODS ====================
    
    /**
     * Admin: Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Admin: Update user role
     */
    @Transactional
    public void updateUserRole(String id, String newRole) {
        logger.info("Admin: Updating role for user: {} to {}", id, newRole);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Validate role
        validateRole(newRole);
        
        user.setRole(newRole);
        userRepository.save(user);
        logger.info("User role updated successfully: {}", id);
    }
    
    /**
     * Admin: Update user status (enable/disable)
     */
    @Transactional
    public void updateUserStatus(String id, String newStatus) {
        logger.info("Admin: Updating status for user: {} to {}", id, newStatus);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Validate status
        validateStatus(newStatus);
        
        user.setStatus(newStatus);
        userRepository.save(user);
        logger.info("User status updated successfully: {}", id);
    }
    
    /**
     * Validate role
     */
    private void validateRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new BusinessException("INVALID_ROLE", "Role cannot be empty");
        }
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new BusinessException("INVALID_ROLE", "Invalid role. Must be USER or ADMIN");
        }
    }
    
    /**
     * Validate status
     */
    private void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BusinessException("INVALID_STATUS", "Status cannot be empty");
        }
        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            throw new BusinessException("INVALID_STATUS", "Invalid status. Must be ACTIVE or INACTIVE");
        }
    }
}

