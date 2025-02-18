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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    // 1. 방 예약 추가
    public ReservationDTO registerReservation(Integer roomId, Integer memberId, String checkInDate, String checkOutDate, Integer count) {
        // 방 정보 확인
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다. roomId: " + roomId));

        // 이미 예약된 방인지 확인
        if (!room.getRoomStatus()) {
            throw new IllegalStateException("이미 예약된 방입니다. roomId: " + roomId);
        }

        // 회원 정보 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 정보를 찾을 수 없습니다. memberId: " + memberId));

        // 날짜 검증
        if (!isValidDateRange(checkInDate, checkOutDate)) {
            log.error("체크인 날짜는 체크아웃 날짜보다 이전이어야 합니다.");
            throw new IllegalArgumentException("체크인 날짜는 체크아웃 날짜보다 이전이어야 합니다.");
        }

        // 인원 수 제한 (최소 1명 ~ 최대 6명)
        if (count < 1 || count > 6) {
            throw new IllegalArgumentException("예약 인원 수는 최소 1명, 최대 6명까지 가능합니다.");
        }

        try {
            // 체크인, 체크아웃 날짜 변환
            LocalDate checkIn = LocalDate.parse(checkInDate);
            LocalDate checkOut = LocalDate.parse(checkOutDate);

            // 숙박 일수 계산 (체크아웃 날짜 - 체크인 날짜)
            long dayCount = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (dayCount <= 0) {
                throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다. checkInDate: " + checkInDate + ", checkOutDate: " + checkOutDate);
            }

            // 총 비용 계산 (객실 가격 * 숙박일)
            int totalPrice = room.getRoomPrice() * (int) dayCount;

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .checkInDate(checkInDate)
                    .checkOutDate(checkOutDate)
                    .dayCount((int) dayCount)
                    .totalPrice(totalPrice)
                    .reservationStatus("예약")
                    .count(count)
                    .room(room)
                    .member(member)
                    .build();

            reservationRepository.save(reservation);

            // 방 상태 변경 (예약 완료 후)
            room.setRoomStatus(false);
            roomRepository.save(room);

            return ReservationDTO.fromEntity(reservation);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다. (yyyy-MM-dd 형식이어야 합니다.) checkInDate: " + checkInDate + ", checkOutDate: " + checkOutDate, e);
        } catch (Exception e) {
            throw new RuntimeException("예약 처리 중 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    // 2. 모든 예약 목록 가져오기
    public Page<ReservationDTO> getAllReservations(Pageable pageable) {
        List<ReservationDTO> filteredReservations = reservationRepository.findAll(pageable)
                .map(ReservationDTO::fromEntity)
                .getContent()  // List로 변환
                .stream()
                .filter(reservation -> !"취소 완료".equals(reservation.getReservationStatus())) // "취소 완료" 제외
                .toList();

        return new PageImpl<>(filteredReservations, pageable, filteredReservations.size());
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

        // 숙박일 수 다시 계산
        LocalDate checkInDate = LocalDate.parse(reservationDTO.getCheckInDate());
        LocalDate checkOutDate = LocalDate.parse(reservationDTO.getCheckOutDate());
        int dayCount = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        reservation.setDayCount(dayCount);

        // 총 비용 계산 (객실 가격 * 숙박일 수)
        Room room = reservation.getRoom();
        int totalPrice = room.getRoomPrice() * dayCount;
        reservation.setTotalPrice(totalPrice);

        // 예약 인원수 업데이트
        reservation.setCount(reservationDTO.getCount());
        log.info("수정된 count 값: {}", reservation.getCount());

        // 예약자 정보 업데이트
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
        reservation.setMember(member);

        // 업데이트 후 저장
        reservationRepository.save(reservation);
    }

    // 5. 방 예약 삭제 (예약 취소 - 관리자)
    public void deleteReservation(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        // 예약 상태를 "취소 완료"로 변경
        reservation.setReservationStatus("취소 완료");
        reservationRepository.save(reservation);

        Room room = reservation.getRoom();

        // 해당 방의 다른 예약이 없는지 확인 후 상태 변경
        boolean hasOtherReservations = reservationRepository.existsByRoom_RoomId(room.getRoomId());

        if (!hasOtherReservations) {
            room.setRoomStatus(true); // 다른 예약이 없다면 예약 가능 상태로 변경
            roomRepository.save(room);
        }

    }

    // 유저가 "취소 완료" 상태의 예약을 직접 삭제하는 메서드
    public void deleteReservationByUser(Integer reservationId, Integer memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        if (!reservation.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 삭제할 수 있습니다.");
        }

        if (!"취소 완료".equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("취소 완료된 예약만 삭제할 수 있습니다.");
        }

        // 유저가 직접 삭제할 경우에만 DB에서 제거
        reservationRepository.delete(reservation);
    }

    public void approveCancelReservation(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        if (!"취소 요청".equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("취소 요청된 예약만 승인할 수 있습니다.");
        }

        // 관리자가 "취소 완료"로만 변경 (DB에서 삭제 X)
        reservation.setReservationStatus("취소 완료");
        reservationRepository.save(reservation);
    }

    /* 체크인 날짜가 체크아웃 날짜보다 이전인지 검증메서드 */
    private boolean isValidDateRange(String checkInDate, String checkOutDate) {
        try {
            LocalDate checkIn = LocalDate.parse(checkInDate);
            LocalDate checkOut = LocalDate.parse(checkOutDate);
            return checkIn.isBefore(checkOut); // 체크인 날짜가 체크아웃 날짜보다 이전이면 true 반환
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다. (yyyy-MM-dd 형식이어야 합니다.)");
        }
    }

    /* 키워드 검색 메서드 */

    public Page<ReservationDTO> searchReservations(String category, String keyword, Pageable pageable) {
        Page<Reservation> reservations;

        if (keyword == null || keyword.trim().isEmpty()) {
            return reservationRepository.findAll(pageable).map(ReservationDTO::fromEntity);
        }

        try {
            switch (category) {
                case "roomName":
                    reservations = reservationRepository.findByRoom_RoomNameContaining(keyword, pageable);
                    break;
                case "memberId":
                    Integer memberId = Integer.parseInt(keyword);
                    reservations = reservationRepository.findByMember_MemberId(memberId, pageable);
                    break;
                case "memberName":
                    reservations = reservationRepository.findByMember_MemberNameContaining(keyword, pageable);
                    break;
                default:
                    log.warn(" 잘못된 검색 카테고리 입력: {}", category);
                    return reservationRepository.findAll(pageable).map(ReservationDTO::fromEntity); // 기본값 전체 검색
            }
        } catch (NumberFormatException e) {
            log.error(" 예약자 ID 검색 시 숫자가 아닌 값 입력: {}", keyword, e);
            return reservationRepository.findAll(pageable).map(ReservationDTO::fromEntity);
        }

        return reservations.map(ReservationDTO::fromEntity);
    }

    // 특정 사용자의 예약 내역 조회 (모든 예약 가져오기)
    public Page<ReservationDTO> getUserReservations(Integer memberId, Pageable pageable) {
        Page<Reservation> reservationsPage = reservationRepository.findAllByMember_MemberId(memberId, pageable);

        // 로그로 예약 개수 출력 (기존 기능 유지)
        log.info("조회된 예약 개수 (Service): {}", reservationsPage.getTotalElements());

        // Page<Reservation>을 Page<ReservationDTO>로 변환
        return reservationsPage.map(ReservationDTO::fromEntity);
    }

    // 고객 예약 취소 요청
    public void requestCancelReservation(Integer reservationId, Integer memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

        if (!reservation.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        reservation.setReservationStatus("취소 요청");
        reservationRepository.save(reservation);
    }

}
