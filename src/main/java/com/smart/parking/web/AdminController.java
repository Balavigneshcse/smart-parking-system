package com.smart.parking.web;

import com.smart.parking.security.SecurityUtils;
import com.smart.parking.service.PricingService;
import com.smart.parking.service.ReservationService;
import com.smart.parking.service.StatsService;
import com.smart.parking.service.UserManagementService;
import com.smart.parking.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final StatsService statsService;
    private final UserManagementService userMgmt;
    private final ReservationService reservationService;
    private final PricingService pricingService;

    public AdminController(StatsService statsService, UserManagementService userMgmt,
                           ReservationService reservationService, PricingService pricingService) {
        this.statsService = statsService;
        this.userMgmt = userMgmt;
        this.reservationService = reservationService;
        this.pricingService = pricingService;
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<StatsResponse> daily() {
        Long pid = SecurityUtils.currentUser().getManagedPlace().getId();
        return ResponseEntity.ok(statsService.daily(pid));
    }

    @GetMapping("/stats/weekly")
    public ResponseEntity<StatsResponse> weekly() {
        Long pid = SecurityUtils.currentUser().getManagedPlace().getId();
        return ResponseEntity.ok(statsService.weekly(pid));
    }

    @GetMapping("/stats/monthly")
    public ResponseEntity<StatsResponse> monthly() {
        Long pid = SecurityUtils.currentUser().getManagedPlace().getId();
        return ResponseEntity.ok(statsService.monthly(pid));
    }

    @GetMapping("/staff")
    public ResponseEntity<List<StaffMemberResponse>> listStaff() {
        Long pid = SecurityUtils.currentUser().getManagedPlace().getId();
        return ResponseEntity.ok(userMgmt.listStaff(pid));
    }

    @PostMapping("/staff")
    public ResponseEntity<StaffMemberResponse> addStaff(@Valid @RequestBody AssignStaffRequest req) {
        return ResponseEntity.ok(userMgmt.createStaff(req, SecurityUtils.currentUser()));
    }

    @DeleteMapping("/staff/{staffId}")
    public ResponseEntity<Void> removeStaff(@PathVariable(name = "staffId") Long staffId) {
        userMgmt.deactivateStaff(staffId, SecurityUtils.currentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> reservations() {
        Long pid = SecurityUtils.currentUser().getManagedPlace().getId();
        return ResponseEntity.ok(reservationService.placeReservations(pid));
    }

    // ── Pricing Management (NEW) ────────────────────────────────────────────
    // Admin can view and edit the per-hour rates used across the whole system.
    // Customers see these same rates live on the booking page via /api/prices.

    @GetMapping("/pricing")
    public ResponseEntity<List<PricingRuleResponse>> listPricing() {
        return ResponseEntity.ok(pricingService.listAll());
    }

    @PutMapping("/pricing/{id}")
    public ResponseEntity<PricingRuleResponse> updatePricing(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody UpdatePricingRequest req) {
        return ResponseEntity.ok(pricingService.update(id, req));
    }

    @PostMapping("/pricing/{spaceType}/{vehicleType}")
    public ResponseEntity<PricingRuleResponse> createPricing(
            @PathVariable(name = "spaceType") String spaceType,
            @PathVariable(name = "vehicleType") String vehicleType,
            @Valid @RequestBody UpdatePricingRequest req) {
        return ResponseEntity.ok(pricingService.create(spaceType, vehicleType, req));
    }
}
