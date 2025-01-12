package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.entity.Hotel;
import com.explorebnb.clone.airBnbApp.entity.HotelMinPrice;
import com.explorebnb.clone.airBnbApp.entity.Inventory;
import com.explorebnb.clone.airBnbApp.repository.HotelMinPriceRepository;
import com.explorebnb.clone.airBnbApp.repository.HotelRepository;
import com.explorebnb.clone.airBnbApp.repository.InventoryRepository;
import com.explorebnb.clone.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {
    private final HotelRepository hotelRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingService pricingService;

    @Scheduled(cron ="0 0 * * * *")
    public void updatePrice(){
        int page=0;
        int batchSize=100;
        while(true)
        {
            Page<Hotel> hotelPage=hotelRepository.findAll(PageRequest.of(page,batchSize));
            if(hotelPage.isEmpty())
            {
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrice);
            page++;
        }
    }

    private void updateHotelPrice(Hotel hotel) {
        log.info("Updating hotel price for hotel with id:{}",hotel.getId());
        LocalDate startDate= LocalDate.now();
        LocalDate endDate=LocalDate.now().plusYears(1);
        List<Inventory> inventoryList=inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);
        updateInventoryPrice(inventoryList);
        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);

         }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate,BigDecimal> dailyMinPrice=inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice,Collectors.minBy(Comparator.naturalOrder()))
                )).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e->e.getValue().orElse(
                        BigDecimal.ZERO)));
        List<HotelMinPrice> hotelMinPrices=new ArrayList<>();
        dailyMinPrice.forEach((date,price)->{
            HotelMinPrice hotelMinPrice=hotelMinPriceRepository.findByHotelAndDate(hotel,date).orElse(new HotelMinPrice(hotel,date));
            hotelMinPrice.setPrice(price);
            hotelMinPrices.add(hotelMinPrice);
        });
        hotelMinPriceRepository.saveAll(hotelMinPrices);
    }


    private void updateInventoryPrice(List<Inventory> inventoryList) {
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice=pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }

}
