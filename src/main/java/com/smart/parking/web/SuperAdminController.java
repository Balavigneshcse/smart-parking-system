package com.smart.parking.web;

import com.smart.parking.security.SecurityUtils;
import com.smart.parking.service.PlaceService;
import com.smart.parking.service.StatsService;
import com.smart.parking.service.UserManagementService;
import com.smart.parking.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/super")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class SuperAdminController {

    private final UserManagementService userMgmt;
    private final StatsService statsService;
    private final PlaceService placeService;

    public SuperAdminController(UserManagementService userMgmt, StatsService statsService,
                                PlaceService placeService) {
        this.userMgmt = userMgmt;
        this.statsService = statsService;
        this.placeService = placeService;
    }

    @PostMapping("/state-managers")
    public ResponseEntity<UserResponse> createStateManager(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userMgmt.createStateManager(req));
    }

    @PostMapping("/admins")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userMgmt.createAdmin(req, SecurityUtils.currentUser()));
    }

    @GetMapping("/state-managers")
    public ResponseEntity<List<UserResponse>> listStateManagers() {
        return ResponseEntity.ok(userMgmt.listStateManagers());
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserResponse>> listAdmins() {
        return ResponseEntity.ok(userMgmt.listAdmins());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listAll() {
        return ResponseEntity.ok(userMgmt.listAllUsers());
    }

    @GetMapping("/places")
    public ResponseEntity<List<PlaceResponse>> listPlaces() {
        return ResponseEntity.ok(placeService.listActivePlaces());
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> systemStats() {
        return ResponseEntity.ok(statsService.systemDaily());
    }
}
