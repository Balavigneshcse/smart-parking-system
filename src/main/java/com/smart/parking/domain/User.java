package com.smart.parking.domain;

import com.smart.parking.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(length = 15)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // For ADMIN role — the place they manage
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managed_place_id")
    private ParkingPlace managedPlace;

    // For STATE_MANAGER role — the state they manage
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managed_state_id")
    private State managedState;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
