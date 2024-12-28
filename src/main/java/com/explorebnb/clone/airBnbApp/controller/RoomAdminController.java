package com.explorebnb.clone.airBnbApp.controller;

import com.explorebnb.clone.airBnbApp.dto.RoomDto;
import com.explorebnb.clone.airBnbApp.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomAdminController {
    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId, @RequestBody RoomDto roomDto){
        RoomDto roomDto1=roomService.createNewRoom(hotelId,roomDto);
        return new ResponseEntity<>(roomDto1, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(@PathVariable Long hotelId){
        List<RoomDto> roomDtoList=roomService.getAllRoomsInHotel(hotelId);
        return ResponseEntity.ok(roomDtoList);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long hotelId,@PathVariable Long roomId){
        RoomDto roomDto=roomService.getRoomById(roomId);
        return ResponseEntity.ok(roomDto);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable Long hotelId,@PathVariable Long roomId){
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }
}
