package com.smart.parking.web.dto;
import java.math.BigDecimal;
public record ReservationResponse(
    Long id, String reference, String placeName, String zoneCode, String spaceCode,
    String spaceType, String plateNumber, String vehicleType, String ownerName,
    String startsAt, String endsAt, BigDecimal quotedAmount, String paymentMode, String status
) {}
