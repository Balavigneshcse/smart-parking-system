package com.smart.parking.service;

import com.smart.parking.domain.PricingRule;
import com.smart.parking.domain.enums.SpaceType;
import com.smart.parking.domain.enums.VehicleType;
import com.smart.parking.exception.BusinessRuleException;
import com.smart.parking.exception.ResourceNotFoundException;
import com.smart.parking.repository.PricingRuleRepository;
import com.smart.parking.web.dto.PricingRuleResponse;
import com.smart.parking.web.dto.UpdatePricingRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PricingService {

    private final PricingRuleRepository pricingRepo;

    public PricingService(PricingRuleRepository pricingRepo) {
        this.pricingRepo = pricingRepo;
    }

    /** Used by the public /api/prices endpoint and the admin pricing panel. */
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> listAll() {
        return pricingRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /** Admin updates the rate / minimum-hours for an existing pricing rule. */
    @Transactional
    public PricingRuleResponse update(Long id, UpdatePricingRequest req) {
        PricingRule rule = pricingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PricingRule", id));
        rule.setRatePerHour(req.ratePerHour());
        if (req.minimumHours() != null) rule.setMinimumHours(req.minimumHours());
        return toResponse(pricingRepo.save(rule));
    }

    /** Admin creates a new pricing rule for a space/vehicle combination not yet priced. */
    @Transactional
    public PricingRuleResponse create(String spaceTypeStr, String vehicleTypeStr, UpdatePricingRequest req) {
        SpaceType   spaceType   = SpaceType.valueOf(spaceTypeStr.toUpperCase());
        VehicleType vehicleType = VehicleType.valueOf(vehicleTypeStr.toUpperCase());

        pricingRepo.findBySpaceTypeAndVehicleTypeAndActiveTrue(spaceType, vehicleType)
                .ifPresent(r -> { throw new BusinessRuleException(
                        "A pricing rule for " + spaceType + " / " + vehicleType + " already exists."); });

        PricingRule rule = PricingRule.builder()
                .spaceType(spaceType)
                .vehicleType(vehicleType)
                .ratePerHour(req.ratePerHour())
                .minimumHours(req.minimumHours() != null ? req.minimumHours() : 1)
                .active(true)
                .build();
        return toResponse(pricingRepo.save(rule));
    }

    private PricingRuleResponse toResponse(PricingRule r) {
        return new PricingRuleResponse(
                r.getId(), r.getSpaceType().name(), r.getVehicleType().name(),
                r.getRatePerHour(), r.getMinimumHours());
    }
}
