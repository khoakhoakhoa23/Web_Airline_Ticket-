package com.flightbooking.service;

import com.flightbooking.dto.PaymentWebhookDTO;
import com.flightbooking.entity.Payment;
import com.flightbooking.entity.PaymentWebhook;
import com.flightbooking.repository.PaymentRepository;
import com.flightbooking.repository.PaymentWebhookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentWebhookService {
    
    @Autowired
    private PaymentWebhookRepository paymentWebhookRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Transactional
    public PaymentWebhookDTO processWebhook(String provider, String payload, String signature) {
        // Save webhook first
        PaymentWebhook webhook = new PaymentWebhook();
        webhook.setId(UUID.randomUUID().toString());
        webhook.setProvider(provider);
        webhook.setPayload(payload);
        
        // Verify signature (simplified - in production, implement proper signature verification)
        boolean verified = verifySignature(provider, payload, signature);
        webhook.setVerified(verified);
        
        webhook = paymentWebhookRepository.save(webhook);
        
        if (verified) {
            // Parse payload and update payment status
            // This is simplified - in production, parse JSON and extract payment info
            String paymentId = extractPaymentIdFromPayload(payload);
            if (paymentId != null) {
                webhook.setPaymentId(paymentId);
                paymentWebhookRepository.save(webhook);
                
                // Update payment status based on webhook
                String status = extractStatusFromPayload(payload);
                if (status != null) {
                    // Pass null for failureReason (can be enhanced to extract from payload)
                    paymentService.updatePaymentStatus(paymentId, status, null);
                }
            }
        }
        
        return convertToDTO(webhook);
    }
    
    private boolean verifySignature(String provider, String payload, String signature) {
        // Simplified signature verification
        // In production, implement proper signature verification for each provider
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        // TODO: Implement actual signature verification
        return true;
    }
    
    private String extractPaymentIdFromPayload(String payload) {
        // Simplified extraction - in production, parse JSON properly
        // This is a placeholder
        if (payload.contains("transactionId")) {
            // Extract from JSON
            return "payment-id-from-payload";
        }
        return null;
    }
    
    private String extractStatusFromPayload(String payload) {
        // Simplified extraction - in production, parse JSON properly
        if (payload.contains("success") || payload.contains("00")) {
            return "COMPLETED";
        } else if (payload.contains("fail") || payload.contains("error")) {
            return "FAILED";
        }
        return null;
    }
    
    private PaymentWebhookDTO convertToDTO(PaymentWebhook webhook) {
        PaymentWebhookDTO dto = new PaymentWebhookDTO();
        dto.setId(webhook.getId());
        dto.setPaymentId(webhook.getPaymentId());
        dto.setProvider(webhook.getProvider());
        dto.setPayload(webhook.getPayload());
        dto.setVerified(webhook.getVerified());
        dto.setReceivedAt(webhook.getReceivedAt());
        return dto;
    }
}

