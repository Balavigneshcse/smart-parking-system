package com.smart.parking.web;

import com.smart.parking.domain.ActivityLog;
import com.smart.parking.service.ActivityLogService;
import com.smart.parking.web.dto.ActivityLogResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN','STATE_MANAGER','ADMIN')")
public class ActivityLogController {

    private final ActivityLogService logService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public ActivityLogController(ActivityLogService logService) { this.logService = logService; }

    @GetMapping("/recent")
    public ResponseEntity<List<ActivityLogResponse>> recent(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(logService.getRecent(hours).stream().map(this::toDto).toList());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','STATE_MANAGER')")
    public ResponseEntity<List<ActivityLogResponse>> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(logService.getAll(page, size).stream().map(this::toDto).toList());
    }

    @GetMapping("/errors")
    public ResponseEntity<List<ActivityLogResponse>> errors() {
        return ResponseEntity.ok(logService.getErrors().stream().map(this::toDto).toList());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','STATE_MANAGER')")
    public ResponseEntity<List<ActivityLogResponse>> byUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(logService.getByUser(userId).stream().map(this::toDto).toList());
    }

    private ActivityLogResponse toDto(ActivityLog l) {
        return new ActivityLogResponse(l.getId(), l.getAction(), l.getDetails(),
                l.getUserName(), l.getUserRole(), l.getIpAddress(),
                l.getTimestamp() != null ? l.getTimestamp().format(FMT) : null,
                l.isSuccess(), l.getPaymentAmount(), l.getPaymentRef(), l.getErrorMessage());
    }
}
