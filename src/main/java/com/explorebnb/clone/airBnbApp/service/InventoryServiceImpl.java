package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.*;
import com.explorebnb.clone.airBnbApp.entity.Inventory;
import com.explorebnb.clone.airBnbApp.entity.Room;
import com.explorebnb.clone.airBnbApp.entity.User;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.repository.HotelMinPriceRepository;
import com.explorebnb.clone.airBnbApp.repository.InventoryRepository;
import com.explorebnb.clone.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.explorebnb.clone.airBnbApp.util.AppUtils.getCurrentUser;

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
    public Page<HotelPriceResponseDto> searchHotels(HotelSearchRequestDto hotelSearchRequestDto) {
        log.info("Searching Hotels using pagination and filter:{},{},{}",hotelSearchRequestDto.getCity()
        ,hotelSearchRequestDto.getStartDate(),hotelSearchRequestDto.getEndDate());
        Pageable pageable=PageRequest.of(hotelSearchRequestDto.getPageNumber(),hotelSearchRequestDto.getSize());
        Long dateCount=ChronoUnit.DAYS.between(hotelSearchRequestDto.getStartDate(),hotelSearchRequestDto.getEndDate())+1;
        //business logic -90 days
        Page<HotelPriceDto> hotelPage=hotelMinPriceRepository.findHotelsWithAvailableInventory(
                hotelSearchRequestDto.getCity(),hotelSearchRequestDto.getStartDate(),
                hotelSearchRequestDto.getEndDate(),hotelSearchRequestDto.getRoomCounts(),
                dateCount,pageable);
        return hotelPage.map(hotelPriceDto -> {
            HotelPriceResponseDto hotelPriceResponseDto = modelMapper.map(hotelPriceDto.getHotel(), HotelPriceResponseDto.class);
            hotelPriceResponseDto.setPrice(hotelPriceDto.getPrice());
            return hotelPriceResponseDto;
        });
    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Fetching all inventory for room with id:{}",roomId);
        Room room=roomRepository.findById(roomId).orElseThrow(()->
                new ResourceNotFoundException("Room not found with id:"+roomId));
        User user=getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new AccessDeniedException("You are not the owner of room with id:"+roomId);
        }
        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map(element->modelMapper.map(element,InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating Inventory for room id:{} between date range{} and {}",roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());
        Room room=roomRepository.findById(roomId).orElseThrow(()->
                new ResourceNotFoundException("Room not found with id:"+roomId));
        User user=getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new AccessDeniedException("You are not the owner of room with id:"+roomId);
        }
        inventoryRepository.findInventoryAndLockBeforeUpdate(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());
        inventoryRepository.updateInventory(roomId,updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(),updateInventoryRequestDto.getSurgeFactor()
        ,updateInventoryRequestDto.getClosed());
    }


}
