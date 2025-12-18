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
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
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
    
    @Value("${stripe.api.key:}")
    private String stripeApiKey;
    
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
     * Create Stripe payment
     * Uses Stripe Checkout Session for hosted payment page
     */
    private PaymentResponse createStripePayment(Booking booking, PaymentCreateRequest request) {
        try {
            // Initialize Stripe with API key
            Stripe.apiKey = stripeApiKey;
            
            // Calculate amount in cents (Stripe requires smallest currency unit)
            // For VND: amount * 1 (VND has no subunit)
            // For USD: amount * 100 (cents)
            long amountInSmallestUnit;
            String currency;
            
            if ("VND".equals(booking.getCurrency())) {
                amountInSmallestUnit = booking.getTotalAmount().longValue();
                currency = "vnd";
            } else {
                // Default to USD
                amountInSmallestUnit = booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
                currency = "usd";
            }
            
            // Create Stripe Checkout Session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(request.getSuccessUrl() != null ? 
                            request.getSuccessUrl() : defaultSuccessUrl + "?booking_id=" + booking.getId())
                    .setCancelUrl(request.getCancelUrl() != null ? 
                            request.getCancelUrl() : defaultCancelUrl + "?booking_id=" + booking.getId())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency)
                                                    .setUnitAmount(amountInSmallestUnit)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Flight Booking - " + booking.getBookingCode())
                                                                    .setDescription("Flight booking payment")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putMetadata("booking_id", booking.getId())
                    .putMetadata("booking_code", booking.getBookingCode())
                    .build();
            
            Session session = Session.create(params);
            
            // Create payment record in database
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setBookingId(booking.getId());
            payment.setAmount(booking.getTotalAmount());
            payment.setCurrency(booking.getCurrency());
            payment.setPaymentMethod("STRIPE");
            payment.setPaymentIntentId(session.getPaymentIntent());
            payment.setTransactionId(session.getId());
            payment.setStatus("PENDING");
            payment.setDescription("Flight booking payment - " + booking.getBookingCode());
            
            payment = paymentRepository.save(payment);
            
            // Update booking status
            booking.setStatus("PENDING_PAYMENT");
            bookingRepository.save(booking);
            
            // Build response
            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .bookingId(booking.getId())
                    .amount(booking.getTotalAmount())
                    .currency(booking.getCurrency())
                    .paymentMethod("STRIPE")
                    .status("PENDING")
                    .paymentIntentId(session.getPaymentIntent())
                    .checkoutUrl(session.getUrl())
                    .description("Redirect to Stripe Checkout to complete payment")
                    .message("Payment session created. Please complete payment at Stripe Checkout.")
                    .build();
            
        } catch (StripeException e) {
            logger.error("Stripe error creating payment: {}", e.getMessage(), e);
            throw new PaymentFailedException("STRIPE_ERROR", "Failed to create Stripe payment: " + e.getMessage());
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
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
                logger.info("Booking {} confirmed after successful payment", booking.getId());
                
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
