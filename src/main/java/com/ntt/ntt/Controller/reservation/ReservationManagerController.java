package com.ntt.ntt.Controller.reservation;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Reservation;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Service.ReservationService;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/manager/room/reservation")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "ReservationManagerController", description = "관리자 예약 관리 컨트롤러")
public class ReservationManagerController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final PaginationUtil paginationUtil;

    /* -----------관리자 페이지----------- */

    @Operation(summary = "객실 예약 등록", description = "관리자가 특정 객실에 대해 강제로 예약을 등록하는 API")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @PostMapping("/register")
    public String registerReservationProc(@RequestParam("roomId") Integer roomId,
                                          @RequestParam("memberId") Integer memberId,
                                          @RequestParam("checkInDate") LocalDateTime checkInDate,
                                          @RequestParam("checkOutDate") LocalDateTime checkOutDate,
                                          @RequestParam("count") Integer count,
                                          RedirectAttributes redirectAttributes) {
        try {
            reservationService.registerReservation(roomId, memberId, checkInDate, checkOutDate, count);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 등록 중 오류가 발생했습니다.");
        }
        return "redirect:/manager/room/reservation/list";
    }

    @Operation(summary = "객실 예약 목록 조회", description = "관리자가 등록된 모든 예약 목록을 조회하는 페이지.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @GetMapping("/list")
    public String listReservationForm(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, page = 0) Pageable pageable,
            @RequestParam(value = "roomId", required = false) Integer roomId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword) {

        log.info("예약 목록 페이지 호출 - 검색 category: {}, keyword: {}", category, keyword);

        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        String email = userDetails.getUsername();
        Member member = memberRepository.findByMemberEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 정보를 찾을 수 없습니다."));
        Integer memberId = member.getMemberId();
        Role role = member.getRole();

        // 상단 객실 목록 (권한별 페이징 처리)
        List<RoomDTO> allRoomList = roomService.getRoomListWithReservations(memberId, role);

        // 하단 객실 관리 현황의 모든 객실 (권한별 페이징 처리)
        Page<RoomDTO> roomPage;
        if ("roomName".equals(category) || "status".equals(category)) {
            // 검색어가 있으면 검색 결과를 가져옴
            roomPage = roomService.searchAllRooms(keyword, category, pageable);
        } else {
            // 검색어가 없으면 권한별 페이징 처리된 목록을 가져옴
            roomPage = roomService.getPaginatedRoomsWithReservations(memberId, role, pageable);
        }

        // 예약된 방 검색
        Page<ReservationDTO> reservationPage;
        if (roomId != null) {
            List<ReservationDTO> reservations = reservationService.getReservationsByRoomId(roomId);
            reservationPage = new PageImpl<>(reservations, pageable, reservations.size());
        } else if (category != null && keyword != null && !keyword.trim().isEmpty()) {
            reservationPage = reservationService.searchReservations(category, keyword, pageable);
        } else {
            reservationPage = reservationService.getPaginatedReservationsWithRole(memberId, role, pageable);
        }

        List<ReservationDTO> reservations = reservationPage.getContent();

        // 예약 목록이 비어있다면, 기본 빈 예약 객체 추가 (Thymeleaf에서 NullPointerException 방지)
        if (reservations.isEmpty()) {
            model.addAttribute("reservation", new ReservationDTO()); // 빈 객체 추가
        } else {
            model.addAttribute("reservation", reservations.get(0)); // 첫 번째 예약을 기본으로 설정
        }
        model.addAttribute("reservations", reservations);


        // 예약된 방 매핑
        List<ReservationDTO> allReservations = reservationService.getAllReservations(Pageable.unpaged()).getContent();
        Map<Integer, ReservationDTO> reservationMap = new HashMap<>();
        for (ReservationDTO reservation : allReservations) {
            reservationMap.put(reservation.getRoomId(), reservation);
        }

        // 로그인한 유저 정보 추가
        if (userDetails != null) {
            String userEmail = userDetails.getUsername(); // 변수명 변경
            Member userMember = memberRepository.findByMemberEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원 정보를 찾을 수 없습니다."));
            model.addAttribute("memberId", userMember.getMemberId());
        }

        // 페이지네이션 정보 생성
        Map<String, Integer> reservationPageInfo = paginationUtil.pagination(reservationPage);
        Map<String, Integer> roomPageInfo = paginationUtil.pagination(roomPage);

        // 모델에 데이터 추가
        model.addAttribute("roomList", allRoomList);
        model.addAttribute("reservationMap", reservationMap);
        model.addAttribute("reservationPage", reservationPage);
        model.addAttribute("roomPage", roomPage);
        model.addAttribute("reservationPageInfo", reservationPageInfo);
        model.addAttribute("roomPageInfo", roomPageInfo);
        model.addAttribute("currentPage", pageable.getPageNumber());

        // 검색어 정보 유지
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);

        log.info("예약 목록 개수: {}", reservationPage.getTotalElements());

        return "manager/room/reservation/list";
    }

    @Operation(summary = "특정 객실 예약 정보 조회", description = "관리자가 특정 객실의 예약 정보를 확인하는 API.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @GetMapping("/read")
    public String readReservationForm(@RequestParam("roomId") Integer roomId, Model model) {
        model.addAttribute("reservation", reservationService.getReservationByRoomId(roomId));

        List<RoomDTO> roomList = roomService.getAllRoomsWithReservations();
        Page<ReservationDTO> reservationPage = reservationService.getAllReservations(PageRequest.of(0, 10));

        Map<Integer, ReservationDTO> reservationMap = new HashMap<>();
        for (ReservationDTO reservation : reservationPage.getContent()) { // 현재 페이지 데이터만 가져옴
            reservationMap.put(reservation.getRoomId(), reservation);
        }

        model.addAttribute("roomList", roomList);
        model.addAttribute("reservationMap", reservationMap);
        model.addAttribute("reservationList", reservationPage.getContent()); // 현재 페이지 데이터만 전달
        model.addAttribute("reservationPage", reservationPage); // 페이징 정보 추가

        return "manager/room/reservation/list";
    }

    @Operation(summary = "예약 정보 수정", description = "관리자가 특정 예약 정보를 수정하는 API.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @PostMapping("/update")
    public String updateReservationProc(
            @RequestParam(value = "reservationId", required = false) Integer reservationId,
            @RequestParam(value = "roomId", required = true) Integer roomId,
            @RequestParam(value = "reservationEnd", required = false) String reservationEnd,
            @RequestParam(value = "checkInDate", required = false) String checkInDate,
            @RequestParam(value = "checkOutDate", required = false) String checkOutDate,
            @RequestParam(value = "count", required = false) Integer count,
            @RequestParam(value = "memberId", required = false) Integer memberId,
            @ModelAttribute ReservationDTO reservationDTO,
            RedirectAttributes redirectAttributes) {

        log.info("[updateReservationProc] 요청됨 - reservationId: {}, roomId: {}, reservationEnd: {}",
                reservationId, roomId, reservationEnd);

        if (roomId == null) {
            log.error("[updateReservationProc] 객실 ID(roomId)가 없습니다!");
            redirectAttributes.addFlashAttribute("errorMessage", "객실 ID가 필요합니다.");
            return "redirect:/manager/room/reservation/list";
        }

        try {
            // 예약 ID가 없으면 예약 마감 날짜만 수정 (기존 동작과 동일)
            if (reservationId == null) {
                log.info("[updateReservationProc] 예약 마감 날짜만 수정");

                roomService.updateReservationEnd(roomId, reservationEnd);  // String 그대로 전달
                roomService.updateRoomStatusBasedOnReservationEnd(roomId);
                redirectAttributes.addFlashAttribute("successMessage", "예약 마감 날짜가 성공적으로 수정되었습니다.");
                return "redirect:/manager/room/reservation/list";
            }

            // 예약이 존재하는 경우, 날짜 변환 후 예약 정보 수정
            LocalDateTime checkIn = checkInDate != null ? LocalDateTime.parse(checkInDate) : null;
            LocalDateTime checkOut = checkOutDate != null ? LocalDateTime.parse(checkOutDate) : null;

            reservationDTO.setCheckInDate(checkIn);
            reservationDTO.setCheckOutDate(checkOut);
            reservationDTO.setReservationEnd(reservationEnd);
            reservationDTO.setCount(count);

            reservationService.updateReservation(reservationId, reservationDTO, memberId);
            roomService.updateRoomStatusBasedOnReservationEnd(roomId);

            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 수정되었습니다.");
            return "redirect:/manager/room/reservation/list";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", " 예약 수정 실패: " + e.getMessage());
            return "redirect:/manager/room/reservation/list";
        } catch (Exception e) {
            log.error("[updateReservationProc] 예약 수정 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", " 서버 오류 발생. 다시 시도해주세요.");
            return "redirect:/manager/room/reservation/list";
        }
    }

    @Operation(summary = "예약 삭제", description = "관리자가 특정 예약을 삭제하는 API.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @PostMapping("/delete")
    public String deleteReservationProc(@RequestParam("reservationId") Integer reservationId, RedirectAttributes redirectAttributes) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));

            // 예약 상태 확인 (이미 취소된 예약은 다시 취소할 수 없음)
            if (!"예약".equals(reservation.getReservationStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 취소된 예약은 삭제할 수 없습니다.");
                return "redirect:/manager/room/reservation/list";
            }

            // '취소 완료'로 상태 변경하고 1분 후 자동 삭제되도록 설정
            reservation.setReservationStatus("취소 완료");
            reservation.setCancelConfirmedAt(LocalDateTime.now());
            reservationRepository.saveAndFlush(reservation);

            redirectAttributes.addFlashAttribute("successMessage", "예약이 강제 취소되었습니다. 1분 후 자동 삭제됩니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 삭제 중 오류 발생");
        }

        return "redirect:/manager/room/reservation/list";
    }

    @Operation(summary = "객실 강제 사용 중지", description = "관리자가 특정 객실을 강제로 사용 중지하는 API.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @PostMapping("/disableRoom")
    public String disableRoomProc(@RequestParam("roomId") Integer roomId) {
        roomService.disableRoom(roomId);
        return "redirect:/manager/room/reservation/list?success=disable";
    }

    @Operation(summary = "회원 정보 조회", description = "관리자가 특정 회원의 정보를 조회하는 API.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @GetMapping("/member/details")
    @ResponseBody
    public ResponseEntity<?> getMemberDetailsForm(@RequestParam("memberId") Integer memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "memberId", member.get().getMemberId(),
                    "memberName", member.get().getMemberName(),
                    "memberEmail", member.get().getMemberEmail()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
