package com.ntt.ntt.Controller.Room;


import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class RoomController {

    private final RoomService roomService;


    /* -----------유저 페이지----------- */

    @GetMapping("/")
    public String mainPageForm(Model model) {
        // 추천 방 목록 가져오기
        List<RoomDTO> recommendedRooms = roomService.listRecommendedRooms();

        // 로그로 확인
        log.info("Recommended rooms sent to index page: {}", recommendedRooms);

        // 모델에 추천 방 데이터를 추가
        model.addAttribute("recommendedRooms", recommendedRooms);

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
        int pageSize = 3; // 한 번에 로드할 방 개수

        if (page < 0) {
            page = 0; // 음수 방지
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




}
