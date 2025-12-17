package com.flightbooking.service;

import com.flightbooking.dto.EmailContext;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

/**
 * Email Service
 * 
 * Handles sending emails with support for:
 * - Plain text emails
 * - HTML emails with templates (Thymeleaf)
 * - Attachments (future)
 * 
 * Email Templates Location: src/main/resources/templates/email/
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @Value("${email.from:noreply@flightbooking.com}")
    private String emailFrom;
    
    @Value("${email.from.name:Flight Booking System}")
    private String emailFromName;
    
    @Value("${email.enabled:true}")
    private boolean emailEnabled;
    
    /**
     * Send simple text email
     * 
     * @param to Recipient email
     * @param subject Email subject
     * @param text Email body (plain text)
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            logger.info("Email disabled. Would send to: {}, subject: {}", to, subject);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            logger.info("Simple email sent to: {}, subject: {}", to, subject);
        } catch (Exception e) {
            logger.error("Failed to send simple email to: {}, error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
    
    /**
     * Send HTML email using Thymeleaf template
     * 
     * @param emailContext Email context with template and variables
     */
    public void sendHtmlEmail(EmailContext emailContext) {
        if (!emailEnabled) {
            logger.info("Email disabled. Would send to: {}, subject: {}, template: {}", 
                    emailContext.getTo(), emailContext.getSubject(), emailContext.getTemplate());
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            
            // Set from, to, subject
            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(emailContext.getTo());
            helper.setSubject(emailContext.getSubject());
            
            // Process template with variables
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(emailContext.getVariables());
            
            String htmlContent = templateEngine.process(
                    "email/" + emailContext.getTemplate(),
                    thymeleafContext
            );
            
            helper.setText(htmlContent, true);
            
            // Send email
            mailSender.send(message);
            logger.info("HTML email sent to: {}, subject: {}, template: {}", 
                    emailContext.getTo(), emailContext.getSubject(), emailContext.getTemplate());
            
        } catch (MessagingException e) {
            logger.error("Failed to send HTML email to: {}, error: {}", 
                    emailContext.getTo(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error sending email to: {}, error: {}", 
                    emailContext.getTo(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
    
    /**
     * Send email asynchronously (non-blocking)
     * Useful for not blocking main application flow
     */
    public void sendEmailAsync(EmailContext emailContext) {
        new Thread(() -> {
            try {
                sendHtmlEmail(emailContext);
            } catch (Exception e) {
                logger.error("Async email sending failed: {}", e.getMessage());
            }
        }).start();
    }
}

