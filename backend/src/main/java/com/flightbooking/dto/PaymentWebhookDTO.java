package com.flightbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookDTO {
    private String id;
    private String paymentId;
    private String provider;
    private String payload;
    private Boolean verified;
    private LocalDateTime receivedAt;
}

