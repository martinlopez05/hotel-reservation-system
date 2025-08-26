package com.hotels.microservices.msvc_hotels.controller;

import com.hotels.microservices.msvc_hotels.dtos.HotelDTO;
import com.hotels.microservices.msvc_hotels.model.Hotel;
import com.hotels.microservices.msvc_hotels.service.IServiceHotel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
public class HotelController {

    @Autowired
    IServiceHotel serviceHotel;

    @GetMapping
    public ResponseEntity<List<HotelDTO>> getAllHotel(){
        return ResponseEntity.ok(serviceHotel.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDTO> getHotel(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean includeRooms){
        return ResponseEntity.ok(serviceHotel.findById(id,includeRooms));
    }

    @PostMapping
    public ResponseEntity<HotelDTO> createHotel(@RequestBody HotelDTO hotelDTO){
        HotelDTO createdHotelDTO = serviceHotel.create(hotelDTO);
        return new ResponseEntity<>(createdHotelDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHotel(@PathVariable Long id){
        serviceHotel.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelDTO> editHotel(@RequestBody HotelDTO hotelDTO, @PathVariable Long id){
        HotelDTO editedHotelDTO = serviceHotel.edit(hotelDTO,id);
        return ResponseEntity.ok(editedHotelDTO);
    }


}
