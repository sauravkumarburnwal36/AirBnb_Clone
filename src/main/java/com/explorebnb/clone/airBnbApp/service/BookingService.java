package com.explorebnb.clone.airBnbApp.service;


import com.explorebnb.clone.airBnbApp.dto.BookingDto;
import com.explorebnb.clone.airBnbApp.dto.BookingRequestDto;
import com.explorebnb.clone.airBnbApp.dto.GuestDto;
import com.stripe.model.Event;

import java.util.List;

public interface BookingService {
    BookingDto initalizeBooking(BookingRequestDto bookingRequestDto);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);

    void capturePayments(Event event);

    String getBookingStatus(Long bookingId);

    void cancelBooking(Long bookingId);
}
