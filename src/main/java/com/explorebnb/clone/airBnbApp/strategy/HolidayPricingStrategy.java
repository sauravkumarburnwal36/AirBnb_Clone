package com.explorebnb.clone.airBnbApp.strategy;

import com.explorebnb.clone.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{
    private final PricingStrategy wrappedPricingStrategy;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price=wrappedPricingStrategy.calculatePrice(inventory);
        boolean isHolidayToday= true;//call api to find
        if(isHolidayToday){
            price=price.multiply(BigDecimal.valueOf(2));
        }
        return price;
    }
}
