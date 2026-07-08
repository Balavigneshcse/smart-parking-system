package com.smart.parking.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdatePricingRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Rate must be greater than 0")
        BigDecimal ratePerHour,

        @Min(value = 1, message = "Minimum hours must be at least 1")
        Integer minimumHours
) {}
