package com.smart.parking.web.dto;
import java.math.BigDecimal;
public record PaymentResponse(Long id, BigDecimal amount, String paymentMethod,
    String paymentStatus, String transactionRef, String paidAt) {}
