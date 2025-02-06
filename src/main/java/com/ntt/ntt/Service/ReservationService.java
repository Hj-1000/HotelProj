package com.ntt.ntt.Service;


import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Reservation;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final MemberRepository memberRepository;


    // 1. 방 예약 추가
    public ReservationDTO registerReservation(Integer roomId, Integer memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));

        if (!room.getRoomStatus()) {
            throw new IllegalStateException("이미 예약된 방입니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 정보를 찾을 수 없습니다."));

        Reservation reservation = Reservation.builder()
                .checkInDate(String.valueOf(LocalDate.now()))
                .checkOutDate(String.valueOf(LocalDate.now().plusDays(1)))
                .totalPrice(room.getRoomPrice())
                .reservationStatus("예약됨")
                .room(room)
                .member(member)
                .build();

        reservationRepository.save(reservation);

        // 예약 완료 후 상태 변경
        room.setRoomStatus(false);
        roomRepository.save(room);

        System.out.println("예약된 방 ID: " + room.getRoomId() + ", 상태: " + room.getRoomStatus()); // 디버깅 로그

        return ReservationDTO.fromEntity(reservation);
    }

    // 2. 모든 예약 목록 가져오기
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationDTO::fromEntity) // memberName, memberEmail 자동 포함
                .collect(Collectors.toList());
    }

    // 3. 특정 방의 예약 정보 조회
    public ReservationDTO getReservationByRoomId(Integer roomId) {
        Reservation reservation = reservationRepository.findFirstByRoom_RoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방의 예약 정보가 없습니다."));
        return ReservationDTO.fromEntity(reservation); // DTO 변환 적용
    }


    // 4. 방 예약 수정
    public void updateReservation(Integer reservationId, ReservationDTO reservationDTO, Integer memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        // 체크인/체크아웃 날짜 업데이트
        reservation.setCheckInDate(reservationDTO.getCheckInDate());
        reservation.setCheckOutDate(reservationDTO.getCheckOutDate());

        // 예약 마감 날짜 업데이트
        reservation.getRoom().setReservationEnd(reservationDTO.getReservationEnd());

        // 예약자 정보 업데이트
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
        reservation.setMember(member);

        reservationRepository.save(reservation);
    }

    // 5. 방 예약 삭제 (예약 취소)
    public void deleteReservation(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        Room room = reservation.getRoom();

        reservationRepository.delete(reservation);

        // 방을 예약 가능 상태로 변경
        room.setRoomStatus(true);
        roomRepository.save(room);
    }

}
