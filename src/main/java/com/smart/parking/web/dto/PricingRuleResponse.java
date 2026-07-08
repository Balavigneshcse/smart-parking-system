package com.smart.parking.web.dto;

import java.math.BigDecimal;

public record PricingRuleResponse(
        Long       id,
        String     spaceType,
        String     vehicleType,
        BigDecimal ratePerHour,
        int        minimumHours
) {}
