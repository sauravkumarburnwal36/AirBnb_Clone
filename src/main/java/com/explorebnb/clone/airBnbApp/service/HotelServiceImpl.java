package com.explorebnb.clone.airBnbApp.service;

import com.explorebnb.clone.airBnbApp.dto.HotelDto;
import com.explorebnb.clone.airBnbApp.entity.Hotel;
import com.explorebnb.clone.airBnbApp.exception.ResourceNotFoundException;
import com.explorebnb.clone.airBnbApp.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating hotel with name:{}",hotelDto.getName());
        Hotel hotel=modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        hotel=hotelRepository.save(hotel);
        log.info("Created a new hotel with id:{}",hotelDto.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Fetching hotel with id:{}",id);
        Hotel hotel=hotelRepository.findById(id).orElseThrow(()->new
                ResourceNotFoundException("Hotel with id:"+id+" not found"));
        log.info("Hotel with id:{}",id);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel details for id:{}",id);
        Hotel hotel=hotelRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("Hotel with id: "+id+" doesn't exist"));
        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel=hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public void deleteHotelById(Long id) {
        boolean exists=hotelRepository.existsById(id);
        if(!exists)throw new ResourceNotFoundException("Hotel with id:"+id+" doesn't exist");
        hotelRepository.deleteById(id);
        //delete the future inverntories for this hotel

    }

    @Override
    public void activateHotel(Long hotelId) {
        log.info("Activating the Hotel with id:{}",hotelId);
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(
                ()->new ResourceNotFoundException("Hotel with id: "+hotelId+" doesn't exist."));
        hotel.setActive(true);
        //add the future inventories
        hotelRepository.save(hotel);

    }
}
