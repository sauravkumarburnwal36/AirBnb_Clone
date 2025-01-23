package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.HotelPriceDto;
import com.explorebnb.clone.airBnbApp.dto.HotelSearchRequestDto;
import com.explorebnb.clone.airBnbApp.entity.Inventory;
import com.explorebnb.clone.airBnbApp.entity.Room;
import com.explorebnb.clone.airBnbApp.repository.HotelMinPriceRepository;
import com.explorebnb.clone.airBnbApp.repository.InventoryRepository;
import com.explorebnb.clone.airBnbApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today=LocalDate.now();
        LocalDate endDate=today.plusYears(1);
        for(;!today.isAfter(endDate);today=today.plusDays(1)){
            Inventory inventory=Inventory.builder()
                    .date(today)
                    .city(room.getHotel().getCity())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .price(room.getBasePrice())
                    .closed(false)
                    .hotel(room.getHotel())
                    .totalCount(room.getTotalCount())
                    .surgeFactor(BigDecimal.ONE)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with id: {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequestDto) {
        log.info("Searching Hotels using pagination and filter:{},{},{}",hotelSearchRequestDto.getCity()
        ,hotelSearchRequestDto.getStartDate(),hotelSearchRequestDto.getEndDate());
        Pageable pageable=PageRequest.of(hotelSearchRequestDto.getPageNumber(),hotelSearchRequestDto.getSize());
        Long dateCount=ChronoUnit.DAYS.between(hotelSearchRequestDto.getStartDate(),hotelSearchRequestDto.getEndDate())+1;
        //business logic -90 days
        Page<HotelPriceDto> hotelPage=hotelMinPriceRepository.findHotelsWithAvailableInventory(
                hotelSearchRequestDto.getCity(),hotelSearchRequestDto.getStartDate(),
                hotelSearchRequestDto.getEndDate(),hotelSearchRequestDto.getRoomCounts(),
                dateCount,pageable);
        return hotelPage;
    }


}
