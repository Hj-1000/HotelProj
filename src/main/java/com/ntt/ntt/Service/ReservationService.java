package com.ntt.ntt.Service;


import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Reservation;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    // 1. 방 예약 추가
    public ReservationDTO registerReservation(Integer roomId, Integer memberId , String checkInDate , String checkOutDate) {

        // 방 정보 확인
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));

        // 이미 예약된 방인지 확인
        if (!room.getRoomStatus()) {
            throw new IllegalStateException("이미 예약된 방입니다. roomId : " + roomId);
        }

        // 회원 정보 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 정보를 찾을 수 없습니다. memberId : " + memberId));


        // 날짜 검증
        if (!isValidDateRange(checkInDate, checkOutDate)) {
            throw new IllegalArgumentException("체크인 날짜는 체크아웃 날짜보다 이전이어야 합니다.");
        }

        try {
            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .checkInDate(checkInDate)
                    .checkOutDate(checkOutDate)
                    .totalPrice(room.getRoomPrice())
                    .reservationStatus("예약됨")
                    .room(room)
                    .member(member)
                    .build();

            reservationRepository.save(reservation);

            // 방 상태 변경 (예약 완료 후)
            room.setRoomStatus(false);
            roomRepository.save(room);

            return ReservationDTO.fromEntity(reservation);

        } catch (Exception e) {
            throw new RuntimeException("예약 처리 중 오류가 발생했습니다.", e);
        }

    }

    /* 체크인 날짜가 체크아웃 날짜보다 이전인지 검증하는 메서드 */

    private boolean isValidDateRange(String checkInDate, String checkOutDate) {
        try {
            LocalDate checkIn = LocalDate.parse(checkInDate);
            LocalDate checkOut = LocalDate.parse(checkOutDate);
            return checkIn.isBefore(checkOut); // 체크인 날짜가 체크아웃 날짜보다 이전이면 true 반환
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다. (yyyy-MM-dd 형식이어야 합니다.)");
        }
    }

    // 2. 모든 예약 목록 가져오기
    public Page<ReservationDTO> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable)
                .map(ReservationDTO::fromEntity);
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
        RoomDTO roomDTO = reservationDTO.getRoom(); // RoomDTO 객체에서 예약 마감 날짜 가져오기
        if (roomDTO != null) {
            reservation.getRoom().setReservationEnd(roomDTO.getReservationEnd()); // Room 엔티티에 반영
        }

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
