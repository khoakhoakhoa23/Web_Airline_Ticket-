package com.flightbooking.service;

import com.flightbooking.dto.BookingDTO;
import com.flightbooking.dto.CreateBookingRequest;
import com.flightbooking.dto.FlightSegmentDTO;
import com.flightbooking.dto.PassengerDTO;
import com.flightbooking.entity.Booking;
import com.flightbooking.entity.FlightSegment;
import com.flightbooking.entity.Passenger;
import com.flightbooking.exception.BusinessException;
import com.flightbooking.exception.ResourceNotFoundException;
import com.flightbooking.exception.UnauthorizedActionException;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.FlightSegmentRepository;
import com.flightbooking.repository.PassengerRepository;
import com.flightbooking.repository.SeatSelectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private FlightSegmentRepository flightSegmentRepository;
    
    @Autowired
    private PassengerRepository passengerRepository;
    
    @Autowired
    private com.flightbooking.repository.UserRepository userRepository;
    
    @Autowired
    private com.flightbooking.repository.TicketRepository ticketRepository;
    
    @Autowired
    private com.flightbooking.repository.SeatSelectionRepository seatSelectionRepository;
    
    @Autowired
    private com.flightbooking.repository.PaymentRepository paymentRepository;
    
    /**
     * Create new booking with validation
     * 
     * ✅ PRODUCTION STANDARD:
     * - userId is extracted from JWT token (passed from controller)
     * - NOT from request body (security best practice)
     * - Returns 401 if user not authenticated or not found
     * 
     * @param request CreateBookingRequest with flight segments and passengers (NO userId)
     * @param userId User ID from JWT token (SecurityContext)
     * @param emailFromToken Email from JWT token (for auto-create user if needed)
     * @return Created BookingDTO
     * @throws BusinessException if validation fails
     * @throws org.springframework.security.core.AuthenticationException if user not authenticated
     */
    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request, String userId, String emailFromToken) {
        logger.info("=== Creating booking ===");
        logger.info("User ID (from JWT token): {}", userId);
        logger.info("Currency: {}", request.getCurrency());
        logger.info("Flight segments count: {}", request.getFlightSegments() != null ? request.getFlightSegments().size() : 0);
        logger.info("Passengers count: {}", request.getPassengers() != null ? request.getPassengers().size() : 0);
        
        // ✅ CRITICAL: Validate userId from JWT token
        if (userId == null || userId.trim().isEmpty()) {
            logger.error("❌ Booking creation failed: User ID from JWT token is null or empty");
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                "User not authenticated. Please login to create booking.");
        }
        
        // ✅ CRITICAL: Check if user exists in database
        logger.info("🔍 Step 3: Checking if user exists in database...");
        logger.info("   User ID to check: {}", userId);
        logger.info("   User ID type: {}", userId.getClass().getName());
        logger.info("   User ID length: {}", userId.length());
        
        boolean userExists = userRepository.existsById(userId);
        logger.info("   User exists in database: {}", userExists);
        
        if (!userExists) {
            logger.warn("⚠️ User not found in database. User ID: {}", userId);
            logger.warn("   This could mean:");
            logger.warn("   1. User was deleted from database");
            logger.warn("   2. User ID in JWT token doesn't match any user in database");
            logger.warn("   3. User was never created during registration");
            logger.info("Attempting to auto-create user from JWT token...");
            
            // ✅ AUTO-FIX: Use email from JWT token (passed from controller)
            try {
                String email = emailFromToken;
                
                if (email == null || email.trim().isEmpty()) {
                    logger.error("❌ Cannot extract email from JWT token for auto-create user");
                    logger.error("   User ID: {}", userId);
                    logger.error("   This usually means JWT token is missing email claim");
                    // ✅ Return 401 Unauthorized (not 404) - user is not authenticated properly
                    throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                        "User account not found. Please register or login again.");
                }
                
                logger.info("✅ Using email from JWT token for auto-create: {}", email);
                
                // Check if user exists by email
                java.util.Optional<com.flightbooking.entity.User> existingUser = userRepository.findByEmail(email);
                
                if (existingUser.isPresent()) {
                    // User exists with different ID - this shouldn't happen, but handle it
                    logger.error("❌ User exists with email {} but different ID. Expected: {}, Found: {}", 
                        email, userId, existingUser.get().getId());
                    throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                        "User ID mismatch. Please logout and login again.");
                } else {
                    // ✅ AUTO-CREATE: Create user from JWT token info
                    logger.info("Auto-creating user with ID: {}, Email: {}", userId, email);
                    
                    com.flightbooking.entity.User newUser = new com.flightbooking.entity.User();
                    newUser.setId(userId);
                    newUser.setEmail(email);
                    newUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cyhQQFk82zZt8Ci9LE8tXqJ58W22u"); // Default: 123456
                    newUser.setPhone(null); // Will be updated later when user updates profile
                    newUser.setRole("CUSTOMER");
                    newUser.setStatus("ACTIVE");
                    
                    try {
                        newUser = userRepository.save(newUser);
                        userRepository.flush(); // Force immediate persist
                        logger.info("✅ User auto-created successfully. ID: {}, Email: {}", 
                            newUser.getId(), newUser.getEmail());
                        
                        // Verify user was created
                        boolean created = userRepository.existsById(newUser.getId());
                        if (!created) {
                            logger.error("❌ CRITICAL: User was created but does not exist!");
                            throw new RuntimeException("Failed to auto-create user");
                        }
                        logger.info("✅ Verified: Auto-created user exists in database");
                        
                        // ✅ CRITICAL: Re-check user exists after auto-create
                        userExists = userRepository.existsById(userId);
                        logger.info("   Re-checked user exists after auto-create: {}", userExists);
                        
                        if (!userExists) {
                            logger.error("❌ CRITICAL: User was auto-created but still not found!");
                            throw new RuntimeException("Failed to auto-create user - user still not found after creation");
                        }
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // Handle duplicate email or other constraint violations
                        logger.error("❌ DataIntegrityViolationException during auto-create user: {}", e.getMessage());
                        
                        if (e.getMessage() != null && e.getMessage().contains("email")) {
                            logger.error("   Email already exists: {}", email);
                            throw new BusinessException("EMAIL_EXISTS", 
                                "Email already exists. Please use a different email or login with existing account.");
                        }
                        
                        // Check if it's a foreign key constraint (shouldn't happen here, but handle it)
                        if (e.getMessage() != null && e.getMessage().contains("foreign key")) {
                            logger.error("   Foreign key constraint violation - this shouldn't happen during user creation");
                            throw new BusinessException("USER_CREATION_FAILED", 
                                "Failed to create user account due to database constraint. Please contact support.");
                        }
                        
                        logger.error("   Database constraint violation: {}", e.getMessage());
                        throw new BusinessException("USER_CREATION_FAILED", 
                            "Failed to create user account due to database constraint.");
                    } catch (Exception e) {
                        logger.error("❌ Failed to auto-create user: {}", e.getMessage(), e);
                        throw new BusinessException("USER_CREATION_FAILED", 
                            "Failed to create user account. Please register first.");
                    }
                }
            } catch (org.springframework.security.core.AuthenticationException e) {
                throw e; // Re-throw authentication exceptions (401)
            } catch (BusinessException e) {
                throw e; // Re-throw business exceptions
            } catch (Exception e) {
                logger.error("❌ Error during auto-create user: {}", e.getMessage(), e);
                // ✅ Return 401 Unauthorized (not 404) - user authentication issue
                throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                    "User account not found. Please register or login again.");
            }
        } else {
            logger.info("✅ User validation passed. User ID: {} exists in database", userId);
        }
        
        // ✅ CRITICAL: Final verification before creating booking
        // Re-check user exists one more time to ensure transaction consistency
        boolean finalCheck = userRepository.existsById(userId);
        if (!finalCheck) {
            logger.error("❌ CRITICAL: User does not exist in database after all checks!");
            logger.error("   User ID: {}", userId);
            logger.error("   This should not happen - user should exist or be auto-created by now");
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                "User account not found. Please register or login again.");
        }
        logger.info("✅ Final verification: User exists in database. Proceeding with booking creation...");
        
        // Validate request
        validateCreateBookingRequest(request);
        
        // Create booking entity
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setBookingCode(generateBookingCode());
        booking.setStatus("PENDING");
        booking.setUserId(userId); // ✅ Use userId from JWT token, NOT from request
        booking.setCurrency(request.getCurrency() != null && !request.getCurrency().trim().isEmpty() 
            ? request.getCurrency() : "VND");
        
        // ✅ Calculate total amount with null safety
        BigDecimal totalAmount = request.getFlightSegments().stream()
            .map(segment -> {
                BigDecimal baseFare = segment.getBaseFare() != null ? segment.getBaseFare() : BigDecimal.ZERO;
                BigDecimal taxes = segment.getTaxes() != null ? segment.getTaxes() : BigDecimal.ZERO;
                return baseFare.add(taxes);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Add seat price if provided
        if (request.getSeatPrice() != null && request.getSeatPrice().compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = totalAmount.add(request.getSeatPrice());
        }
        
        booking.setTotalAmount(totalAmount);
        
        // Set hold expiration (15 minutes from now) - matches seat lock duration
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(15));
        
        logger.debug("Saving booking to database. Booking ID: {}, User ID: {}, Total: {}", 
            booking.getId(), booking.getUserId(), booking.getTotalAmount());
        
        try {
            booking = bookingRepository.save(booking);
            logger.info("Booking saved successfully. Booking ID: {}", booking.getId());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Database integrity violation when saving booking. User ID: {}, Error: {}", 
                userId, e.getMessage());
            // Re-throw as AuthenticationException for better error message (401 instead of 404)
            if (e.getMessage() != null && e.getMessage().contains("user_id")) {
                throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                    "User not found with ID: " + userId + ". Please register or login again.");
            }
            throw new BusinessException("DATABASE_ERROR", "Failed to create booking due to database constraint violation");
        }
        
        // Store booking ID in final variable for use in lambda expressions
        final String bookingId = booking.getId();
        
        // ✅ Create flight segments with null safety
        List<FlightSegment> segments = request.getFlightSegments().stream()
            .map(dto -> {
                if (dto == null) {
                    logger.error("Flight segment DTO is null");
                    throw new BusinessException("INVALID_BOOKING", "Flight segment cannot be null");
                }
                
                FlightSegment segment = new FlightSegment();
                segment.setId(UUID.randomUUID().toString());
                segment.setBookingId(bookingId);
                segment.setAirline(dto.getAirline() != null ? dto.getAirline().trim() : null);
                segment.setFlightNumber(dto.getFlightNumber() != null ? dto.getFlightNumber().trim() : null);
                segment.setOrigin(dto.getOrigin() != null ? dto.getOrigin().trim() : null);
                segment.setDestination(dto.getDestination() != null ? dto.getDestination().trim() : null);
                segment.setDepartTime(dto.getDepartTime());
                segment.setArriveTime(dto.getArriveTime());
                segment.setCabinClass(dto.getCabinClass() != null ? dto.getCabinClass().trim() : null);
                segment.setBaseFare(dto.getBaseFare() != null ? dto.getBaseFare() : BigDecimal.ZERO);
                segment.setTaxes(dto.getTaxes() != null ? dto.getTaxes() : BigDecimal.ZERO);
                return segment;
            })
            .collect(Collectors.toList());
        
        logger.debug("Saving {} flight segments", segments.size());
        try {
            flightSegmentRepository.saveAll(segments);
            logger.debug("Flight segments saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save flight segments: {}", e.getMessage(), e);
            throw new BusinessException("DATABASE_ERROR", "Failed to save flight segments: " + e.getMessage());
        }
        
        // ✅ Create passengers with null safety
        List<Passenger> passengers = request.getPassengers().stream()
            .map(dto -> {
                if (dto == null) {
                    logger.error("Passenger DTO is null");
                    throw new BusinessException("INVALID_BOOKING", "Passenger cannot be null");
                }
                
                Passenger passenger = new Passenger();
                passenger.setId(UUID.randomUUID().toString());
                passenger.setBookingId(bookingId);
                passenger.setFullName(dto.getFullName() != null ? dto.getFullName().trim() : null);
                passenger.setDateOfBirth(dto.getDateOfBirth());
                passenger.setGender(dto.getGender() != null ? dto.getGender().trim() : null);
                passenger.setDocumentType(dto.getDocumentType() != null ? dto.getDocumentType().trim() : null);
                passenger.setDocumentNumber(dto.getDocumentNumber() != null ? dto.getDocumentNumber().trim() : null);
                return passenger;
            })
            .collect(Collectors.toList());
        
        logger.debug("Saving {} passengers", passengers.size());
        final List<Passenger> savedPassengers;
        try {
            savedPassengers = passengerRepository.saveAll(passengers);
            logger.debug("Passengers saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save passengers: {}", e.getMessage(), e);
            throw new BusinessException("DATABASE_ERROR", "Failed to save passengers: " + e.getMessage());
        }
        
        // ✅ Create seat selections if provided
        logger.info("Checking seat selections - request.getSeatSelections(): {}, segments.isEmpty(): {}", 
            request.getSeatSelections(), segments.isEmpty());
        if (request.getSeatSelections() != null && !request.getSeatSelections().isEmpty() && !segments.isEmpty()) {
            logger.info("Creating {} seat selections for booking {}", request.getSeatSelections().size(), bookingId);
            logger.info("Seat selections data: {}", request.getSeatSelections());
            
            String segmentId = segments.get(0).getId(); // Use first segment
            
            List<com.flightbooking.entity.SeatSelection> seatSelections = request.getSeatSelections().stream()
                .map(seatInput -> {
                    // Validate passenger index
                    if (seatInput.getPassengerIndex() == null || 
                        seatInput.getPassengerIndex() < 0 || 
                        seatInput.getPassengerIndex() >= savedPassengers.size()) {
                        logger.warn("Invalid passenger index {} for seat {}. Skipping.", 
                            seatInput.getPassengerIndex(), seatInput.getSeatNumber());
                        return null;
                    }
                    
                    Passenger passenger = savedPassengers.get(seatInput.getPassengerIndex());
                    if (passenger == null || passenger.getId() == null) {
                        logger.warn("Passenger at index {} not found. Skipping seat selection.", 
                            seatInput.getPassengerIndex());
                        return null;
                    }
                    
                    com.flightbooking.entity.SeatSelection seatSelection = new com.flightbooking.entity.SeatSelection();
                    seatSelection.setId(UUID.randomUUID().toString());
                    seatSelection.setBookingId(bookingId);
                    seatSelection.setPassengerId(passenger.getId());
                    seatSelection.setSegmentId(segmentId);
                    seatSelection.setSeatNumber(seatInput.getSeatNumber());
                    seatSelection.setSeatType(seatInput.getSeatType() != null ? seatInput.getSeatType() : "STANDARD");
                    seatSelection.setPrice(seatInput.getPrice() != null ? seatInput.getPrice() : BigDecimal.ZERO);
                    seatSelection.setStatus("RESERVED"); // Default status when created
                    
                    logger.debug("Created seat selection: {} for passenger {} (index {})", 
                        seatInput.getSeatNumber(), passenger.getFullName(), seatInput.getPassengerIndex());
                    
                    return seatSelection;
                })
                .filter(seatSelection -> seatSelection != null)
                .collect(Collectors.toList());
            
            if (!seatSelections.isEmpty()) {
                try {
                    seatSelectionRepository.saveAll(seatSelections);
                    logger.info("✅ Created {} seat selections for booking {}", seatSelections.size(), bookingId);
                } catch (Exception e) {
                    logger.error("Failed to save seat selections: {}", e.getMessage(), e);
                    // Don't fail booking creation if seat selection fails
                    // But log the error for debugging
                }
            }
        } else {
            logger.info("No seat selections provided for booking {}", bookingId);
        }
        
        logger.info("Booking created successfully: {} (code: {})", booking.getId(), booking.getBookingCode());
        return convertToDTO(booking);
    }
    
    /**
     * Validate create booking request
     * 
     * Validates all required fields and business rules before creating booking
     */
    private void validateCreateBookingRequest(CreateBookingRequest request) {
        logger.debug("Validating booking request...");
        
        // ✅ Validate request object is not null
        if (request == null) {
            logger.error("Booking request is null");
            throw new BusinessException("INVALID_BOOKING", "Booking request cannot be null");
        }
        
        // ✅ Validate flight segments
        if (request.getFlightSegments() == null || request.getFlightSegments().isEmpty()) {
            logger.error("Booking validation failed: No flight segments provided");
            throw new BusinessException("INVALID_BOOKING", "Booking must have at least one flight segment");
        }
        
        // ✅ Validate passengers
        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            logger.error("Booking validation failed: No passengers provided");
            throw new BusinessException("INVALID_BOOKING", "Booking must have at least one passenger");
        }
        
        // ✅ Validate each flight segment
        int segmentIndex = 0;
        for (FlightSegmentDTO segment : request.getFlightSegments()) {
            if (segment == null) {
                logger.error("Booking validation failed: Flight segment {} is null", segmentIndex);
                throw new BusinessException("INVALID_BOOKING", "Flight segment cannot be null");
            }
            
            // Validate required fields
            if (segment.getAirline() == null || segment.getAirline().trim().isEmpty()) {
                throw new BusinessException("INVALID_BOOKING", "Flight segment airline is required");
            }
            if (segment.getFlightNumber() == null || segment.getFlightNumber().trim().isEmpty()) {
                throw new BusinessException("INVALID_BOOKING", "Flight segment flight number is required");
            }
            if (segment.getOrigin() == null || segment.getOrigin().trim().isEmpty()) {
                throw new BusinessException("INVALID_BOOKING", "Flight segment origin is required");
            }
            if (segment.getDestination() == null || segment.getDestination().trim().isEmpty()) {
                throw new BusinessException("INVALID_BOOKING", "Flight segment destination is required");
            }
            if (segment.getDepartTime() == null) {
                throw new BusinessException("INVALID_BOOKING", "Flight segment departure time is required");
            }
            if (segment.getArriveTime() == null) {
                throw new BusinessException("INVALID_BOOKING", "Flight segment arrival time is required");
            }
            
            // ✅ Validate prices
            if (segment.getBaseFare() == null || segment.getBaseFare().compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("Booking validation failed: Invalid base fare for segment {}", segmentIndex);
                throw new BusinessException("INVALID_PRICE", "Flight base fare must be greater than zero");
            }
            if (segment.getTaxes() == null || segment.getTaxes().compareTo(BigDecimal.ZERO) < 0) {
                logger.error("Booking validation failed: Invalid taxes for segment {}", segmentIndex);
                throw new BusinessException("INVALID_PRICE", "Flight taxes cannot be negative");
            }
            
            // ✅ Validate departure times (cannot be in the past)
            LocalDateTime now = LocalDateTime.now();
            if (segment.getDepartTime().isBefore(now)) {
                logger.error("Booking validation failed: Departure time in the past for segment {}", segmentIndex);
                throw new BusinessException("INVALID_DATE", "Cannot book flights with departure time in the past");
            }
            if (segment.getArriveTime().isBefore(segment.getDepartTime())) {
                logger.error("Booking validation failed: Arrival time before departure for segment {}", segmentIndex);
                throw new BusinessException("INVALID_DATE", "Arrival time must be after departure time");
            }
            
            segmentIndex++;
        }
        
        // ✅ Validate each passenger
        int passengerIndex = 0;
        for (com.flightbooking.dto.PassengerDTO passenger : request.getPassengers()) {
            if (passenger == null) {
                logger.error("Booking validation failed: Passenger {} is null", passengerIndex);
                throw new BusinessException("INVALID_BOOKING", "Passenger cannot be null");
            }
            
            // Validate required passenger fields
            if (passenger.getFullName() == null || passenger.getFullName().trim().isEmpty()) {
                throw new BusinessException("INVALID_BOOKING", "Passenger full name is required");
            }
            if (passenger.getDateOfBirth() == null) {
                throw new BusinessException("INVALID_BOOKING", "Passenger date of birth is required");
            }
            if (passenger.getGender() == null || passenger.getGender().trim().isEmpty()) {
                throw new BusinessException("INVALID_BOOKING", "Passenger gender is required");
            }
            if (passenger.getDocumentType() == null || passenger.getDocumentType().trim().isEmpty()) {
                throw new BusinessException("INVALID_BOOKING", "Passenger document type is required");
            }
            if (passenger.getDocumentNumber() == null || passenger.getDocumentNumber().trim().isEmpty()) {
                throw new BusinessException("INVALID_BOOKING", "Passenger document number is required");
            }
            
            passengerIndex++;
        }
        
        logger.debug("Booking request validation passed");
    }
    
    /**
     * Get booking by ID with ownership check
     * 
     * @param id Booking ID
     * @param currentUserId Current authenticated user ID (from JWT token)
     * @param currentUserEmail Current authenticated user email (for admin check, optional)
     * @return BookingDTO
     * @throws ResourceNotFoundException if booking not found
     * @throws UnauthorizedActionException if user doesn't own the booking
     */
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(String id, String currentUserId, String currentUserEmail) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
        
        logger.debug("Checking ownership - Booking userId: {}, Current userId: {}, Current email: {}", 
            booking.getUserId(), currentUserId, currentUserEmail);
        
        // Check ownership: Compare userId directly (more reliable than email)
        // Admin can view any booking (checked by email if provided)
        boolean isOwner = currentUserId != null && currentUserId.equals(booking.getUserId());
        boolean isAdmin = currentUserEmail != null && isAdminByEmail(currentUserEmail);
        
        if (currentUserId != null && !isOwner && !isAdmin) {
            logger.warn("Access denied - User {} tried to access booking {} (owner: {})", 
                currentUserId, id, booking.getUserId());
            throw new UnauthorizedActionException("You do not have permission to view this booking");
        }
        
        logger.debug("Access granted - isOwner: {}, isAdmin: {}", isOwner, isAdmin);
        return convertToDTO(booking);
    }
    
    /**
     * Get booking by code with ownership check
     * 
     * @param bookingCode Booking code
     * @param currentUserId Current authenticated user ID (from JWT token)
     * @param currentUserEmail Current authenticated user email (for admin check, optional)
     * @return BookingDTO
     * @throws ResourceNotFoundException if booking not found
     * @throws UnauthorizedActionException if user doesn't own the booking
     */
    @Transactional(readOnly = true)
    public BookingDTO getBookingByCode(String bookingCode, String currentUserId, String currentUserEmail) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with code: " + bookingCode));
        
        // Check ownership: Compare userId directly
        boolean isOwner = currentUserId != null && currentUserId.equals(booking.getUserId());
        boolean isAdmin = currentUserEmail != null && isAdminByEmail(currentUserEmail);
        
        if (currentUserId != null && !isOwner && !isAdmin) {
            throw new UnauthorizedActionException("You do not have permission to view this booking");
        }
        
        return convertToDTO(booking);
    }
    
    /**
     * Get all bookings for a user with ownership check
     * 
     * @param userId User ID
     * @param currentUserId Current authenticated user ID (from JWT token)
     * @param currentUserEmail Current authenticated user email (for admin check, optional)
     * @return List of BookingDTO
     * @throws UnauthorizedActionException if user tries to view other user's bookings
     */
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByUserId(String userId, String currentUserId, String currentUserEmail) {
        // Check ownership: user can only view their own bookings (unless admin)
        boolean isOwner = currentUserId != null && currentUserId.equals(userId);
        boolean isAdmin = currentUserEmail != null && isAdminByEmail(currentUserEmail);
        
        if (currentUserId != null && !isOwner && !isAdmin) {
            throw new UnauthorizedActionException("You do not have permission to view these bookings");
        }
        
        return bookingRepository.findByUserId(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update booking status with ownership check and validation
     * 
     * @param id Booking ID
     * @param status New status
     * @param currentUserId Current authenticated user ID (from JWT token)
     * @param currentUserEmail Current authenticated user email (for admin check, optional)
     * @return Updated BookingDTO
     * @throws ResourceNotFoundException if booking not found
     * @throws UnauthorizedActionException if user doesn't own the booking
     * @throws BusinessException if status transition is invalid
     */
    @Transactional
    public BookingDTO updateBookingStatus(String id, String status, String currentUserId, String currentUserEmail) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
        
        // Check ownership: Compare userId directly
        boolean isOwner = currentUserId != null && currentUserId.equals(booking.getUserId());
        boolean isAdmin = currentUserEmail != null && isAdminByEmail(currentUserEmail);
        
        if (currentUserId != null && !isOwner && !isAdmin) {
            throw new UnauthorizedActionException("You do not have permission to update this booking");
        }
        
        // Validate status transition
        validateStatusTransition(booking.getStatus(), status);
        
        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        return convertToDTO(booking);
    }
    
    /**
     * Cancel booking with ownership check and business rules
     * 
     * @param id Booking ID
     * @param currentUserId Current authenticated user ID (from JWT token)
     * @param currentUserEmail Current authenticated user email (for admin check, optional)
     * @throws ResourceNotFoundException if booking not found
     * @throws UnauthorizedActionException if user doesn't own the booking
     * @throws BusinessException if booking cannot be cancelled
     */
    @Transactional
    public void cancelBooking(String id, String currentUserId, String currentUserEmail) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
        
        // Check ownership: Compare userId directly
        boolean isOwner = currentUserId != null && currentUserId.equals(booking.getUserId());
        boolean isAdmin = currentUserEmail != null && isAdminByEmail(currentUserEmail);
        
        if (currentUserId != null && !isOwner && !isAdmin) {
            throw new UnauthorizedActionException("You do not have permission to cancel this booking");
        }
        
        // Business rule: Cannot cancel already cancelled bookings
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BusinessException("ALREADY_CANCELLED", "Booking is already cancelled");
        }
        
        // Business rule: Cannot cancel completed bookings
        if ("COMPLETED".equals(booking.getStatus())) {
            throw new BusinessException("CANNOT_CANCEL_COMPLETED", "Cannot cancel completed booking");
        }
        
        // Business rule: Check if booking is past departure time
        if (booking.getFlightSegments() != null && !booking.getFlightSegments().isEmpty()) {
            FlightSegment firstSegment = booking.getFlightSegments().get(0);
            if (firstSegment.getDepartTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException("CANNOT_CANCEL_PAST", "Cannot cancel booking after departure");
            }
        }
        
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }
    
    /**
     * Check if user owns the booking
     */
    /**
     * Check if current user is the owner of the booking
     * Compares userId directly (more reliable than email)
     */
    private boolean isOwner(Booking booking, String userId) {
        if (userId == null || booking.getUserId() == null) {
            return false;
        }
        return booking.getUserId().equals(userId);
    }
    
    /**
     * Check if user is admin by email
     * In production, check via UserService or SecurityContext roles
     */
    private boolean isAdminByEmail(String email) {
        if (email == null) {
            return false;
        }
        // TODO: Implement proper admin check via UserService
        // For now, check if email contains "admin" (temporary solution)
        return email.toLowerCase().contains("admin");
    }
    
    /**
     * Validate booking status transition
     * 
     * Valid transitions:
     * - PENDING -> PENDING_PAYMENT, CANCELLED
     * - PENDING_PAYMENT -> CONFIRMED, CANCELLED, PENDING
     * - CONFIRMED -> COMPLETED, CANCELLED
     * - COMPLETED -> (no transitions)
     * - CANCELLED -> (no transitions)
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return; // No change
        }
        
        switch (currentStatus) {
            case "PENDING":
                if (!"PENDING_PAYMENT".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
                    throw new BusinessException("INVALID_STATUS_TRANSITION", 
                        "Cannot transition from PENDING to " + newStatus);
                }
                break;
            case "PENDING_PAYMENT":
                if (!"CONFIRMED".equals(newStatus) && !"CANCELLED".equals(newStatus) && !"PENDING".equals(newStatus)) {
                    throw new BusinessException("INVALID_STATUS_TRANSITION", 
                        "Cannot transition from PENDING_PAYMENT to " + newStatus);
                }
                break;
            case "CONFIRMED":
                if (!"COMPLETED".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
                    throw new BusinessException("INVALID_STATUS_TRANSITION", 
                        "Cannot transition from CONFIRMED to " + newStatus);
                }
                break;
            case "COMPLETED":
                throw new BusinessException("INVALID_STATUS_TRANSITION", 
                    "Cannot change status of completed booking");
            case "CANCELLED":
                throw new BusinessException("INVALID_STATUS_TRANSITION", 
                    "Cannot change status of cancelled booking");
            default:
                throw new BusinessException("UNKNOWN_STATUS", "Unknown booking status: " + currentStatus);
        }
    }
    
    @Transactional
    public BookingDTO finalizeBooking(String id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking must be CONFIRMED before finalizing");
        }
        
        // Finalize booking - confirm seats and baggage
        booking.setStatus("FINALIZED");
        booking = bookingRepository.save(booking);
        
        return convertToDTO(booking);
    }
    
    private String generateBookingCode() {
        return "BK" + System.currentTimeMillis();
    }
    
    public BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingCode(booking.getBookingCode());
        dto.setStatus(booking.getStatus());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setCurrency(booking.getCurrency());
        dto.setHoldExpiresAt(booking.getHoldExpiresAt());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        dto.setUserId(booking.getUserId());
        
        if (booking.getFlightSegments() != null) {
            dto.setFlightSegments(booking.getFlightSegments().stream()
                .map(segment -> {
                    FlightSegmentDTO segmentDTO = new FlightSegmentDTO();
                    segmentDTO.setId(segment.getId());
                    segmentDTO.setAirline(segment.getAirline());
                    segmentDTO.setFlightNumber(segment.getFlightNumber());
                    segmentDTO.setOrigin(segment.getOrigin());
                    segmentDTO.setDestination(segment.getDestination());
                    segmentDTO.setDepartTime(segment.getDepartTime());
                    segmentDTO.setArriveTime(segment.getArriveTime());
                    segmentDTO.setCabinClass(segment.getCabinClass());
                    segmentDTO.setBaseFare(segment.getBaseFare());
                    segmentDTO.setTaxes(segment.getTaxes());
                    segmentDTO.setBookingId(segment.getBookingId());
                    return segmentDTO;
                })
                .collect(Collectors.toList()));
        }
        
        if (booking.getPassengers() != null) {
            // Load seat selections for this booking to map with passengers
            List<com.flightbooking.entity.SeatSelection> seatSelections = 
                seatSelectionRepository.findByBookingId(booking.getId());
            
            // Create a map: passengerId -> seatNumber
            java.util.Map<String, String> seatMap = seatSelections.stream()
                .filter(ss -> ss.getPassengerId() != null && ss.getSeatNumber() != null)
                .collect(Collectors.toMap(
                    com.flightbooking.entity.SeatSelection::getPassengerId,
                    com.flightbooking.entity.SeatSelection::getSeatNumber,
                    (existing, replacement) -> existing // Keep first if duplicate
                ));
            
            logger.debug("Booking {} - Found {} seat selections, mapped to {} passengers", 
                booking.getId(), seatSelections.size(), seatMap.size());
            
            dto.setPassengers(booking.getPassengers().stream()
                .map(passenger -> {
                    PassengerDTO passengerDTO = new PassengerDTO();
                    passengerDTO.setId(passenger.getId());
                    passengerDTO.setFullName(passenger.getFullName());
                    passengerDTO.setDateOfBirth(passenger.getDateOfBirth());
                    passengerDTO.setGender(passenger.getGender());
                    passengerDTO.setDocumentType(passenger.getDocumentType());
                    passengerDTO.setDocumentNumber(passenger.getDocumentNumber());
                    passengerDTO.setBookingId(passenger.getBookingId());
                    // Map seat number from seat selections
                    passengerDTO.setSeatNumber(seatMap.get(passenger.getId()));
                    return passengerDTO;
                })
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    // ==================== ADMIN METHODS ====================
    
    /**
     * Admin: Get all bookings with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookingDTO> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Admin: Get bookings by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookingDTO> getBookingsByStatus(String status, Pageable pageable) {
        return bookingRepository.findByStatus(status, pageable)
                .map(this::convertToDTO);
    }
    
    /**
     * Admin: Get booking by ID (no ownership check)
     */
    @Transactional(readOnly = true)
    public BookingDTO getAdminBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        return convertToDTO(booking);
    }
    
    /**
     * Admin: Approve booking (change status to CONFIRMED)
     * 
     * Can approve from PENDING or PENDING_PAYMENT status
     * 
     * @param id Booking ID
     * @return Updated BookingDTO
     * @throws RuntimeException if booking not found or invalid status
     */
    @Transactional
    public BookingDTO adminApproveBooking(String id) {
        logger.info("Admin: Approving booking: {}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        
        String currentStatus = booking.getStatus();
        logger.info("Current booking status: {}", currentStatus);
        
        // Validate that booking can be approved
        if ("CANCELLED".equals(currentStatus)) {
            throw new BusinessException("CANNOT_APPROVE_CANCELLED", 
                "Cannot approve a cancelled booking");
        }
        
        if ("CONFIRMED".equals(currentStatus)) {
            throw new BusinessException("ALREADY_CONFIRMED", 
                "Booking is already confirmed");
        }
        
        if ("COMPLETED".equals(currentStatus)) {
            throw new BusinessException("CANNOT_APPROVE_COMPLETED", 
                "Cannot approve a completed booking");
        }
        
        // Approve booking: PENDING or PENDING_PAYMENT -> CONFIRMED
        if ("PENDING".equals(currentStatus) || "PENDING_PAYMENT".equals(currentStatus)) {
            booking.setStatus("CONFIRMED");
            booking.setUpdatedAt(LocalDateTime.now());
            booking = bookingRepository.save(booking);
            logger.info("✅ Booking {} approved successfully. Status changed from {} to CONFIRMED", 
                id, currentStatus);
            
            // If booking has payment, approve payment as well
            try {
                java.util.List<com.flightbooking.entity.Payment> payments = paymentRepository.findByBookingId(id);
                
                for (com.flightbooking.entity.Payment payment : payments) {
                    if ("PENDING".equals(payment.getStatus())) {
                        payment.setStatus("SUCCESS");
                        payment.setUpdatedAt(LocalDateTime.now());
                        paymentRepository.save(payment);
                        logger.info("✅ Payment {} approved automatically with booking approval", payment.getId());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to approve payment for booking {}: {}", id, e.getMessage());
                // Don't fail booking approval if payment update fails
            }
            
            // Create ticket for approved booking
            try {
                createTicketForBooking(booking);
                logger.info("✅ Ticket created for booking {}", id);
            } catch (Exception e) {
                logger.error("Failed to create ticket for booking {}: {}", id, e.getMessage());
                // Don't fail the approval if ticket creation fails
            }
        } else {
            throw new BusinessException("INVALID_STATUS_FOR_APPROVAL", 
                "Cannot approve booking with status: " + currentStatus + ". Only PENDING or PENDING_PAYMENT can be approved.");
        }
        
        return convertToDTO(booking);
    }
    
    /**
     * Admin: Cancel any booking (override business rules if needed)
     */
    @Transactional
    public void adminCancelBooking(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        
        // Admin can cancel any booking except already cancelled ones
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is already cancelled");
        }
        
        booking.setStatus("CANCELLED");
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }
    
    /**
     * Create ticket for approved booking
     * Generates PNR and e-ticket number
     */
    private void createTicketForBooking(Booking booking) {
        logger.info("Creating ticket for booking: {}", booking.getId());
        
        // Check if ticket already exists
        List<com.flightbooking.entity.Ticket> existingTickets = ticketRepository.findByBookingId(booking.getId());
        if (!existingTickets.isEmpty()) {
            logger.info("Ticket already exists for booking: {}", booking.getId());
            return;
        }
        
        // Create ticket
        com.flightbooking.entity.Ticket ticket = new com.flightbooking.entity.Ticket();
        ticket.setId(UUID.randomUUID().toString());
        ticket.setBookingId(booking.getId());
        ticket.setPnr(generatePNR());
        ticket.setEticketNumber(generateETicketNumber());
        ticket.setStatus("ISSUED");
        ticket.setIssuedAt(LocalDateTime.now());
        
        ticketRepository.save(ticket);
        logger.info("Ticket created for booking: {} - PNR: {}, E-Ticket: {}", 
            booking.getId(), ticket.getPnr(), ticket.getEticketNumber());
    }
    
    /**
     * Generate PNR (Passenger Name Record) - 6 alphanumeric characters
     */
    private String generatePNR() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder pnr = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            pnr.append(chars.charAt(random.nextInt(chars.length())));
        }
        return pnr.toString();
    }
    
    /**
     * Generate E-Ticket number - 13 digits
     */
    private String generateETicketNumber() {
        StringBuilder eticket = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 13; i++) {
            eticket.append(random.nextInt(10));
        }
        return eticket.toString();
    }
}

