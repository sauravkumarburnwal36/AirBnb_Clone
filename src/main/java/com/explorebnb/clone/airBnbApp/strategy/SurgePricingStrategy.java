package com.explorebnb.clone.airBnbApp.strategy;

import com.explorebnb.clone.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SurgePricingStrategy implements PricingStrategy{
    private final PricingStrategy wrappedPricingStartegy;
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price=wrappedPricingStartegy.calculatePrice(inventory);
        return price.multiply(BigDecimal.valueOf(1.2));
    }
}
