package com.smart.parking.service;

import com.smart.parking.domain.ParkingPlace;
import com.smart.parking.domain.ParkingSpace;
import com.smart.parking.domain.ParkingZone;
import com.smart.parking.exception.ResourceNotFoundException;
import com.smart.parking.repository.ParkingPlaceRepository;
import com.smart.parking.repository.ParkingSpaceRepository;
import com.smart.parking.repository.ParkingZoneRepository;
import com.smart.parking.web.dto.PlaceResponse;
import com.smart.parking.web.dto.SpaceResponse;
import com.smart.parking.web.dto.ZoneOccupancyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PlaceService {

    private final ParkingPlaceRepository placeRepo;
    private final ParkingZoneRepository zoneRepo;
    private final ParkingSpaceRepository spaceRepo;

    public PlaceService(ParkingPlaceRepository placeRepo, ParkingZoneRepository zoneRepo,
                        ParkingSpaceRepository spaceRepo) {
        this.placeRepo = placeRepo;
        this.zoneRepo  = zoneRepo;
        this.spaceRepo = spaceRepo;
    }

    @Transactional(readOnly = true)          // ← FIX: session open for lazy loads
    public List<PlaceResponse> listActivePlaces() {
        return placeRepo.findAllActiveSorted().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ZoneOccupancyResponse> getOccupancy(Long placeId) {
        placeRepo.findById(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("ParkingPlace", placeId));
        return zoneRepo.findByPlace_IdOrderByNameAsc(placeId).stream()
                .map(z -> new ZoneOccupancyResponse(z.getId(), z.getName(), z.getLevel(),
                        spaceRepo.findByZone_Id(z.getId()).stream()
                                .map(s -> new SpaceResponse(s.getId(), s.getCode(),
                                        s.getSpaceType().name(), s.getStatus().name()))
                                .toList()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ParkingPlace getById(Long id) {
        return placeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ParkingPlace", id));
    }

    private PlaceResponse toResponse(ParkingPlace p) {
        long available = placeRepo.countAvailableSpaces(p.getId());
        long total     = placeRepo.countTotalSpaces(p.getId());
        String stateName = p.getState() != null ? p.getState().getName() : "";
        return new PlaceResponse(p.getId(), p.getName(), p.getAddress(),
                p.getCategory().name(), stateName, (int) total, available);
    }
}
