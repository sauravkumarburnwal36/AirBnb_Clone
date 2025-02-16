package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.HotelDto;
import com.explorebnb.clone.airBnbApp.dto.HotelInfoDto;
import com.explorebnb.clone.airBnbApp.dto.HotelInfoRequestDto;
import com.explorebnb.clone.airBnbApp.entity.Hotel;

import java.util.List;

public interface HotelService {

    HotelDto createNewHotel(HotelDto hotelDto);

     HotelDto getHotelById(Long id);

     HotelDto updateHotelById(Long id,HotelDto hotelDto);

     void deleteHotelById(Long id);

    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto);

    List<HotelDto> getAllHotels();
}
