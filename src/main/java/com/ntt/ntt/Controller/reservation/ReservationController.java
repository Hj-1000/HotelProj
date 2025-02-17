package com.ntt.ntt.Controller.reservation;


import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.RoomRepository;
import com.ntt.ntt.Service.ReservationService;
import com.ntt.ntt.Service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Log4j2
@Tag(name = "reservationController", description = "유저 예약 관리 컨트롤러")
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reservation/register")
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

    @Operation(summary = "호텔 예약내역 조회", description = "호텔 예약내역 조회 페이지로 이동한다.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myPage/reservationList")
    public String reservationList(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam(value = "page", defaultValue = "0") int page, // 현재 페이지 (0부터 시작)
                                  @RequestParam(value = "size", defaultValue = "5") int size, // 페이지 크기 (5개)
                                  Model model) {
        if (userDetails == null) {
            return "redirect:/login";  // 로그인되어있지 않으면 로그인 페이지로 리다이렉트
        }

        Optional<Member> memberOptional = memberRepository.findByMemberEmail(userDetails.getUsername());
        if (memberOptional.isEmpty()) {
            return "redirect:/login"; // 회원 정보가 없으면 로그인 페이지로 리다이렉트
        }

        Member member = memberOptional.get();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkInDate", "reservationId"));

        Page<ReservationDTO> reservationsPage = reservationService.getUserReservations(member.getMemberId(), pageable);

        // 현재 페이지가 존재하지 않는 경우 마지막 페이지로 이동하도록 처리
        if (reservationsPage.getTotalPages() > 0 && page >= reservationsPage.getTotalPages()) {
            return "redirect:/myPage/reservationList?page=" + (reservationsPage.getTotalPages() - 1);
        }

        // 데이터가 없을 경우 빈 리스트 반환
        List<ReservationDTO> reservations = reservationsPage.getContent();  // `getContent()` 사용

        // 이미지 리스트 확인
        for (ReservationDTO reservation : reservations) {
            if (reservation.getRoom() != null) {
                log.info("예약된 객실 ID: {}", reservation.getRoom().getRoomId());
                log.info("이미지 리스트 개수: {}", reservation.getRoom().getRoomImageDTOList().size());
                if (!reservation.getRoom().getRoomImageDTOList().isEmpty()) {
                    log.info("첫 번째 이미지 경로: {}", reservation.getRoom().getRoomImageDTOList().get(0).getImagePath());
                } else {
                    log.warn("객실 ID {} - 이미지 없음", reservation.getRoom().getRoomId());
                }
            }
        }

        // Thymeleaf에서 사용하기 위해 Model에 데이터 추가
        model.addAttribute("reservations", reservations);
        model.addAttribute("currentPage", reservationsPage.getNumber()); // 현재 페이지 번호
        model.addAttribute("totalPages", reservationsPage.getTotalPages()); // 전체 페이지 수

        return "myPage/reservationList";
    }


    //  고객예약 취소 요청
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/myPage/reservation/cancel")
    public ResponseEntity<?> requestCancelReservation(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestParam Integer reservationId) {
        Optional<Member> memberOptional = memberRepository.findByMemberEmail(userDetails.getUsername());
        if (memberOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("로그인이 필요합니다.");
        }

        Member member = memberOptional.get();

        try {
            // 기존 직접 접근 방식 대신 Service의 메서드 호출로 변경
            reservationService.requestCancelReservation(reservationId, member.getMemberId());
            return ResponseEntity.ok("예약 취소 요청이 접수되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
