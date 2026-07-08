package com.smart.parking.web.dto;
public record UserResponse(Long id, String name, String email, String phone, String role,
    String managedPlace, String managedState, boolean active, String createdAt) {}
