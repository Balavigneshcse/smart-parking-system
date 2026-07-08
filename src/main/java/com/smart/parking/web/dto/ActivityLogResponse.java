package com.smart.parking.web.dto;
import java.math.BigDecimal;
public record ActivityLogResponse(
    Long id, String action, String details,
    String userName, String userRole, String ipAddress,
    String timestamp, boolean success,
    BigDecimal paymentAmount, String paymentRef, String errorMessage
) {}
