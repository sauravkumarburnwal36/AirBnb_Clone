package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.*;
import com.explorebnb.clone.airBnbApp.entity.Hotel;
import com.explorebnb.clone.airBnbApp.entity.Room;
import com.explorebnb.clone.airBnbApp.entity.User;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.exception.UnAuthorisedException;
import com.explorebnb.clone.airBnbApp.repository.HotelRepository;
import com.explorebnb.clone.airBnbApp.repository.InventoryRepository;
import com.explorebnb.clone.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.explorebnb.clone.airBnbApp.util.AppUtils.getCurrentUser;

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
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
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
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this hotel with id:"+id);
        }
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel details for id:{}",id);
        Hotel hotel=hotelRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("Hotel with id: "+id+" doesn't exist"));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this hotel with id:"+id);
        }
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
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this hotel with id:"+id);
        }
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
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this hotel with id:"+hotelId);
        }
        hotel.setActive(true);
        //add the future inventories
        for(Room room:hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }

    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto) {
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException(
                "Hotel with id:"+hotelId+" doesn't exist."));
        long daysCount = ChronoUnit.DAYS.between(hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate())+1;
        List<RoomPriceDto> roomPriceDtoList = inventoryRepository.findRoomAveragePrice(hotelId,
                hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate(),
                hotelInfoRequestDto.getRoomsCount(), daysCount);

        List<RoomPriceResponseDto> rooms = roomPriceDtoList.stream()
                .map(roomPriceDto -> {
                    RoomPriceResponseDto roomPriceResponseDto = modelMapper.map(roomPriceDto.getRoom(),
                            RoomPriceResponseDto.class);
                    roomPriceResponseDto.setPrice(roomPriceDto.getPrice());
                    return roomPriceResponseDto;
                })
                .collect(Collectors.toList());

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);

    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user=getCurrentUser();
        log.info("Fetching All Hotels having user with Id:{}",user.getId());
        List<Hotel> hotels=hotelRepository.findByOwner(user);
        return hotels.stream().map(element->modelMapper.map(element,HotelDto.class))
                .collect(Collectors.toList());
    }
}
