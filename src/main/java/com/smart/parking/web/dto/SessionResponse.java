package com.smart.parking.web.dto;
import java.math.BigDecimal;
public record SessionResponse(
    Long id, String plateNumber, String vehicleType, String ownerName,
    String spaceName, String entryTime, String exitTime,
    BigDecimal totalFee, String status, boolean preBooked
) {}
