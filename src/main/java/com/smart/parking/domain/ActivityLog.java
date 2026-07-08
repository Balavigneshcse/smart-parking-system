package com.smart.parking.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_log_user_id",   columnList = "user_id"),
    @Index(name = "idx_log_action",    columnList = "action"),
    @Index(name = "idx_log_timestamp", columnList = "timestamp")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ActivityLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", length = 150)
    private String userName;

    @Column(name = "user_role", length = 20)
    private String userRole;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 1000)
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    @Builder.Default
    private boolean success = true;

    @Column(name = "payment_amount", precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_ref", length = 100)
    private String paymentRef;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
