package com.flightbooking.service;

import com.flightbooking.dto.PaymentCreateRequest;
import com.flightbooking.dto.PaymentResponse;
import com.flightbooking.entity.Booking;
import com.flightbooking.entity.Payment;
import com.flightbooking.exception.BusinessException;
import com.flightbooking.exception.PaymentFailedException;
import com.flightbooking.exception.ResourceNotFoundException;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.PaymentRepository;
// Stripe imports removed - using manual admin approval flow instead
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Payment Service
 * 
 * Handles payment operations with multiple payment providers
 * Currently supports:
 * - Stripe (international cards)
 * - VNPay (Vietnamese gateway) - placeholder
 * - Momo (Vietnamese e-wallet) - placeholder
 */
@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private com.flightbooking.service.SeatSelectionService seatSelectionService;
    
    @Autowired
    private com.flightbooking.service.SeatLockService seatLockService;
    
    @Autowired
    private com.flightbooking.service.BookingService bookingService;
    
    @Autowired
    private com.flightbooking.service.TicketService ticketService;
    
    /**
     * Stripe Secret Key (sk_test_... or sk_live_...)
     * NEVER expose this to frontend - only used server-side
     * 
     * Why stripe.secret.key instead of stripe.api.key?
     * - More explicit naming: clearly indicates this is a SECRET key
     * - Prevents confusion with publishable keys (pk_test_...)
     * - Industry standard naming convention
     * 
     * Backward compatibility: Also check stripe.api.key if stripe.secret.key is not set
     */
    @Value("${stripe.secret.key:${stripe.api.key:}}")
    private String stripeSecretKey;
    
    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;
    
    @Value("${payment.success.url:http://localhost:5173/payment/success}")
    private String defaultSuccessUrl;
    
    @Value("${payment.cancel.url:http://localhost:5173/payment/cancel}")
    private String defaultCancelUrl;
    
    /**
     * Create payment for a booking
     * 
     * @param request Payment create request
     * @return Payment response with payment details
     */
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        logger.info("Creating payment for booking: {}", request.getBookingId());
        
        // 1. Validate booking exists
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));
        
        // 2. Validate booking status
        if ("CONFIRMED".equals(booking.getStatus())) {
            throw new BusinessException("ALREADY_PAID", "Booking is already confirmed and paid");
        }
        
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BusinessException("BOOKING_CANCELLED", "Cannot create payment for cancelled booking");
        }
        
        if ("COMPLETED".equals(booking.getStatus())) {
            throw new BusinessException("BOOKING_COMPLETED", "Cannot create payment for completed booking");
        }
        
        // 3. Check if booking already has successful payment
        boolean hasSuccessfulPayment = paymentRepository.existsByBookingIdAndStatus(
                request.getBookingId(), "SUCCESS");
        if (hasSuccessfulPayment) {
            throw new BusinessException("DUPLICATE_PAYMENT", "Booking already has a successful payment");
        }
        
        // 4. Validate payment method
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new BusinessException("INVALID_PAYMENT_METHOD", "Payment method is required");
        }
        
        // 5. Create payment based on payment method
        PaymentResponse response;
        switch (request.getPaymentMethod()) {
            case "STRIPE":
                response = createStripePayment(booking, request);
                break;
            case "VNPAY":
                response = createVNPayPayment(booking, request);
                break;
            case "MOMO":
                response = createMomoPayment(booking, request);
                break;
            default:
                throw new BusinessException("UNSUPPORTED_PAYMENT_METHOD", "Unsupported payment method: " + request.getPaymentMethod());
        }
        
        logger.info("Payment created successfully: {}", response.getPaymentId());
        return response;
    }
    
    /**
     * Create manual payment (admin approval required)
     * 
     * Flow:
     * 1. User clicks "Pay" → Create payment with status PENDING
     * 2. Send notification to admin for approval
     * 3. Admin approves payment → Payment status → SUCCESS
     * 4. Auto-confirm booking and issue ticket
     * 
     * This replaces Stripe integration with manual admin approval process
     */
    private PaymentResponse createStripePayment(Booking booking, PaymentCreateRequest request) {
        try {
            // Create payment record with PENDING status (waiting for admin approval)
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setBookingId(booking.getId());
            payment.setAmount(booking.getTotalAmount());
            payment.setCurrency(booking.getCurrency());
            payment.setPaymentMethod("STRIPE"); // Keep method name for compatibility
            payment.setStatus("PENDING");
            payment.setDescription("Flight booking payment - " + booking.getBookingCode() + " (Awaiting admin approval)");
            payment.setCreatedAt(java.time.LocalDateTime.now());
            
            payment = paymentRepository.save(payment);
            
            // Update booking status to PENDING_PAYMENT
            booking.setStatus("PENDING_PAYMENT");
            bookingRepository.save(booking);
            
            // Send notification to admin for approval
            try {
                notificationService.createAdminApprovalNotification(booking.getId());
                logger.info("Admin approval notification sent for booking: {}", booking.getId());
            } catch (Exception e) {
                logger.error("Failed to send admin notification for booking {}: {}", 
                    booking.getId(), e.getMessage());
                // Don't fail payment creation if notification fails
            }
            
            logger.info("Payment created successfully (pending admin approval): {}", payment.getId());
            
            // Build response
            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .bookingId(booking.getId())
                    .amount(booking.getTotalAmount())
                    .currency(booking.getCurrency())
                    .paymentMethod("STRIPE")
                    .status("PENDING")
                    .description("Flight booking payment - " + booking.getBookingCode())
                    .message("Payment request submitted. Waiting for admin approval. You will be notified once your payment is approved.")
                    .build();
            
        } catch (Exception e) {
            logger.error("Error creating payment: {}", e.getMessage(), e);
            throw new PaymentFailedException("STRIPE", "PAYMENT_CREATION_ERROR", 
                "Failed to create payment: " + e.getMessage());
        }
    }
    
    /**
     * Create VNPay payment (placeholder)
     * TODO: Implement VNPay integration
     */
    private PaymentResponse createVNPayPayment(Booking booking, PaymentCreateRequest request) {
        // TODO: Implement VNPay payment creation
        // 1. Generate VNPay payment URL
        // 2. Create payment record
        // 3. Return payment URL
        
        logger.warn("VNPay payment not yet implemented");
        throw new BusinessException("NOT_IMPLEMENTED", "VNPay payment is not yet implemented. Please use STRIPE.");
    }
    
    /**
     * Create Momo payment (placeholder)
     * TODO: Implement Momo integration
     */
    private PaymentResponse createMomoPayment(Booking booking, PaymentCreateRequest request) {
        // TODO: Implement Momo payment creation
        // 1. Generate Momo payment request
        // 2. Create payment record
        // 3. Return payment URL
        
        logger.warn("Momo payment not yet implemented");
        throw new BusinessException("NOT_IMPLEMENTED", "Momo payment is not yet implemented. Please use STRIPE.");
    }
    
    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
    }
    
    /**
     * Get payments for a booking
     */
    public List<Payment> getPaymentsByBookingId(String bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
    
    /**
     * Update payment status
     * Called by webhook handler
     */
    @Transactional
    public void updatePaymentStatus(String paymentId, String status, String failureReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        payment.setStatus(status);
        if (failureReason != null) {
            payment.setFailureReason(failureReason);
        }
        
        paymentRepository.save(payment);
        
        // Update booking status if payment successful
        if ("SUCCESS".equals(status)) {
            Booking booking = payment.getBooking();
            if (booking != null) {
                // ✅ STANDARD FLOW: Auto-confirm booking after successful payment
                // Check if seats are still available before confirming
                try {
                    // Confirm seat selections first
                    seatSelectionService.confirmSeatSelectionsForBooking(booking.getId());
                    
                    // Confirm seat locks
                    seatLockService.confirmLocksForBooking(booking.getId());
                    
                    // Confirm booking
                    booking.setStatus("CONFIRMED");
                    bookingRepository.save(booking);
                    logger.info("✅ Booking {} auto-confirmed after successful payment", booking.getId());
                    
                    // Create ticket automatically after confirmation
                    try {
                        ticketService.issueTicket(booking.getId());
                        logger.info("✅ Ticket created automatically for booking {}", booking.getId());
                    } catch (Exception e) {
                        logger.error("Failed to create ticket automatically: {}", e.getMessage());
                        // Ticket creation failure shouldn't fail the payment confirmation
                        // Ticket might already exist or booking might not be in correct state
                    }
                    
                } catch (Exception e) {
                    logger.error("Failed to confirm booking after payment: {}", e.getMessage(), e);
                    // Fallback: Set to PENDING_PAYMENT for admin review
                    booking.setStatus("PENDING_PAYMENT");
                    bookingRepository.save(booking);
                    
                    // Create notification for admin to review
                    try {
                        notificationService.createAdminApprovalNotification(booking.getId());
                    } catch (Exception ex) {
                        logger.error("Failed to create admin approval notification: {}", ex.getMessage());
                    }
                }
                
                // Send booking confirmation email
                try {
                    notificationService.sendBookingConfirmationEmail(booking.getId());
                } catch (Exception e) {
                    logger.error("Failed to send booking confirmation email: {}", e.getMessage());
                }
                
                // Send payment receipt email
                try {
                    notificationService.sendPaymentReceiptEmail(payment.getId(), payment);
                } catch (Exception e) {
                    logger.error("Failed to send payment receipt email: {}", e.getMessage());
                }
            }
        }
    }
}
