package com.smart.parking.web.dto;
import jakarta.validation.constraints.*;
public record OnSpotRegisterRequest(
    @NotBlank String plateNumber,
    @NotBlank String vehicleType,
    @NotBlank String ownerName,
    @NotBlank String ownerPhone,
    String ownerEmail,
    Long spaceId
) {}
