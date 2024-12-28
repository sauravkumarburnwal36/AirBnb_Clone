package com.explorebnb.clone.airBnbApp.service;


import com.explorebnb.clone.airBnbApp.dto.BookingDto;
import com.explorebnb.clone.airBnbApp.dto.BookingRequestDto;

public interface BookingService {
    BookingDto initalizeBooking(BookingRequestDto bookingRequestDto);
}
