package com.smart.parking.web;

import com.smart.parking.service.PlaceService;
import com.smart.parking.web.dto.PlaceResponse;
import com.smart.parking.web.dto.ZoneOccupancyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;
    public PlaceController(PlaceService placeService) { this.placeService = placeService; }

    @GetMapping
    public ResponseEntity<List<PlaceResponse>> listPlaces() {
        return ResponseEntity.ok(placeService.listActivePlaces());
    }

    @GetMapping("/{id}/occupancy")
    public ResponseEntity<List<ZoneOccupancyResponse>> occupancy(@PathVariable("id") Long id) {
        return ResponseEntity.ok(placeService.getOccupancy(id));
    }
}
