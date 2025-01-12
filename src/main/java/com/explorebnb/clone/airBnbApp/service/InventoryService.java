package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.HotelPriceDto;
import com.explorebnb.clone.airBnbApp.dto.HotelSearchRequestDto;
import com.explorebnb.clone.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;


public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);


    Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequestDto);
}
