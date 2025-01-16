package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.BookingDto;
import com.explorebnb.clone.airBnbApp.dto.BookingRequestDto;
import com.explorebnb.clone.airBnbApp.dto.GuestDto;
import com.explorebnb.clone.airBnbApp.entity.*;
import com.explorebnb.clone.airBnbApp.entity.enums.BookingStatus;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.exception.UnAuthorisedException;
import com.explorebnb.clone.airBnbApp.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        //create the booking

        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .amount(BigDecimal.TEN)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckOutDate())
                .roomCounts(bookingRequestDto.getRoomCounts())
                .user(getCurrentUser())
                .build();
        bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking id:{}",bookingId);
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()->
                new ResourceNotFoundException("Booking with id:"+bookingId+" not found"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to this user id:"+user.getId());
        }
        if(hasBookingExpired(booking))
        {
            throw new IllegalStateException("Booking has been expired");
        }
        if(booking.getBookingStatus()!=BookingStatus.RESERVED)
        {
            throw new IllegalStateException("Booking status is not in reserved state,cannot add guests");
        }
        for(GuestDto guestDto:guestDtoList){
            Guest guest=modelMapper.map(guestDto,Guest.class);
           guest.setUser(user);
            guest=guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking=bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);
    }

    private boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    private User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
