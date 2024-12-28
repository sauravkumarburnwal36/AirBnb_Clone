package com.explorebnb.clone.airBnbApp.controller;

import com.explorebnb.clone.airBnbApp.dto.BookingDto;
import com.explorebnb.clone.airBnbApp.dto.BookingRequestDto;
import com.explorebnb.clone.airBnbApp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class HotelBookingController {
    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDto> initalizeBooking(@RequestBody BookingRequestDto bookingRequestDto){
        return ResponseEntity.ok(bookingService.initalizeBooking(bookingRequestDto));
    }
}
