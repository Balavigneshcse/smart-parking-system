package com.smart.parking.web.dto;
public record PlaceResponse(Long id, String name, String address, String category,
    String stateName, int totalSpaces, long availableSpaces) {}
