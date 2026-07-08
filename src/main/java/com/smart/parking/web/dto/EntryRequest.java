package com.smart.parking.web.dto;
import jakarta.validation.constraints.NotBlank;
public record EntryRequest(@NotBlank String plateNumber, Long spaceId) {}
