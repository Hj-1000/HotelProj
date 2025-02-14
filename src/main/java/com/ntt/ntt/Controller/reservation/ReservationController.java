package com.ntt.ntt.Controller.reservation;


import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Service.ReservationService;
import com.ntt.ntt.Service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
@Log4j2
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/register")
    public ResponseEntity<?> registerReservationProc(@AuthenticationPrincipal UserDetails userDetails,
                                                     @RequestBody ReservationDTO reservationDTO) {
        // 1. 로그인 여부 확인
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        // 2. 회원 정보 확인
        Optional<Member> memberOptional = memberRepository.findByMemberEmail(userDetails.getUsername());
        if (memberOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "회원 정보를 찾을 수 없습니다."));
        }
        Member member = memberOptional.get();

        // 3. 객실 정보 확인
        Optional<Room> roomOptional = roomRepository.findById(reservationDTO.getRoomId());
        if (roomOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "해당 객실을 찾을 수 없습니다."));
        }
        Room room = roomOptional.get();

        // 4. 체크인 & 체크아웃 날짜 검증
        LocalDate checkInDate;
        LocalDate checkOutDate;
        try {
            checkInDate = LocalDate.parse(reservationDTO.getCheckInDate());
            checkOutDate = LocalDate.parse(reservationDTO.getCheckOutDate());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "날짜 형식이 올바르지 않습니다."));
        }

        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "체크아웃 날짜는 체크인 날짜 이후여야 합니다."));
        }

        // 5. 숙박일 수 계산
        long stayDays = ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        // 6. 총 금액 계산 (객실 가격 × 숙박일 수)
        int totalPrice = room.getRoomPrice() * (int) stayDays;

        // 7. 예약 생성 및 저장
        try {
            reservationService.registerReservation(
                    reservationDTO.getRoomId(),
                    member.getMemberId(),
                    reservationDTO.getCheckInDate(),
                    reservationDTO.getCheckOutDate(),
                    reservationDTO.getCount()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "예약이 완료되었습니다.",
                    "stayDays", stayDays + "박",
                    "totalPrice", totalPrice + " KRW"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "예약 처리 중 오류 발생"));
        }
    }
}
