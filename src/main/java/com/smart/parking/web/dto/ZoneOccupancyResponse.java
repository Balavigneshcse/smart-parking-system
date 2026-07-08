package com.smart.parking.web.dto;
import java.util.List;
public record ZoneOccupancyResponse(Long zoneId, String zoneName, String level,
    List<SpaceResponse> spaces) {}
