package com.ntt.ntt.Controller.Room;


import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Service.hotel.HotelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
@Tag(name = "roomController", description = "유저 객실 관리 컨트롤러")
public class RoomController {

    private final RoomService roomService;

    // 2025-02-24 추천 호텔목록을 띄우기 위해 추가
    private final HotelService hotelService;


    /* -----------유저 페이지----------- */

    @GetMapping("/")
    public String mainPageForm(Model model) {
        // 추천 호텔 목록 가져오기
        List<HotelDTO> recommendedHotels = hotelService.listRecommendedHotels();

        // 로그로 확인
        log.info("Recommended hotels sent to index page: {}", recommendedHotels);

        // 모델에 추천 방 데이터를 추가
        model.addAttribute("recommendedHotels", recommendedHotels);

        return "index"; // index.html 반환
    }

    // 메인

    @GetMapping("/roomList") // 초기 Thymeleaf 화면 렌더링
    public String roomListPageForm(Model model) {
        Page<RoomDTO> initialRooms = roomService.getPaginatedRooms(PageRequest.of(0, 6));

        // Room 상태 업데이트를 로그로 확인 (디버깅용)
        initialRooms.getContent().forEach(room -> log.info("Room: {}, Status: {}", room.getRoomName(), room.getRoomStatus()));

        log.info("Room 데이터: {}", initialRooms.getContent());

        model.addAttribute("roomList", initialRooms.getContent());

        return "roomList"; // roomList.html 반환
    }

    // AJAX 요청 처리

    @GetMapping("/roomList/data")
    @ResponseBody
    public Map<String, Object> roomListDataForm(@RequestParam(value = "page", defaultValue = "0") int page) {

        // 한 번에 로드할 방 개수
        int pageSize = 3;

        // 음수 방지
        if (page < 0) {
            page = 0;
        }

        Page<RoomDTO> paginatedRooms = roomService.getPaginatedRooms(PageRequest.of(page, pageSize));

        // Room 데이터 확인 로그
        paginatedRooms.getContent().forEach(room -> log.info("Room: {}, Status: {}", room.getRoomName(), room.getRoomStatus()));

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("rooms", paginatedRooms.getContent());
        response.put("isLastPage", paginatedRooms.isLast());
        return response;
    }

    // 특정 객실 상세보기 페이지
    @GetMapping("/room/{roomId}")
    public String getRoomDetail(@PathVariable Integer roomId, Model model) {
        log.info("객실 상세보기 요청 - Room ID: {}", roomId);

        RoomDTO room = roomService.readRoom(roomId);
        if (room == null) {
            log.warn("객실을 찾을 수 없습니다. Room ID: {}", roomId);
            return "redirect:/roomList"; // 방이 없으면 리스트 페이지로 이동
        }

        model.addAttribute("room", room);
        return "detail"; // detail.html 반환
    }

}
