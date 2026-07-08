package com.smart.parking.web.dto;
import jakarta.validation.constraints.*;
public record CreateReservationRequest(
    @NotNull Long placeId,
    @NotBlank String spaceType,
    @NotNull String startsAt,
    @NotNull String endsAt,
    @NotBlank String plateNumber,
    @NotBlank String vehicleType,
    @NotBlank String ownerName,
    @NotBlank String ownerPhone,
    String ownerEmail,
    @NotBlank String paymentMode
) {}
