package com.smart.parking.domain;

import com.smart.parking.domain.enums.StaffRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "place_staff")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PlaceStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private ParkingPlace place;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_role", nullable = false, length = 15)
    private StaffRole staffRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
    private User assignedBy;

    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) assignedAt = LocalDateTime.now();
    }
}
