package com.smart.parking.domain;

import com.smart.parking.domain.enums.PaymentMode;
import com.smart.parking.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private ParkingSpace space;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id")
    private User customerUser;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(name = "quoted_amount", precision = 10, scale = 2)
    private BigDecimal quotedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 15)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
