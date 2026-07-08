package com.smart.parking.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parking_zones")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ParkingZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private ParkingPlace place;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String level;

    @Column(length = 200)
    private String description;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ParkingSpace> spaces = new ArrayList<>();
}
