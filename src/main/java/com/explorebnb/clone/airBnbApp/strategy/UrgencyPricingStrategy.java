package com.explorebnb.clone.airBnbApp.strategy;

import com.explorebnb.clone.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UrgencyPricingStrategy implements PricingStrategy{
    private final PricingStrategy wrappedPricingStrategy;
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price=wrappedPricingStrategy.calculatePrice(inventory);
        LocalDate today=LocalDate.now();
        if(!inventory.getDate().isBefore(today)&&inventory.getDate().isBefore(today.plusDays(7)))
        {
            price=price.multiply(BigDecimal.valueOf(1.4));
        }
        return price;
    }
}
