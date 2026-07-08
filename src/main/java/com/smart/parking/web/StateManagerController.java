package com.smart.parking.web;

import com.smart.parking.security.SecurityUtils;
import com.smart.parking.service.StatsService;
import com.smart.parking.service.UserManagementService;
import com.smart.parking.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/state")
@PreAuthorize("hasAuthority('STATE_MANAGER')")
public class StateManagerController {

    private final UserManagementService userMgmt;
    private final StatsService statsService;

    public StateManagerController(UserManagementService userMgmt, StatsService statsService) {
        this.userMgmt = userMgmt;
        this.statsService = statsService;
    }

    @PostMapping("/admins")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userMgmt.createAdmin(req, SecurityUtils.currentUser()));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserResponse>> listAdmins() {
        return ResponseEntity.ok(userMgmt.listAdmins());
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> stats() {
        return ResponseEntity.ok(statsService.systemDaily());
    }
}
