package com.flightbooking.controller;

import com.flightbooking.dto.PaymentWebhookDTO;
import com.flightbooking.service.PaymentWebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-webhooks")
@CrossOrigin(origins = "*")
public class PaymentWebhookController {
    
    @Autowired
    private PaymentWebhookService paymentWebhookService;
    
    @PostMapping("/vnpay")
    public ResponseEntity<String> handleVNPayWebhook(@RequestBody String payload,
                                                    @RequestHeader(value = "X-VNPay-Signature", required = false) String signature) {
        try {
            paymentWebhookService.processWebhook("VNPAY", payload, signature);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR");
        }
    }
    
    @PostMapping("/zalopay")
    public ResponseEntity<String> handleZaloPayWebhook(@RequestBody String payload,
                                                       @RequestHeader(value = "X-ZaloPay-Signature", required = false) String signature) {
        try {
            paymentWebhookService.processWebhook("ZALOPAY", payload, signature);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR");
        }
    }
    
    @PostMapping("/momo")
    public ResponseEntity<String> handleMoMoWebhook(@RequestBody String payload,
                                                      @RequestHeader(value = "X-MoMo-Signature", required = false) String signature) {
        try {
            paymentWebhookService.processWebhook("MOMO", payload, signature);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR");
        }
    }
}

