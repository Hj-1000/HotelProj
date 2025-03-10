package com.ntt.ntt.Controller.Room;


import com.ntt.ntt.DTO.BannerDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Service.BannerService;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Service.hotel.HotelService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
@Tag(name = "RoomController", description = "유저 객실 관리 컨트롤러")
public class RoomController {

    private final RoomService roomService;
    private final MemberService memberService;
    private final HotelService hotelService;
    private final BannerService bannerService;

    /* -----------유저 페이지----------- */

    @Operation(summary = "메인 페이지", description = "추천 호텔과 배너 목록을 가져와서 메인 페이지(index)를 반환한다.")
    @GetMapping("/")
    public String mainPageForm(Model model) {
        // 추천 호텔 목록 가져오기
        List<HotelDTO> recommendedHotels = hotelService.listRecommendedHotels();
        
        // 활성화된 배너 목록 가져오기
        List<BannerDTO> activeBanners = bannerService.getActiveBanners();

        // 로그로 확인
        log.info("Recommended hotels sent to index page: {}", recommendedHotels);
        log.info("Active banners sent to index page: {}", activeBanners);

        // 모델에 추천 방 데이터와 배너 데이터를 추가
        model.addAttribute("recommendedHotels", recommendedHotels);
        model.addAttribute("activeBanners", activeBanners);

        return "index"; // index.html 반환
    }

    @Operation(summary = "객실 목록 페이지", description = "객실 목록을 조회하여 roomList.html 페이지를 렌더링한다.")
    @GetMapping("/roomList") // 초기 Thymeleaf 화면 렌더링
    public String roomListPageForm(Model model) {
        Page<RoomDTO> initialRooms = roomService.getPaginatedRooms(PageRequest.of(0, 6));

        // Room 상태 업데이트를 로그로 확인 (디버깅용)
        initialRooms.getContent().forEach(room -> log.info("Room: {}, Status: {}", room.getRoomName(), room.getRoomStatus()));

        log.info("Room 데이터: {}", initialRooms.getContent());

        model.addAttribute("roomList", initialRooms.getContent());

        return "roomList"; // roomList.html 반환
    }

    @Operation(summary = "객실 목록 데이터 (AJAX)", description = "페이지 번호를 기반으로 객실 데이터를 페이징 처리하여 JSON 형태로 반환한다.")
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

    @Operation(summary = "객실 상세보기", description = "특정 객실의 상세 정보를 조회하여 detail.html 페이지를 렌더링한다.")
    @GetMapping("/room/{roomId}")
    public String getRoomDetail(@PathVariable Integer roomId, Model model, Principal principal) {
        log.info("객실 상세보기 요청 - Room ID: {}", roomId);

        try {
            RoomDTO room = roomService.readRoom(roomId);
            model.addAttribute("room", room);

            // 로그인한 사용자의 ID 가져오기
            if (principal != null) {
                MemberDTO member = memberService.findByEmail(principal.getName());
                model.addAttribute("memberId", member.getMemberId());
            }

            return "detail"; // detail.html 반환
        } catch (IllegalArgumentException e) {
            log.warn("객실을 찾을 수 없습니다. Room ID: {}", roomId);
            model.addAttribute("errorMessage", "요청하신 객실을 찾을 수 없습니다.");
            return "error/404"; // 404 페이지로 이동
        }
    }

}
