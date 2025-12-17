package com.flightbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_webhooks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhook {
    @Id
    private String id;
    
    @Column(name = "payment_id")
    private String paymentId;
    
    private String provider;
    
    @Column(columnDefinition = "JSONB")
    private String payload;
    
    private Boolean verified;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", insertable = false, updatable = false)
    private Payment payment;
    
    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}

