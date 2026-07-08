package com.smart.parking.web.dto;
import jakarta.validation.constraints.NotBlank;
public record VerifyPaymentRequest(
    @NotBlank String razorpayOrderId,
    @NotBlank String razorpayPaymentId,
    @NotBlank String razorpaySignature,
    Long reservationId,
    Long sessionId,
    String paymentMethod
) {}
