package com.smart.parking.web.dto;
import jakarta.validation.constraints.NotBlank;
public record SessionExitRequest(@NotBlank String paymentMethod) {}
