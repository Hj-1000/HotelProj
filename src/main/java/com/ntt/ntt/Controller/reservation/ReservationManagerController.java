package com.ntt.ntt.Controller.reservation;


import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Service.ReservationService;
import com.ntt.ntt.Service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public String reservationListPageForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<RoomDTO> roomList = roomService.getRoomListWithReservations();
        List<ReservationDTO> reservationList = reservationService.getAllReservations();

        // reservationList를 roomId 기준으로 매핑
        Map<Integer, ReservationDTO> reservationMap = new HashMap<>();
        for (ReservationDTO reservation : reservationList) {
            if (reservation.getReservationEnd() == null || reservation.getReservationEnd().isEmpty()) {
                System.out.println(" reservationEnd가 NULL이거나 빈 값임! roomId: " + reservation.getRoomId());
            } else {
                System.out.println(" roomId: " + reservation.getRoomId() + " | reservationEnd: " + reservation.getReservationEnd());
            }

            reservationMap.put(reservation.getRoomId(), reservation);
        }

        // 현재 로그인한 사용자의 ID 가져오기 (UserDetails에서 이메일을 가져와서 memberId 조회)
        if (userDetails != null) {
            String email = userDetails.getUsername(); // 기본적으로 username은 email로 설정됨
            Member member = memberRepository.findByMemberEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원 정보를 찾을 수 없습니다."));
            model.addAttribute("memberId", member.getMemberId()); //  Model에 memberId 추가
        }

        model.addAttribute("roomList", roomList);
        model.addAttribute("reservationMap", reservationMap);

        return "manager/room/reservation/list";
    }

    // 3. 특정 방의 예약 정보 조회
    @GetMapping("/details")
    public String getReservationByRoomIdForm(@RequestParam("roomId") Integer roomId, Model model) {
        model.addAttribute("reservation", reservationService.getReservationByRoomId(roomId));
        return "manager/room/reservation/details";
    }

    // 4. 예약 수정
    @PostMapping("/update")
    public String updateReservationProc(@RequestParam("reservationId") Integer reservationId,
                                        @ModelAttribute ReservationDTO reservationDTO) {
        reservationService.updateReservation(reservationId, reservationDTO);
        return "redirect:/manager/room/reservation/list";
    }

    // 5. 예약 삭제 (GET 방식으로도 변경 가능)
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
}
