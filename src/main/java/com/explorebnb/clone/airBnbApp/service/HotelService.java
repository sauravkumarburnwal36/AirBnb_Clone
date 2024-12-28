package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.HotelDto;
import com.explorebnb.clone.airBnbApp.dto.HotelInfoDto;
import com.explorebnb.clone.airBnbApp.entity.Hotel;

public interface HotelService {

    public HotelDto createNewHotel(HotelDto hotelDto);

    public HotelDto getHotelById(Long id);

    public HotelDto updateHotelById(Long id,HotelDto hotelDto);

    public void deleteHotelById(Long id);

    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId);
}
