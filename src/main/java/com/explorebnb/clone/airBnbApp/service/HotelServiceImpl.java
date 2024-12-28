package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.HotelDto;
import com.explorebnb.clone.airBnbApp.dto.HotelInfoDto;
import com.explorebnb.clone.airBnbApp.dto.RoomDto;
import com.explorebnb.clone.airBnbApp.entity.Hotel;
import com.explorebnb.clone.airBnbApp.entity.Room;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.repository.HotelRepository;
import com.explorebnb.clone.airBnbApp.repository.InventoryRepository;
import com.explorebnb.clone.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    private final InventoryRepository inventoryRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating hotel with name:{}",hotelDto.getName());
        Hotel hotel=modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        hotel=hotelRepository.save(hotel);
        log.info("Created a new hotel with id:{}",hotelDto.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Fetching hotel with id:{}",id);
        Hotel hotel=hotelRepository.findById(id).orElseThrow(()->new
                ResourceNotFoundException("Hotel with id:"+id+" not found"));
        log.info("Hotel with id:{}",id);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel details for id:{}",id);
        Hotel hotel=hotelRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("Hotel with id: "+id+" doesn't exist"));
        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel=hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Transactional
    @Override
    public void deleteHotelById(Long id) {
        log.info("Deleting the hotel with id:{}",id);
        Hotel hotel=hotelRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("Hotel with id:{}"+id+" doesn't exist"));

        for(Room room:hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
        //delete the future inverntories for this hotel

    }

    @Transactional
    @Override
    public void activateHotel(Long hotelId) {
        log.info("Activating the Hotel with id:{}",hotelId);
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(
                ()->new ResourceNotFoundException("Hotel with id: "+hotelId+" doesn't exist."));
        hotel.setActive(true);
        //add the future inventories
        hotelRepository.save(hotel);
        for(Room room:hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }

    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException(
                "Hotel with id:"+hotelId+" doesn't exist."));
        List<RoomDto> roomDtos= hotel.getRooms().stream().map((element) -> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
        return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),roomDtos);
    }
}
