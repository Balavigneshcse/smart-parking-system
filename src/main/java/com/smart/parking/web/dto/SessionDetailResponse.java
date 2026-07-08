package com.smart.parking.web.dto;

import java.math.BigDecimal;

public record SessionDetailResponse(
        Long       id,
        String     plateNumber,
        String     vehicleType,
        String     ownerName,
        String     spaceName,
        String     entryTime,
        String     status,
        boolean    preBooked,
        String     reservationRef,
        /** "ONLINE" | "ON_SPOT" | null */
        String     paymentMode,
        /** true  → customer already paid online; security just marks exit */
        boolean    alreadyPaidOnline,
        /** live fee estimate (entry → now) including overtime 2× if applicable */
        BigDecimal estimatedFee,
        /** null for walk-ins; reservation end time for pre-booked vehicles */
        String     bookedUntil,
        /** true if vehicle stayed past its booked end time */
        boolean    overtime
) {}
