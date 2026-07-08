package com.smart.parking.web.dto;
import jakarta.validation.constraints.*;
public record AssignStaffRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotBlank String phone,
    @NotBlank String staffRole
) {}
