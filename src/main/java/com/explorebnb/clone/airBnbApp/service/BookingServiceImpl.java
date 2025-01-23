package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.BookingDto;
import com.explorebnb.clone.airBnbApp.dto.BookingRequestDto;
import com.explorebnb.clone.airBnbApp.dto.GuestDto;
import com.explorebnb.clone.airBnbApp.entity.*;
import com.explorebnb.clone.airBnbApp.entity.enums.BookingStatus;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.exception.UnAuthorisedException;
import com.explorebnb.clone.airBnbApp.repository.*;
import com.explorebnb.clone.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;
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

        //use JPQL to initialize booking
        inventoryRepository.initBooking(room.getId(),bookingRequestDto.getCheckInDate(),bookingRequestDto.getCheckOutDate(),bookingRequestDto.getRoomCounts());
        BigDecimal priceForOneRoom=pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice=priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequestDto.getRoomCounts()));
        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .amount(totalPrice)
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

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking with id:"+bookingId+ "not founed"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorisedException("Booking  does not belong to this user with id:"+user.getId());
        }
        if(hasBookingExpired(booking))
        {
            throw new IllegalStateException("Booking has already expired");
        }
        String sessionUrl=checkoutService.getCheckoutSession(booking,frontendUrl+"/payments/success",frontendUrl+"/payments/failure");
        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayments(Event event) {
        if("checkout.session.completed".equals(event.getType()))
        {
            Session session=(Session)event.getDataObjectDeserializer().getObject().orElse(null);
            if(session!=null){
                String sessionId=session.getId();
                Booking booking=bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(()-> new
                        ResourceNotFoundException("Booking not found with sessionId:"+sessionId));
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),
                        booking.getCheckOutDate(),booking.getRoomCounts());
                inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckInDate(),
                        booking.getCheckOutDate(),booking.getRoomCounts());
                log.info("Successfully Confirmed the booking for Booking Id:{}",booking.getId());
            }
            else{
                log.warn("Unhandled Event Type:{}",event.getType());

            }
        }
    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking with id:"+bookingId+ "not founed"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorisedException("Booking  does not belong to this user with id:"+user.getId());
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(
                ()->new ResourceNotFoundException("Booking with id:"+bookingId+ "not founed"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorisedException("Booking  does not belong to this user with id:"+user.getId());
        }
        if(booking.getBookingStatus()!=BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed booking can be cancelled");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate()
        ,booking.getCheckOutDate(),booking.getRoomCounts());
        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomCounts());

      try {
          Session session=Session.retrieve(booking.getPaymentSessionId());
          RefundCreateParams refundParam=RefundCreateParams.builder()
                  .setPaymentIntent(session.getPaymentIntent())
                          .build();
          Refund.create(refundParam);
      }
      catch (StripeException e){
          throw new RuntimeException(e);
      }

    }

    private boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    private User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
