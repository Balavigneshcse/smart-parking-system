package com.smart.parking.web;

import com.smart.parking.service.PricingService;
import com.smart.parking.web.dto.PricingRuleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Public endpoint — no authentication required.
 * Returns the latest pricing rules so customers always see admin-updated rates.
 */
@RestController
@RequestMapping("/api/prices")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public ResponseEntity<List<PricingRuleResponse>> listPrices() {
        return ResponseEntity.ok(pricingService.listAll());
    }
}
