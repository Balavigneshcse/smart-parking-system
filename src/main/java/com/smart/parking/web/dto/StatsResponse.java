package com.smart.parking.web.dto;
import java.math.BigDecimal;
public record StatsResponse(
    String period, long totalVehicles, long currentlyParked,
    BigDecimal totalRevenue, long totalReservations, long availableSpaces, long totalSpaces
) {}
