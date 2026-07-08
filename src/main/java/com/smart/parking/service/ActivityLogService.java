package com.smart.parking.service;

import com.smart.parking.domain.ActivityLog;
import com.smart.parking.repository.ActivityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);
    private final ActivityLogRepository repo;

    // ── Action constants ──────────────────────────────────────────────────
    public static final String USER_LOGIN              = "USER_LOGIN";
    public static final String USER_LOGOUT             = "USER_LOGOUT";
    public static final String USER_REGISTER           = "USER_REGISTER";
    public static final String PASSWORD_RESET_REQUEST  = "PASSWORD_RESET_REQUEST";
    public static final String PASSWORD_RESET_COMPLETE = "PASSWORD_RESET_COMPLETE";
    public static final String RESERVATION_CREATED     = "RESERVATION_CREATED";
    public static final String PAYMENT_INITIATED       = "PAYMENT_INITIATED";
    public static final String PAYMENT_SUCCESS         = "PAYMENT_SUCCESS";
    public static final String PAYMENT_FAILED          = "PAYMENT_FAILED";
    public static final String VEHICLE_ENTRY           = "VEHICLE_ENTRY";
    public static final String VEHICLE_EXIT            = "VEHICLE_EXIT";
    public static final String ON_SPOT_ENTRY           = "ON_SPOT_ENTRY";
    public static final String STAFF_CREATED           = "STAFF_CREATED";
    public static final String STAFF_REMOVED           = "STAFF_REMOVED";
    public static final String ADMIN_CREATED           = "ADMIN_CREATED";
    public static final String STATE_MGR_CREATED       = "STATE_MANAGER_CREATED";
    public static final String ERROR                   = "ERROR";

    public ActivityLogService(ActivityLogRepository repo) { this.repo = repo; }

    @Async
    public void log(String action, String details, Long userId, String userName,
                    String userRole, String ip, boolean success) {
        try {
            repo.save(ActivityLog.builder()
                    .action(action).details(details)
                    .userId(userId).userName(userName).userRole(userRole)
                    .ipAddress(ip).timestamp(LocalDateTime.now())
                    .success(success).build());
        } catch (Exception e) {
            log.warn("Activity log write failed: {}", e.getMessage());
        }
    }

    @Async
    public void logPayment(String action, String details, Long userId, String userName,
                           String userRole, String ip, BigDecimal amount, String paymentRef, boolean success) {
        try {
            repo.save(ActivityLog.builder()
                    .action(action).details(details)
                    .userId(userId).userName(userName).userRole(userRole)
                    .ipAddress(ip).timestamp(LocalDateTime.now())
                    .success(success).paymentAmount(amount).paymentRef(paymentRef).build());
        } catch (Exception e) {
            log.warn("Activity log (payment) write failed: {}", e.getMessage());
        }
    }

    @Async
    public void logError(String action, String errorMessage, Long userId, String ip) {
        try {
            repo.save(ActivityLog.builder()
                    .action(ERROR).details(action)
                    .userId(userId).ipAddress(ip).timestamp(LocalDateTime.now())
                    .success(false).errorMessage(errorMessage).build());
        } catch (Exception e) {
            log.warn("Activity log (error) write failed: {}", e.getMessage());
        }
    }

    // ── Convenience overloads ─────────────────────────────────────────────
    public void logAnonymous(String action, String details, String ip, boolean success) {
        log(action, details, null, null, null, ip, success);
    }

    public List<ActivityLog> getRecent(int hours) {
        return repo.findRecent(LocalDateTime.now().minusHours(hours));
    }

    public List<ActivityLog> getAll(int page, int size) {
        return repo.findAllByOrderByTimestampDesc(PageRequest.of(page, size)).getContent();
    }

    public List<ActivityLog> getErrors() {
        return repo.findBySuccessFalseOrderByTimestampDesc();
    }

    public List<ActivityLog> getByUser(Long userId) {
        return repo.findByUserIdOrderByTimestampDesc(userId);
    }
}
