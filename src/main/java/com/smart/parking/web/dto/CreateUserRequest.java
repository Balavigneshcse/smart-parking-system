package com.smart.parking.web.dto;
import jakarta.validation.constraints.*;
public record CreateUserRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotBlank String phone,
    @NotBlank String role,
    Long placeId,
    Long stateId
) {}
