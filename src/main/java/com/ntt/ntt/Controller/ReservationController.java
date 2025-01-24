package com.ntt.ntt.Controller;


import com.ntt.ntt.DTO.ReservationDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/room/reservation")
@RequiredArgsConstructor
@Log4j2
public class ReservationController {

    private final ReservationService reservationService;

    // 방 목록 가져오기
    @GetMapping("/list")
    public String reservationListPage(Model model) {
        // 필요한 데이터 설정 (예: 방 목록)
        model.addAttribute("rooms"/* 방 목록 데이터 */);
        return "manager/room/reservation/list"; // Thymeleaf 템플릿 경로
    }

    // 방 상세 정보 가져오기
    @GetMapping("/room/{roomId}")
    public RoomDTO getRoomDetails(@PathVariable Integer roomId) {
        return reservationService.getRoomDetails(roomId);
    }

    // 예약 추가
    @PostMapping("/register")
    public Integer registerReservation(@RequestBody ReservationDTO reservationDTO) {
        return reservationService.registerReservation(reservationDTO);
    }

    // 예약 수정
    @PutMapping("/update/{reservationId}")
    public void updateReservation(@PathVariable Integer reservationId,
                                  @RequestBody ReservationDTO reservationDTO) {
        reservationService.updateReservation(reservationId, reservationDTO);
    }

    // 예약 삭제
    @DeleteMapping("/delete/{reservationId}")
    public void deleteReservation(@PathVariable Integer reservationId) {
        reservationService.deleteReservation(reservationId);
    }

    // 방 상태 변경
    @PutMapping("/room/status/{roomId}")
    public void changeRoomStatus(@PathVariable Integer roomId, @RequestParam String status) {
        reservationService.changeRoomStatus(roomId, status);
    }
}
