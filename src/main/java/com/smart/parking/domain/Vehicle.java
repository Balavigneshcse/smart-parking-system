package com.smart.parking.domain;

import com.smart.parking.domain.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plate_number", nullable = false, length = 20)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 10)
    private VehicleType vehicleType;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_phone", length = 15)
    private String ownerPhone;

    @Column(name = "owner_email")
    private String ownerEmail;

    // Null for on-spot / unregistered walk-ins
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id")
    private User customerUser;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
