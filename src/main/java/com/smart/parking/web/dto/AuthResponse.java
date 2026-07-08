package com.smart.parking.web.dto;
public record AuthResponse(String token, String role, Long userId, String name, String email, Long placeId, Long stateId) {}
