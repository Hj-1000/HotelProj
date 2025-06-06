package com.ntt.ntt.Controller.reservation;


import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.ReservationRepository;
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
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Log4j2
@Tag(name = "ReservationController", description = "유저 예약 관리 컨트롤러")
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    /* -----------유저 페이지----------- */

    // 날짜 형식 지정
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Operation(summary = "호텔 객실 예약", description = "유저가 호텔 객실을 예약하는 API")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reservation/register")
    public ResponseEntity<?> registerReservationProc(@AuthenticationPrincipal UserDetails userDetails,
                                                     @RequestBody ReservationDTO reservationDTO) {
        try {
            Integer roomId = reservationDTO.getRoomId();
            Integer count = reservationDTO.getCount();
            var checkInDate = reservationDTO.getCheckInDate();
            var checkOutDate = reservationDTO.getCheckOutDate();

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

            Optional<Room> roomOptional = roomRepository.findById(roomId);
            if (roomOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "해당 객실을 찾을 수 없습니다."));
            }
            Room room = roomOptional.get();

            // 체크인 & 체크아웃 날짜 검증
            if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "체크아웃 날짜는 체크인 날짜 이후여야 합니다."));
            }

            // 숙박일 수 계산
            long stayDays = ChronoUnit.DAYS.between(checkInDate.toLocalDate(), checkOutDate.toLocalDate());

            // 총 금액 계산
            int totalPrice = room.getRoomPrice() * (int) stayDays;

            // 예약 등록 실행
            reservationService.registerReservation(roomId, member.getMemberId(), checkInDate, checkOutDate, count);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "예약이 완료되었습니다.",
                    "stayDays", stayDays + "박",
                    "totalPrice", totalPrice + " KRW"
            ));

        } catch (Exception e) {
            log.error("예약 처리 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "호텔 예약내역 조회", description = "호텔 예약내역 조회 페이지로 이동한다.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myPage/reservationList")
    public String reservationListForm(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "size", defaultValue = "5") int size,
                                      Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Optional<Member> memberOptional = memberRepository.findByMemberEmail(userDetails.getUsername());
        if (memberOptional.isEmpty()) {
            return "redirect:/login";
        }

        Member member = memberOptional.get();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkInDate", "reservationId"));

        Page<ReservationDTO> reservationsPage = reservationService.getUserReservations(member.getMemberId(), pageable);

        if (reservationsPage.getTotalPages() > 0 && page >= reservationsPage.getTotalPages()) {
            return "redirect:/myPage/reservationList?page=" + (reservationsPage.getTotalPages() - 1);
        }

        List<ReservationDTO> reservations = reservationsPage.getContent();

        model.addAttribute("reservations", reservations);
        model.addAttribute("currentPage", reservationsPage.getNumber());
        model.addAttribute("totalPages", reservationsPage.getTotalPages());

        return "myPage/reservationList";
    }

    @Operation(summary = "객실 예약된 날짜 조회", description = "특정 객실의 예약된 날짜를 조회하는 API")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservation/booked-dates/{roomId}")
    public ResponseEntity<?> getBookedDatesForm(@PathVariable Integer roomId) {
        if (roomId == null) {
            return ResponseEntity.badRequest().body(" 방 ID가 필요합니다.");
        }

        List<Map<String, String>> bookedDates = reservationService.getBookedDatesByRoom(roomId);

        if (bookedDates.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList()); // 빈 배열 반환
        }

        return ResponseEntity.ok(bookedDates);
    }

    @Operation(summary = "유저 예약 취소 요청", description = "유저가 자신의 예약을 취소 요청하는 API")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/myPage/reservation/cancel")
    public ResponseEntity<?> requestCancelReservationProc(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestParam Integer reservationId) {
        Optional<Member> memberOptional = memberRepository.findByMemberEmail(userDetails.getUsername());
        if (memberOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("로그인이 필요합니다.");
        }

        Member member = memberOptional.get();

        try {
            reservationService.requestCancelReservation(reservationId, member.getMemberId());
            return ResponseEntity.ok("예약 취소 요청이 접수되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "관리자 예약 취소 승인", description = "관리자가 유저의 예약 취소 요청을 승인하는 API")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @PostMapping("/admin/reservation/cancel")
    public ResponseEntity<?> approveCancelReservationProc(@RequestParam Integer reservationId) {
        try {
            reservationService.approveCancelReservation(reservationId);
            return ResponseEntity.ok("예약이 취소 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "유저 예약 삭제", description = "유저가 본인의 취소된 예약을 삭제하는 API")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/myPage/reservation/delete")
    public ResponseEntity<Map<String, Object>> deleteReservationProc(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Integer reservationId) {

        Optional<Member> memberOptional = memberRepository.findByMemberEmail(userDetails.getUsername());
        if (memberOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        Member member = memberOptional.get();

        try {
            boolean isDeleted = reservationService.deleteReservation(reservationId, member.getMemberId());

            //  이미 삭제된 예약도 성공 처리
            if (!isDeleted) {
                log.warn("삭제 요청했지만 이미 삭제된 예약 ID: {}", reservationId);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "예약 내역이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
