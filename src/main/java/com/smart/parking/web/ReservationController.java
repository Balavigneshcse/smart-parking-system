package com.smart.parking.web;

import com.smart.parking.security.SecurityUtils;
import com.smart.parking.service.ReservationService;
import com.smart.parking.web.dto.CreateReservationRequest;
import com.smart.parking.web.dto.ReservationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@PreAuthorize("hasAuthority('CUSTOMER')")
public class ReservationController {

    private final ReservationService reservationService;
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest req) {
        return ResponseEntity.ok(reservationService.create(req, SecurityUtils.currentUserId()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationResponse>> mine() {
        return ResponseEntity.ok(reservationService.myReservations(SecurityUtils.currentUserId()));
    }
}
