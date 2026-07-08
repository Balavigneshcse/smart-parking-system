package com.smart.parking.domain;

import com.smart.parking.domain.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_sessions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VehicleSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private ParkingSpace space;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private ParkingPlace place;

    // Linked reservation if pre-booked
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_by_id")
    private User entryBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exit_by_id")
    private User exitBy;

    @Column(name = "total_fee", precision = 10, scale = 2)
    private BigDecimal totalFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "is_pre_booked")
    @Builder.Default
    private boolean preBooked = false;
}
