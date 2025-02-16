package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.*;
import com.explorebnb.clone.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;


public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);


    Page<HotelPriceResponseDto> searchHotels(HotelSearchRequestDto hotelSearchRequestDto);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
