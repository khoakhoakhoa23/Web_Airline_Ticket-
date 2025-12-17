package com.flightbooking.repository;

import com.flightbooking.entity.PaymentWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook, String> {
    List<PaymentWebhook> findByPaymentId(String paymentId);
}

