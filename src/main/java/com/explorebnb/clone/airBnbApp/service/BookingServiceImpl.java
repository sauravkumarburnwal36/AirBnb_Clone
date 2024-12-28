package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.BookingDto;
import com.explorebnb.clone.airBnbApp.dto.BookingRequestDto;
import com.explorebnb.clone.airBnbApp.entity.*;
import com.explorebnb.clone.airBnbApp.entity.enums.BookingStatus;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    @Override
    @Transactional
    public BookingDto initalizeBooking(BookingRequestDto bookingRequestDto) {
        log.info("Initializing Booking with hotel:{} with room:{} having roomCounts:{} from:{}---{}",
                bookingRequestDto.getHotelId()
        ,bookingRequestDto.getRoomId(),bookingRequestDto.getRoomCounts(),bookingRequestDto.getCheckInDate(),
                bookingRequestDto.getCheckOutDate());
        Hotel hotel=hotelRepository.findById(bookingRequestDto.getHotelId()).orElseThrow(
                ()-> new ResourceNotFoundException("Hotel with id:"+bookingRequestDto.getHotelId()+" not found"));
        Room room=roomRepository.findById(bookingRequestDto.getRoomId()).orElseThrow(()-> new
                ResourceNotFoundException("Room with id:"+bookingRequestDto.getRoomId()+" not found"));
        List<Inventory> inventoryList=inventoryRepository.findAndLockAvailableInventory(bookingRequestDto.getRoomId(),
                bookingRequestDto.getCheckInDate(),bookingRequestDto.getCheckOutDate(),bookingRequestDto.getRoomCounts());
        long daysCount= ChronoUnit.DAYS.between(bookingRequestDto.getCheckInDate(),bookingRequestDto.getCheckOutDate())+1;

        if(inventoryList.size()!=daysCount)
        {
            throw new IllegalStateException("Room is not available anymore");
        }
        for(Inventory inventory:inventoryList){
            inventory.setReservedCount(inventory.getReservedCount()+bookingRequestDto.getRoomCounts());
        }
        inventoryRepository.saveAll(inventoryList);
        User user=new User();
        user.setId(1L);
        //create the booking

        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .amount(BigDecimal.TEN)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckOutDate())
                .roomCounts(bookingRequestDto.getRoomCounts())
                .user(user)
                .build();
        bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }
}
