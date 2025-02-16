package com.explorebnb.clone.airBnbApp.controller;

import com.explorebnb.clone.airBnbApp.dto.*;
import com.explorebnb.clone.airBnbApp.entity.Hotel;
import com.explorebnb.clone.airBnbApp.service.HotelService;
import com.explorebnb.clone.airBnbApp.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    @Operation(summary = "Search hotels", tags = {"Browse Hotels"})
    public ResponseEntity<Page<HotelPriceResponseDto>> searchHotels(@RequestBody HotelSearchRequestDto hotelSearchRequestDto){
        var page=inventoryService.searchHotels(hotelSearchRequestDto);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    @Operation(summary = "Get a hotel info by hotelId", tags = {"Browse Hotels"})
    public ResponseEntity<HotelInfoDto> getHotelInfoById(@PathVariable Long hotelId,@RequestBody HotelInfoRequestDto hotelInfoRequestDto){
        return ResponseEntity.ok(hotelService.getHotelInfoById((hotelId),hotelInfoRequestDto));
    }
}
