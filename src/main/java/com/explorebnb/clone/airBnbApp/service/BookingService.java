package com.explorebnb.clone.airBnbApp.service;


import com.explorebnb.clone.airBnbApp.dto.BookingDto;
import com.explorebnb.clone.airBnbApp.dto.BookingRequestDto;
import com.explorebnb.clone.airBnbApp.dto.GuestDto;
import com.explorebnb.clone.airBnbApp.dto.HotelReportDto;
import com.explorebnb.clone.airBnbApp.entity.enums.BookingStatus;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    BookingDto initalizeBooking(BookingRequestDto bookingRequestDto);

    BookingDto addGuests(Long bookingId, List<Long> guestIdList);

    String initiatePayments(Long bookingId);

    void capturePayments(Event event);

    BookingStatus getBookingStatus(Long bookingId);

    void cancelBooking(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBookings();
}
