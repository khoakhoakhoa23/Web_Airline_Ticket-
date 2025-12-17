package com.flightbooking.controller;

import com.flightbooking.dto.PaymentCreateRequest;
import com.flightbooking.dto.PaymentResponse;
import com.flightbooking.entity.Payment;
import com.flightbooking.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payment Controller
 * 
 * Handles payment-related API endpoints
 * 
 * Endpoints:
 * - POST /api/payments/create - Create new payment
 * - GET /api/payments/{id} - Get payment by ID
 * - GET /api/payments/booking/{bookingId} - Get payments for booking
 * - POST /api/payments/webhook/stripe - Handle Stripe webhooks
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"},
            methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH},
            allowedHeaders = "*",
            allowCredentials = "true")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;
    
    /**
     * Create new payment
     * 
     * POST /api/payments/create
     * Request: { "bookingId": "...", "paymentMethod": "STRIPE" }
     * Response: { "paymentId": "...", "checkoutUrl": "...", ... }
     */
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        try {
            logger.info("Received payment create request for booking: {}", request.getBookingId());
            PaymentResponse response = paymentService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error creating payment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(PaymentResponse.builder()
                            .status("ERROR")
                            .message(e.getMessage())
                            .build());
        }
    }
    
    /**
     * Get payment by ID
     * 
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable String id) {
        try {
            Payment payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            logger.error("Error getting payment: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get payments for a booking
     * 
     * GET /api/payments/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Payment>> getPaymentsByBooking(@PathVariable String bookingId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByBookingId(bookingId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            logger.error("Error getting payments for booking: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Stripe webhook endpoint
     * 
     * POST /api/payments/webhook/stripe
     * 
     * Handles Stripe events:
     * - checkout.session.completed: Payment successful
     * - payment_intent.succeeded: Payment intent successful
     * - payment_intent.payment_failed: Payment failed
     * 
     * Webhook signature verification ensures authenticity
     */
    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        logger.info("Received Stripe webhook");
        
        if (stripeWebhookSecret == null || stripeWebhookSecret.isEmpty()) {
            logger.warn("Stripe webhook secret not configured, skipping signature verification");
            // In production, this should return 400
            // For development, we'll process the webhook anyway
        }
        
        Event event;
        
        try {
            // Verify webhook signature
            if (stripeWebhookSecret != null && !stripeWebhookSecret.isEmpty()) {
                event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            } else {
                // Parse event without verification (development only)
                event = Event.GSON.fromJson(payload, Event.class);
            }
        } catch (SignatureVerificationException e) {
            logger.error("Invalid Stripe webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Error parsing Stripe webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }
        
        // Handle different event types
        try {
            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;
                    
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                    
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                    
                default:
                    logger.info("Unhandled Stripe event type: {}", event.getType());
            }
            
            return ResponseEntity.ok("Webhook processed");
            
        } catch (Exception e) {
            logger.error("Error processing Stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook");
        }
    }
    
    /**
     * Handle checkout.session.completed event
     * Triggered when customer successfully completes payment
     */
    private void handleCheckoutSessionCompleted(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;
        
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            logger.error("Failed to deserialize Stripe object");
            return;
        }
        
        if (stripeObject instanceof Session) {
            Session session = (Session) stripeObject;
            String bookingId = session.getMetadata().get("booking_id");
            String paymentIntentId = session.getPaymentIntent();
            
            logger.info("Checkout session completed for booking: {}", bookingId);
            logger.info("Payment Intent ID: {}", paymentIntentId);
            
            // Find payment by transaction ID (session ID) or payment intent ID
            // Update payment status to SUCCESS
            // This will trigger booking confirmation in PaymentService
            try {
                // For now, we'll log this
                // In production, update payment status here
                logger.info("Payment successful for booking: {}", bookingId);
                // paymentService.updatePaymentStatus(paymentId, "SUCCESS", null);
            } catch (Exception e) {
                logger.error("Error updating payment status: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Handle payment_intent.succeeded event
     */
    private void handlePaymentIntentSucceeded(Event event) {
        logger.info("Payment Intent succeeded");
        // Additional logic if needed
    }
    
    /**
     * Handle payment_intent.payment_failed event
     */
    private void handlePaymentIntentFailed(Event event) {
        logger.info("Payment Intent failed");
        // Update payment status to FAILED
        // Optionally notify user
    }
}
