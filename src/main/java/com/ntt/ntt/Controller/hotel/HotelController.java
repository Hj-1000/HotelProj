package com.ntt.ntt.Controller.hotel;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/hotel")
@RequiredArgsConstructor
@Log4j2
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final PaginationUtil paginationUtil;


    //호텔목록
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(defaultValue = "1") int page, // 1-based 페이지 파라미터
                       @PageableDefault(size = 9) Pageable pageable, // Pageable 기본값
                       Model model) {

        // 페이지 번호를 0-based로 변환
        Pageable adjustedPageable = PageRequest.of(page - 1, pageable.getPageSize());

        // hotelDTOS: 호텔 목록
        Page<HotelDTO> hotelDTOS;

        // 'location' 검색일 경우 정확히 일치하는 값만 검색
        if ("location".equals(searchType)) {
            hotelDTOS = hotelService.list(adjustedPageable, keyword, searchType, true);
        } else {
            hotelDTOS = hotelService.list(adjustedPageable, keyword, searchType, false);
        }

        // 페이지 정보 계산
        Map<String, Integer> pageInfo = paginationUtil.pagination(hotelDTOS);

        // 전체 페이지 수
        int totalPages = hotelDTOS.getTotalPages();
        int currentPage = pageInfo.get("currentPage");

        // 페이지 정보 업데이트
        int startPage = Math.max(1, currentPage - 4); // 10개씩 끊어서 시작 페이지 계산
        int endPage = Math.min(startPage + 9, totalPages); // 최대 10페이지까지, 전체 페이지 수를 넘지 않도록

        // prevPage, nextPage, lastPage 계산
        int prevPage = Math.max(1, currentPage - 1);
        int nextPage = Math.min(totalPages, currentPage + 1);
        int lastPage = totalPages;

        // 페이지 정보에 추가
        pageInfo.put("startPage", startPage);
        pageInfo.put("endPage", endPage);
        pageInfo.put("prevPage", prevPage);
        pageInfo.put("nextPage", nextPage);
        pageInfo.put("lastPage", lastPage);

        // 모델에 데이터 추가
        model.addAttribute("hotelDTOS", hotelDTOS);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);

        return "/hotel/list";
    }


    //읽기
    @GetMapping("/read")
    public String read(@RequestParam Integer hotelId,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "category", required = false) String category,
                       @PageableDefault(size = 5, page = 0) Pageable pageable,
                       Model model, RedirectAttributes redirectAttributes) {
        try {
            // 호텔 정보 조회
            HotelDTO hotelDTO = hotelService.read(hotelId);

            // 방 목록 조회
            Page<RoomDTO> roomDTOS = roomService.searchRooms(keyword, category, pageable);

            // 방 목록에 관련된 이미지와 가격 포맷팅 처리
            for (RoomDTO room : roomDTOS) {
                if (room.getRoomImageDTOList() != null && !room.getRoomImageDTOList().isEmpty()) {
                    log.info("Room ID: {} - 이미지 개수: {}", room.getRoomId(), room.getRoomImageDTOList().size());
                } else {
                    log.info("Room ID: {} - 이미지 없음", room.getRoomId());
                }
                // 가격 포맷팅
                if (room.getFormattedRoomPrice() == null) {
                    String formattedPrice = String.format("%,d", room.getRoomPrice());
                    room.setFormattedRoomPrice(formattedPrice);
                }
            }

            // 페이지네이션 정보 생성
            Map<String, Integer> pageInfo = PaginationUtil.pagination(roomDTOS);
            model.addAllAttributes(pageInfo); // 페이지 정보 추가

            model.addAttribute("hotelDTO", hotelDTO);

            // 모델에 방 목록 추가
            model.addAttribute("roomDTOS", roomDTOS);
            model.addAttribute("keyword", keyword); // 검색 키워드 전달
            model.addAttribute("category", category); // 검색 카테고리 전달

            return "/hotel/read"; // 호텔 상세 보기 페이지로 이동

        } catch (NullPointerException e) {
            // 호텔이 없는 경우 처리
            redirectAttributes.addFlashAttribute("message", "해당 호텔이 없습니다!");
            return "redirect:/hotel/list"; // 목록 페이지로 리다이렉트
        } catch (Exception e) {
            // 서버 오류 처리
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/hotel/list"; // 목록 페이지로 리다이렉트
        }
    }



}
