package com.explorebnb.clone.airBnbApp.strategy;

import com.explorebnb.clone.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy{
    private final PricingStrategy wrappedPricingStrategy;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price=wrappedPricingStrategy.calculatePrice(inventory);
        double occupancyRate=(double)inventory.getBookedCount()/ inventory.getTotalCount();
        if(occupancyRate>0.8)
        {
            price=price.multiply(BigDecimal.valueOf(1.8));
        }
        return price;
    }
}
