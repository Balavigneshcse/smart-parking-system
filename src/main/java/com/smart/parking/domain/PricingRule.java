package com.smart.parking.domain;

import com.smart.parking.domain.enums.SpaceType;
import com.smart.parking.domain.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pricing_rules")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "space_type", nullable = false, length = 15)
    private SpaceType spaceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 10)
    private VehicleType vehicleType;

    @Column(name = "rate_per_hour", nullable = false, precision = 8, scale = 2)
    private BigDecimal ratePerHour;

    @Column(name = "minimum_hours", nullable = false)
    @Builder.Default
    private int minimumHours = 1;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
