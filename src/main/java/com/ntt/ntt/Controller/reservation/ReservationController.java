package com.ntt.ntt.Controller.reservation;


import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
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


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/register")
    public ResponseEntity<?> createReservation(@AuthenticationPrincipal UserDetails userDetails,
                                               @RequestBody ReservationDTO reservationDTO) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        Optional<Member> memberOptional = memberRepository.findByMemberEmail(userDetails.getUsername());
        if (memberOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "회원 정보를 찾을 수 없습니다."));
        }

        Member member = memberOptional.get();

        try {
            reservationService.registerReservation(
                    reservationDTO.getRoomId(),
                    member.getMemberId(),
                    reservationDTO.getCheckInDate(),
                    reservationDTO.getCheckOutDate(),
                    reservationDTO.getCount() // 예약 인원 추가
            );
            return ResponseEntity.ok(Map.of("success", true, "message", "예약이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "예약 실패"));
        }
    }
}
