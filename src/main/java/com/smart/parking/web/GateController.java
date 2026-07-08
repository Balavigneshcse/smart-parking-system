package com.smart.parking.web;

import com.smart.parking.security.SecurityUtils;
import com.smart.parking.service.GateService;
import com.smart.parking.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/gate")
@PreAuthorize("hasAnyAuthority('MANAGER','SECURITY')")
public class GateController {

    private final GateService gateService;
    public GateController(GateService gateService) { this.gateService = gateService; }

    @GetMapping("/check/{plate}")
    public ResponseEntity<VehicleLookupResponse> check(@PathVariable("plate") String plate) {
        return ResponseEntity.ok(gateService.lookup(plate));
    }

    @PostMapping("/entry")
    public ResponseEntity<SessionResponse> entry(@Valid @RequestBody EntryRequest req) {
        return ResponseEntity.ok(gateService.markEntry(req, SecurityUtils.currentUser()));
    }

    @PostMapping("/entry/on-spot")
    public ResponseEntity<SessionResponse> onSpot(@Valid @RequestBody OnSpotRegisterRequest req) {
        return ResponseEntity.ok(gateService.onSpotEntry(req, SecurityUtils.currentUser()));
    }

    /**
     * NEW — called by gate.html before showing the exit modal.
     * Returns session details including paymentMode and alreadyPaidOnline flag
     * so the UI can show the correct exit flow.
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionDetailResponse> sessionDetail(
            @PathVariable("sessionId") Long sessionId) {
        return ResponseEntity.ok(gateService.getSessionDetails(sessionId, SecurityUtils.currentUser()));
    }

    @PostMapping("/exit/{sessionId}")
    public ResponseEntity<SessionResponse> exit(
            @PathVariable("sessionId") Long sessionId,
            @Valid @RequestBody SessionExitRequest req) {
        return ResponseEntity.ok(gateService.markExit(sessionId, req, SecurityUtils.currentUser()));
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<List<SessionResponse>> activeSessions() {
        return ResponseEntity.ok(gateService.activeSessions(SecurityUtils.currentUser()));
    }

    @GetMapping("/sessions/today")
    public ResponseEntity<List<SessionResponse>> todaySessions() {
        return ResponseEntity.ok(gateService.todaySessions(SecurityUtils.currentUser()));
    }
}
