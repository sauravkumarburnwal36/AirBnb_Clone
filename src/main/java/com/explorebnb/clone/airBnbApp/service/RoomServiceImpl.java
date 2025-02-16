package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.RoomDto;
import com.explorebnb.clone.airBnbApp.entity.Hotel;
import com.explorebnb.clone.airBnbApp.entity.Room;
import com.explorebnb.clone.airBnbApp.entity.User;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.exception.UnAuthorisedException;
import com.explorebnb.clone.airBnbApp.repository.HotelRepository;
import com.explorebnb.clone.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.explorebnb.clone.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService{
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room with hotelId:{}",hotelId);
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException(
                "Hotel with id: "+hotelId+" doesn't exist"));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this hotel with id:"+hotelId);
        }
        Room room=modelMapper.map(roomDto,Room.class);
        room.setHotel(hotel);
        room=roomRepository.save(room);
        //ToDO: Create the Inventory as room is created
        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Fetching all hotels with hotel id:{}",hotelId);
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel with id: "+hotelId
        +" doesn't exist"));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this hotel with id:"+hotelId);
        }
        return hotel.getRooms().stream().map((element) -> modelMapper.map(element, RoomDto.class)).collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Fetching Room with id:{}",roomId);
        Room room=roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException(
                "Room with id:"+roomId+" doesn't exist"));
        return modelMapper.map(room,RoomDto.class);
    }

    @Transactional
    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting Room with idZ:{}",roomId);
        Room room=roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException(
                "Room with id:"+roomId+" doesn't exist"
        ));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner()))
        {
            throw new UnAuthorisedException("This user does not own this room with id:"+roomId);
        }
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);
        //Delete the future inventory for this room
    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
        log.info("Updating Room with ID:{} in hotel with Id:{}",roomId,hotelId);
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException(
                "Hotel note found with id:"+hotelId));
        User user=getCurrentUser();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("You are not the owner of this hotel");
        }
        Room room=roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException(
                "Room not found with id:"+roomId));
        modelMapper.map(roomDto,room);
        room.setId(roomId);
        //:TODO if price or inventory is updated ,then update the inventory for this room
        room=roomRepository.save(room);
        return modelMapper.map(room,RoomDto.class);
    }

}
