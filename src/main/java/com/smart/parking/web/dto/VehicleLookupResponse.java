package com.smart.parking.web.dto;
public record VehicleLookupResponse(
    boolean found, boolean hasActiveSession, boolean hasActiveReservation,
    String plateNumber, String vehicleType, String ownerName, String ownerPhone,
    Long sessionId, String reservationRef
) {}
