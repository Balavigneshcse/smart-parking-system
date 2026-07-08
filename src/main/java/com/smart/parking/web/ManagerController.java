package com.smart.parking.web;

import com.smart.parking.exception.BusinessRuleException;
import com.smart.parking.repository.PlaceStaffRepository;
import com.smart.parking.security.SecurityUtils;
import com.smart.parking.service.ReservationService;
import com.smart.parking.service.StatsService;
import com.smart.parking.web.dto.ReservationResponse;
import com.smart.parking.web.dto.StatsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Dedicated REST controller for MANAGER role.
 * Managers are linked to a ParkingPlace via PlaceStaff (not via User.managedPlace),
 * so we resolve their place through PlaceStaffRepository.
 */
@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasAuthority('MANAGER')")
public class ManagerController {

    private final PlaceStaffRepository staffRepo;
    private final ReservationService   reservationService;
    private final StatsService         statsService;

    public ManagerController(PlaceStaffRepository staffRepo,
                             ReservationService reservationService,
                             StatsService statsService) {
        this.staffRepo          = staffRepo;
        this.reservationService = reservationService;
        this.statsService       = statsService;
    }

    /** All reservations for the manager's parking place. */
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> reservations() {
        Long placeId = getPlaceId();
        return ResponseEntity.ok(reservationService.placeReservations(placeId));
    }

    /** Daily stats for the manager's parking place. */
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> stats() {
        return ResponseEntity.ok(statsService.daily(getPlaceId()));
    }

    // ── private helper ───────────────────────────────────────────────────────

    private Long getPlaceId() {
        Long userId = SecurityUtils.currentUser().getId();
        return staffRepo.findActiveByUserId(userId)
                .map(ps -> ps.getPlace().getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "Manager is not assigned to any active place."));
    }
}
