package com.flightbooking.service;

import com.flightbooking.dto.EmailContext;
import com.flightbooking.entity.Booking;
import com.flightbooking.entity.Payment;
import com.flightbooking.entity.User;
import com.flightbooking.repository.BookingRepository;
import com.flightbooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Notification Service
 * 
 * Handles all booking-related notifications:
 * - Booking confirmation emails
 * - Payment receipt emails
 * - Booking reminder emails
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.flightbooking.repository.NotificationRepository notificationRepository;
    
    /**
     * Send booking confirmation email
     * Triggered when booking is confirmed (after payment success)
     * 
     * @param bookingId Booking ID
     */
    public void sendBookingConfirmationEmail(String bookingId) {
        try {
            logger.info("Sending booking confirmation email for booking: {}", bookingId);
            
            // Fetch booking with details
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            
            // Fetch user
            User user = userRepository.findById(booking.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + booking.getUserId()));
            
            // Prepare email variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getEmail()); // or user.getFullName() if exists
            variables.put("bookingCode", booking.getBookingCode());
            variables.put("bookingStatus", booking.getStatus());
            variables.put("totalAmount", formatCurrency(booking.getTotalAmount(), booking.getCurrency()));
            variables.put("currency", booking.getCurrency());
            variables.put("createdAt", booking.getCreatedAt().format(DATETIME_FORMATTER));
            
            // Add flight segments if available
            if (booking.getFlightSegments() != null && !booking.getFlightSegments().isEmpty()) {
                variables.put("flightSegments", booking.getFlightSegments());
            }
            
            // Add passengers if available
            if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
                variables.put("passengers", booking.getPassengers());
                variables.put("passengerCount", booking.getPassengers().size());
            }
            
            // Create email context
            EmailContext emailContext = EmailContext.builder()
                    .to(user.getEmail())
                    .subject("Booking Confirmation – Flight " + booking.getBookingCode())
                    .template("booking-confirmation")
                    .variables(variables)
                    .build();
            
            // Send email asynchronously
            emailService.sendEmailAsync(emailContext);
            
            logger.info("Booking confirmation email sent to: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email for booking: {}, error: {}", 
                    bookingId, e.getMessage(), e);
            // Don't throw exception - email failure shouldn't break booking flow
        }
    }
    
    /**
     * Send payment receipt email
     * Triggered when payment is successful
     * 
     * @param paymentId Payment ID
     */
    public void sendPaymentReceiptEmail(String paymentId, Payment payment) {
        try {
            logger.info("Sending payment receipt email for payment: {}", paymentId);
            
            // Fetch booking
            Booking booking = bookingRepository.findById(payment.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + payment.getBookingId()));
            
            // Fetch user
            User user = userRepository.findById(booking.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + booking.getUserId()));
            
            // Prepare email variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getEmail());
            variables.put("bookingCode", booking.getBookingCode());
            variables.put("paymentAmount", formatCurrency(payment.getAmount(), payment.getCurrency()));
            variables.put("currency", payment.getCurrency());
            variables.put("paymentMethod", payment.getPaymentMethod());
            variables.put("transactionId", payment.getTransactionId());
            variables.put("paymentDate", payment.getCreatedAt().format(DATETIME_FORMATTER));
            variables.put("paymentStatus", payment.getStatus());
            
            // Create email context
            EmailContext emailContext = EmailContext.builder()
                    .to(user.getEmail())
                    .subject("Payment Receipt – Booking " + booking.getBookingCode())
                    .template("payment-receipt")
                    .variables(variables)
                    .build();
            
            // Send email asynchronously
            emailService.sendEmailAsync(emailContext);
            
            logger.info("Payment receipt email sent to: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send payment receipt email for payment: {}, error: {}", 
                    paymentId, e.getMessage(), e);
            // Don't throw exception
        }
    }
    
    /**
     * Send booking reminder email
     * Triggered 24 hours before flight departure
     * 
     * @param bookingId Booking ID
     */
    public void sendBookingReminderEmail(String bookingId) {
        try {
            logger.info("Sending booking reminder email for booking: {}", bookingId);
            
            // Fetch booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            
            // Fetch user
            User user = userRepository.findById(booking.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + booking.getUserId()));
            
            // Prepare email variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getEmail());
            variables.put("bookingCode", booking.getBookingCode());
            
            // Add flight segments with formatted dates/times
            if (booking.getFlightSegments() != null && !booking.getFlightSegments().isEmpty()) {
                variables.put("flightSegments", booking.getFlightSegments());
                // Add first flight departure time for subject
                var firstFlight = booking.getFlightSegments().get(0);
                variables.put("departureDate", firstFlight.getDepartTime().format(DATE_FORMATTER));
                variables.put("departureTime", firstFlight.getDepartTime().format(TIME_FORMATTER));
            }
            
            // Create email context
            EmailContext emailContext = EmailContext.builder()
                    .to(user.getEmail())
                    .subject("Flight Reminder – Your flight is tomorrow!")
                    .template("booking-reminder")
                    .variables(variables)
                    .build();
            
            // Send email asynchronously
            emailService.sendEmailAsync(emailContext);
            
            logger.info("Booking reminder email sent to: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send booking reminder email for booking: {}, error: {}", 
                    bookingId, e.getMessage(), e);
            // Don't throw exception
        }
    }
    
    /**
     * Create notification for admin to approve booking
     * Called when payment is successful and booking needs admin approval
     * 
     * @param bookingId Booking ID
     */
    public void createAdminApprovalNotification(String bookingId) {
        try {
            logger.info("Creating admin approval notification for booking: {}", bookingId);
            
            // Fetch booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            
            // Create notification entity
            com.flightbooking.entity.Notification notification = new com.flightbooking.entity.Notification();
            notification.setId(java.util.UUID.randomUUID().toString());
            notification.setBookingId(bookingId);
            notification.setChannel("ADMIN_PANEL");
            notification.setRecipient("ADMIN");
            notification.setContent("Đơn đặt vé mới " + booking.getBookingCode() + " đang chờ duyệt thanh toán. Vui lòng kiểm tra và duyệt đơn hàng.");
            
            // Save notification
            notificationRepository.save(notification);
            
            logger.info("Admin approval notification created for booking: {}", bookingId);
            
        } catch (Exception e) {
            logger.error("Failed to create admin approval notification for booking: {}, error: {}", 
                    bookingId, e.getMessage(), e);
            // Don't throw exception - notification failure shouldn't break payment flow
        }
    }
    
    /**
     * Format currency amount
     */
    private String formatCurrency(java.math.BigDecimal amount, String currency) {
        if (amount == null) return "0";
        
        if ("VND".equals(currency)) {
            return String.format("%,d VND", amount.longValue());
        } else if ("USD".equals(currency)) {
            return String.format("$%.2f", amount.doubleValue());
        } else {
            return amount.toString() + " " + currency;
        }
    }
}
