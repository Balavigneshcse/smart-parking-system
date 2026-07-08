package com.smart.parking.service;

import com.smart.parking.domain.PricingRule;
import com.smart.parking.domain.enums.SpaceType;
import com.smart.parking.domain.enums.VehicleType;
import com.smart.parking.exception.BusinessRuleException;
import com.smart.parking.repository.PricingRuleRepository;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class FeeCalculator {

    private final PricingRuleRepository pricingRuleRepo;

    public FeeCalculator(PricingRuleRepository pricingRuleRepo) {
        this.pricingRuleRepo = pricingRuleRepo;
    }

    public BigDecimal calculate(SpaceType spaceType, VehicleType vehicleType,
                                LocalDateTime from, LocalDateTime to) {
        PricingRule rule = pricingRuleRepo
                .findBySpaceTypeAndVehicleTypeAndActiveTrue(spaceType, vehicleType)
                .orElseThrow(() -> new BusinessRuleException(
                        "No pricing rule found for " + spaceType + " / " + vehicleType));

        long minutes = Duration.between(from, to).toMinutes();
        if (minutes <= 0) throw new BusinessRuleException("Exit time must be after entry time.");

        // Round up to nearest hour, apply minimum hours
        long hours = (long) Math.ceil(minutes / 60.0);
        hours = Math.max(hours, rule.getMinimumHours());

        return rule.getRatePerHour()
                .multiply(BigDecimal.valueOf(hours))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
