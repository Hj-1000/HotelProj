package com.ntt.ntt.Controller.reservation;

import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Service.ReservationService;
import com.ntt.ntt.Service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // 1. 객실 예약 등록
    @PostMapping("/register")
    public String registerReservationProc(@RequestParam("roomId") Integer roomId,
                                          @RequestParam("memberId") Integer memberId) {
        reservationService.registerReservation(roomId, memberId);
        return "redirect:/manager/room/reservation/list";
    }

    // 2. 방 목록 가져오기
    @GetMapping("/list")
    public String listReservationForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<RoomDTO> roomList = roomService.getRoomListWithReservations();
        List<ReservationDTO> reservationList = reservationService.getAllReservations();

        Map<Integer, ReservationDTO> reservationMap = new HashMap<>();
        for (ReservationDTO reservation : reservationList) {
            reservationMap.put(reservation.getRoomId(), reservation);
        }

        if (userDetails != null) {
            String email = userDetails.getUsername();
            Member member = memberRepository.findByMemberEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원 정보를 찾을 수 없습니다."));
            model.addAttribute("memberId", member.getMemberId());
        }

        model.addAttribute("roomList", roomList);
        model.addAttribute("reservationMap", reservationMap);
        model.addAttribute("reservationList", reservationList);

        return "manager/room/reservation/list";
    }

    // 3. 특정 방의 예약 정보 조회
    @GetMapping("/read")
    public String readReservationForm(@RequestParam("roomId") Integer roomId, Model model) {
        model.addAttribute("reservation", reservationService.getReservationByRoomId(roomId));

        List<RoomDTO> roomList = roomService.getRoomListWithReservations();
        List<ReservationDTO> reservationList = reservationService.getAllReservations();

        Map<Integer, ReservationDTO> reservationMap = new HashMap<>();
        for (ReservationDTO reservation : reservationList) {
            reservationMap.put(reservation.getRoomId(), reservation);
        }

        model.addAttribute("roomList", roomList);
        model.addAttribute("reservationMap", reservationMap);
        model.addAttribute("reservationList", reservationList);

        return "manager/room/reservation/list";
    }

    // 4. 예약 수정
    @PostMapping("/update")
    public String updateReservationProc(@RequestParam(value = "reservationId", required = false) Integer reservationId,
                                        @RequestParam(value = "roomId", required = true) Integer roomId,
                                        @RequestParam(value = "reservationEnd", required = false) String reservationEnd,
                                        @ModelAttribute ReservationDTO reservationDTO) {

        // 🚨 roomId가 없으면 400 에러 방지
        if (roomId == null) {
            throw new IllegalArgumentException("객실 ID(roomId)가 필요합니다.");
        }

        // ✅ 예약 ID가 없을 경우 예약 마감 날짜만 수정
        if (reservationId == null) {
            roomService.updateReservationEnd(roomId, reservationEnd);
            return "redirect:/manager/room/reservation/list";
        }

        reservationService.updateReservation(reservationId, reservationDTO, reservationDTO.getMemberId());
        return "redirect:/manager/room/reservation/list";
    }

    // 5. 예약 삭제
    @PostMapping("/delete")
    public String deleteReservationProc(@RequestParam("reservationId") Integer reservationId) {
        reservationService.deleteReservation(reservationId);
        return "redirect:/manager/room/reservation/list";
    }

    // 6. 방 강제 사용 중지
    @PostMapping("/disableRoom")
    public String disableRoomProc(@RequestParam("roomId") Integer roomId) {
        roomService.disableRoom(roomId);
        return "redirect:/manager/room/reservation/list";
    }

    // 7. 회원 정보 조회 API
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
