package com.hotels.microservices.msvc_reservations.service;

import com.hotels.microservices.msvc_reservations.client.HotelClientRest;
import com.hotels.microservices.msvc_reservations.client.RoomCilentRest;
import com.hotels.microservices.msvc_reservations.dto.HotelDTO;
import com.hotels.microservices.msvc_reservations.dto.ReservationResponseDTO;
import com.hotels.microservices.msvc_reservations.dto.ReservationRequestDTO;
import com.hotels.microservices.msvc_reservations.dto.RoomDTO;
import com.hotels.microservices.msvc_reservations.mapper.IReservationMapper;
import com.hotels.microservices.msvc_reservations.model.Reservation;
import com.hotels.microservices.msvc_reservations.repository.IRepositoryReservation;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceReservation implements IServiceReservation{


    @Autowired
    IRepositoryReservation repositoryReservation;

    @Autowired
    IReservationMapper reservationMapper;

    @Autowired
    RoomCilentRest roomCilentRest;

    @Autowired
    HotelClientRest hotelClientRest;

    @Autowired
    private SequenceGeneratorService sequenceGenerator;

    @Override
    public ReservationResponseDTO create(ReservationRequestDTO reservationRequestDTO) {

        Boolean exists = repositoryReservation.existsByRoomIdAndDateOverlap(
                reservationRequestDTO.getRoomId(),
                reservationRequestDTO.getCheckInDate(),
                reservationRequestDTO.getCheckOutDate()
        );

        if (reservationRequestDTO.getCheckOutDate() == null
                || reservationRequestDTO.getCheckOutDate().isEqual(reservationRequestDTO.getCheckInDate())) {
            reservationRequestDTO.setCheckOutDate(reservationRequestDTO.getCheckInDate());
        }

        long days = ChronoUnit.DAYS.between(reservationRequestDTO.getCheckInDate(), reservationRequestDTO.getCheckOutDate()) + 1;
        if (days == 0){
            days = 1;
        }

        if (Boolean.TRUE.equals(exists)) {
            throw new RuntimeException("Room is  reservated  on  the dates");
        }

        RoomDTO roomDTO = roomCilentRest.getRoom(reservationRequestDTO.getRoomId()).getBody();
        HotelDTO hotelDTO = hotelClientRest.getHotel(reservationRequestDTO.getHotelId(),false).getBody();

        Reservation reservation = reservationMapper.toReservation(reservationRequestDTO);

        reservation.setPrice(roomDTO.getPricePerNight() * days);
        reservation.setOrderNumber(sequenceGenerator.generateSequence("reservationOrder"));

        repositoryReservation.save(reservation);

        ReservationResponseDTO reservationResponseDTO = reservationMapper.toReservationResponse(reservation);

        reservationResponseDTO.setUserName(null);
        reservationResponseDTO.setHotelName(hotelDTO.getName());
        reservationResponseDTO.setRoomNumber(roomDTO.getRoomNumber());

        return reservationResponseDTO;

    }

    @Override
    public List<ReservationResponseDTO> findAll() {
        List<Reservation> reservations = repositoryReservation.findAll();
        List<ReservationResponseDTO> responseList = new ArrayList<>();

        for (Reservation r : reservations) {
            ReservationResponseDTO dto = reservationMapper.toReservationResponse(r);

            RoomDTO roomDTO = roomCilentRest.getRoom(r.getRoomId()).getBody();
            HotelDTO hotelDTO = hotelClientRest.getHotel(r.getHotelId(), false).getBody();

            dto.setRoomNumber(roomDTO != null ? roomDTO.getRoomNumber() : 0);
            dto.setHotelName(hotelDTO != null ? hotelDTO.getName() : null);
            dto.setUserName(null); //cambiar

            responseList.add(dto);
        }

        return responseList;
    }

    @Override
    public ReservationResponseDTO findById(String id) {
        Reservation reservation = repositoryReservation.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        ReservationResponseDTO dto = reservationMapper.toReservationResponse(reservation);

        RoomDTO roomDTO = roomCilentRest.getRoom(reservation.getRoomId()).getBody();
        HotelDTO hotelDTO = hotelClientRest.getHotel(reservation.getHotelId(), false).getBody();

        dto.setRoomNumber(roomDTO != null ? roomDTO.getRoomNumber() : 0);
        dto.setHotelName(hotelDTO != null ? hotelDTO.getName() : null);
        dto.setUserName(null);//cambiar

        return dto;
    }

    @Override
    public void deleteById(String id) {
        repositoryReservation.deleteById(String.valueOf(id));
    }


}
