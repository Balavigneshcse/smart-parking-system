package com.smart.parking.domain;

import com.smart.parking.domain.enums.SpaceStatus;
import com.smart.parking.domain.enums.SpaceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parking_spaces")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ParkingSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private ParkingZone zone;

    @Column(nullable = false, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SpaceType spaceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private SpaceStatus status = SpaceStatus.AVAILABLE;
}
