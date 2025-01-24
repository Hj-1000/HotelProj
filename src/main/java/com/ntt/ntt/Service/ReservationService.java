package com.ntt.ntt.Service;


import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Reservation;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;

    // 방 목록 가져오기
    public List<RoomDTO> getRooms() {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream()
                .map(room -> modelMapper.map(room, RoomDTO.class))
                .collect(Collectors.toList());
    }

    // 방 상세 정보 가져오기
    public RoomDTO getRoomDetails(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));
        return modelMapper.map(room, RoomDTO.class);
    }

    // 예약 추가
    public Integer registerReservation(ReservationDTO reservationDTO) {
        Reservation reservation = modelMapper.map(reservationDTO, Reservation.class);
        reservationRepository.save(reservation);
        return reservation.getReservationId();
    }

    // 예약 수정
    public void updateReservation(Integer reservationId, ReservationDTO reservationDTO) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));
        reservation.setCheckInDate(reservationDTO.getCheckInDate());
        reservation.setCheckOutDate(reservationDTO.getCheckOutDate());
        reservation.setTotalPrice(reservationDTO.getTotalPrice());
        reservation.setReservationStatus(reservationDTO.getReservationStatus());
        reservationRepository.save(reservation);
    }

    // 예약 삭제
    public void deleteReservation(Integer reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    // 방 상태 변경
    public void changeRoomStatus(Integer roomId, String status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));

        // String 값을 Boolean으로 변환
        Boolean booleanStatus = Boolean.valueOf(status);

        room.setRoomStatus(booleanStatus);
        roomRepository.save(room);
    }
}
