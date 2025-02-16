package com.explorebnb.clone.airBnbApp.controller;

import com.explorebnb.clone.airBnbApp.dto.BookingDto;
import com.explorebnb.clone.airBnbApp.dto.HotelDto;
import com.explorebnb.clone.airBnbApp.dto.HotelReportDto;
import com.explorebnb.clone.airBnbApp.service.BookingService;
import com.explorebnb.clone.airBnbApp.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@Slf4j
@RequiredArgsConstructor
public class HotelController {
    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new hotel", tags = {"Admin Hotel"})
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto){
        log.info("Attempting to create hotel with name:{}",hotelDto.getName());
        HotelDto hotelDto1=hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotelDto1, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    @Operation(summary = "Get a hotel by Id", tags = {"Admin Hotel"})
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId){
        log.info("Fetching hotel witd id:{}",hotelId);
        HotelDto hotelDto=hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotelDto);
    }

    @PutMapping("/{hotelId}")
    @Operation(summary = "Update a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId,@RequestBody HotelDto hotelDto){
        log.info("Updating hotel details for hotel id:{}",hotelId);
        HotelDto hotelDto1=hotelService.updateHotelById(hotelId,hotelDto);
        return ResponseEntity.ok(hotelDto);
    }

    @DeleteMapping("/{hotelId}")
    @Operation(summary = "Delete a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId){
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}/activate")
    @Operation(summary = "Activate a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId){
        hotelService.activateHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all hotels owned by admin", tags = {"Admin Hotel"})
    public ResponseEntity<List<HotelDto>> getAllHotels(){
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/{hotelId}/{bookings}")
    @Operation(summary = "Get all bookings of a hotel", tags = {"Admin Bookings"})
    public ResponseEntity<List<BookingDto>> getAllBookingsByHotelId(@PathVariable Long hotelId){
        return ResponseEntity.ok(bookingService.getAllBookingsByHotelId(hotelId));
    }

    @GetMapping("/{hotelId}/{reports}")
    @Operation(summary = "Generate a bookings report of a hotel", tags = {"Admin Bookings"})
    public ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId,
                                                         @RequestParam(required = false)LocalDate startDate,
                                                         @RequestParam(required = false)LocalDate endDate){
        if(startDate==null){
            startDate=LocalDate.now().minusMonths(1);
        }
        if(endDate==null){
            endDate=LocalDate.now();
        }
        return ResponseEntity.ok(bookingService.getHotelReport(hotelId,startDate,endDate));
    }
}
