package com.smart.parking.service;

import com.smart.parking.domain.enums.SpaceStatus;
import com.smart.parking.repository.*;
import com.smart.parking.web.dto.StatsResponse;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
public class StatsService {

    private final VehicleSessionRepository sessionRepo;
    private final ReservationRepository reservationRepo;
    private final ParkingSpaceRepository spaceRepo;
    private final ParkingPlaceRepository placeRepo;

    public StatsService(VehicleSessionRepository sessionRepo, ReservationRepository reservationRepo,
                        ParkingSpaceRepository spaceRepo, ParkingPlaceRepository placeRepo) {
        this.sessionRepo = sessionRepo;
        this.reservationRepo = reservationRepo;
        this.spaceRepo = spaceRepo;
        this.placeRepo = placeRepo;
    }

    public StatsResponse daily(Long placeId) {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        return build("TODAY", placeId, start, start.plusDays(1));
    }

    public StatsResponse weekly(Long placeId) {
        LocalDateTime start = LocalDateTime.now().toLocalDate()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay();
        return build("THIS WEEK", placeId, start, start.plusDays(7));
    }

    public StatsResponse monthly(Long placeId) {
        LocalDateTime start = LocalDateTime.now().toLocalDate()
                .with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        return build("THIS MONTH", placeId, start, start.plusMonths(1));
    }

    public StatsResponse overall(Long placeId) {
        return build("ALL TIME", placeId, LocalDateTime.of(2000,1,1,0,0), LocalDateTime.now().plusDays(1));
    }

    /** System-wide stats for super admin */
    public StatsResponse systemDaily() {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalVehicles = 0, currentlyParked = 0, totalReservations = 0;
        long availableSpaces = 0, totalSpaces = 0;
        for (var place : placeRepo.findByActiveTrue()) {
            LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
            totalRevenue = totalRevenue.add(sessionRepo.sumRevenueByPlaceAndDateRange(place.getId(), start, start.plusDays(1)));
            totalVehicles += sessionRepo.countByPlaceAndDateRange(place.getId(), start, start.plusDays(1));
            currentlyParked += spaceRepo.findByZone_Place_IdAndStatus(place.getId(), SpaceStatus.OCCUPIED).size();
            availableSpaces += placeRepo.countAvailableSpaces(place.getId());
            totalSpaces += placeRepo.countTotalSpaces(place.getId());
        }
        return new StatsResponse("TODAY", totalVehicles, currentlyParked, totalRevenue, totalReservations, availableSpaces, totalSpaces);
    }

    private StatsResponse build(String period, Long placeId, LocalDateTime from, LocalDateTime to) {
        BigDecimal revenue = sessionRepo.sumRevenueByPlaceAndDateRange(placeId, from, to);
        long vehicles = sessionRepo.countByPlaceAndDateRange(placeId, from, to);
        long parked = spaceRepo.findByZone_Place_IdAndStatus(placeId, SpaceStatus.OCCUPIED).size();
        long available = placeRepo.countAvailableSpaces(placeId);
        long total = placeRepo.countTotalSpaces(placeId);
        long reservations = reservationRepo.findByPlaceAndDateRange(placeId, from, to).size();
        return new StatsResponse(period, vehicles, parked, revenue != null ? revenue : BigDecimal.ZERO,
                reservations, available, total);
    }
}
