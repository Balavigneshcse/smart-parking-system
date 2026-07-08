package com.smart.parking.repository;

import com.smart.parking.domain.PricingRule;
import com.smart.parking.domain.enums.SpaceType;
import com.smart.parking.domain.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    Optional<PricingRule> findBySpaceTypeAndVehicleTypeAndActiveTrue(SpaceType spaceType, VehicleType vehicleType);
}
