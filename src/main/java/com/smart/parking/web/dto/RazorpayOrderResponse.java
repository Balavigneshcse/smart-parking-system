package com.smart.parking.web.dto;
import java.math.BigDecimal;
public record RazorpayOrderResponse(
    String razorpayOrderId, String keyId,
    BigDecimal amount, String currency,
    String reservationRef, Long sessionId,
    String description
) {}
