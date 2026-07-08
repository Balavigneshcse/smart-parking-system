package com.smart.parking.domain;

import com.smart.parking.domain.enums.PlaceCategory;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parking_places")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ParkingPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlaceCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @Column(name = "total_spaces")
    @Builder.Default
    private int totalSpaces = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ParkingZone> zones = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
