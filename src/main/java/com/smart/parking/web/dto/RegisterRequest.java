package com.smart.parking.web.dto;
import jakarta.validation.constraints.*;
public record RegisterRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number") String phone,
    @NotNull String dateOfBirth
) {}
