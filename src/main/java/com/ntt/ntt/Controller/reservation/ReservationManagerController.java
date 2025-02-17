package com.ntt.ntt.Controller.reservation;

import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Service.ReservationService;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/manager/room/reservation")
@RequiredArgsConstructor
@Log4j2
public class ReservationManagerController {

    private final ReservationService reservationService;
    private final RoomService roomService;
    private final MemberRepository memberRepository;

    private final PaginationUtil paginationUtil;

    // 1. 객실 예약 등록
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @PostMapping("/register")
    public String registerReservationProc(@RequestParam("roomId") Integer roomId,
                                          @RequestParam("memberId") Integer memberId,
                                          @RequestParam("checkInDate") String checkInDate,
                                          @RequestParam("checkOutDate") String checkOutDate,
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

    // 2. 방 목록 가져오기
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @GetMapping("/list")
    public String listReservationForm(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, page = 0) Pageable pageable,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword) {

        log.info("검색 요청 - category: " + category + ", keyword: " + keyword);

        // 객실 목록 (페이징 없이 전체 가져오기)
        List<RoomDTO> allRoomList = roomService.getRoomListWithReservations();

        // 예약된 방 검색
        Page<ReservationDTO> reservationPage;
        if (category != null && keyword != null && !keyword.trim().isEmpty() &&
                (category.equals("roomName") || category.equals("memberId") || category.equals("memberName"))) {
            reservationPage = reservationService.searchReservations(category, keyword, pageable);
        } else {
            reservationPage = reservationService.getAllReservations(pageable);
        }

        // 모든 방 검색 (객실 이름 + 상태 검색 가능)
        Page<RoomDTO> roomPage;
        if ("roomName".equals(category) || "status".equals(category)) {
            roomPage = roomService.searchAllRooms(keyword, category, pageable);
        } else {
            roomPage = roomService.getPaginatedRooms(pageable);
        }

        // 예약된 방 매핑
        List<ReservationDTO> allReservations = reservationService.getAllReservations(Pageable.unpaged()).getContent();
        Map<Integer, ReservationDTO> reservationMap = new HashMap<>();
        for (ReservationDTO reservation : allReservations) {
            reservationMap.put(reservation.getRoomId(), reservation);
        }

        // 로그인한 유저 정보 추가
        if (userDetails != null) {
            String email = userDetails.getUsername();
            Member member = memberRepository.findByMemberEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원 정보를 찾을 수 없습니다."));
            model.addAttribute("memberId", member.getMemberId());
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

        return "manager/room/reservation/list";
    }

    // 3. 특정 방의 예약 정보 조회
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @GetMapping("/read")
    public String readReservationForm(@RequestParam("roomId") Integer roomId, Model model) {
        model.addAttribute("reservation", reservationService.getReservationByRoomId(roomId));

        List<RoomDTO> roomList = roomService.getRoomListWithReservations();
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

    // 4. 예약 수정
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @PostMapping("/update")
    public String updateReservationProc(@RequestParam(value = "reservationId", required = false) Integer reservationId,
                                        @RequestParam(value = "roomId", required = true) Integer roomId,
                                        @RequestParam(value = "reservationEnd", required = false) String reservationEnd,
                                        @ModelAttribute ReservationDTO reservationDTO) {

        // roomId가 없으면 400 에러 방지
        if (roomId == null) {
            throw new IllegalArgumentException("객실 ID(roomId)가 필요합니다.");
        }

        // 예약 ID가 없을 경우, 예약 마감 날짜만 수정
        if (reservationId == null) {
            roomService.updateReservationEnd(roomId, reservationEnd);
            roomService.updateRoomStatusBasedOnReservationEnd(roomId);
            return "redirect:/manager/room/reservation/list";
        }

        reservationService.updateReservation(reservationId, reservationDTO, reservationDTO.getMemberId());

        // 예약 정보 수정 후 객실 상태 자동 업데이트
        roomService.updateRoomStatusBasedOnReservationEnd(roomId);

        return "redirect:/manager/room/reservation/list?success=update";
    }

    // 5. 예약 삭제
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @PostMapping("/delete")
    public String deleteReservationProc(@RequestParam("reservationId") Integer reservationId) {
        reservationService.deleteReservation(reservationId);
        return "redirect:/manager/room/reservation/list";
    }

    // 6. 방 강제 사용 중지
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @PostMapping("/disableRoom")
    public String disableRoomProc(@RequestParam("roomId") Integer roomId) {
        roomService.disableRoom(roomId);
        return "redirect:/manager/room/reservation/list?success=disable";
    }

    // 7. 회원 정보 조회 API
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @GetMapping("/member/details")
    @ResponseBody
    public ResponseEntity<?> getMemberDetails(@RequestParam("memberId") Integer memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "memberName", member.get().getMemberName(),
                    "memberEmail", member.get().getMemberEmail()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
