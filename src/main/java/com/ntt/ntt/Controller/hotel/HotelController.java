package com.ntt.ntt.Controller.hotel;

import com.ntt.ntt.DTO.*;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Repository.hotel.HotelRepository;
import com.ntt.ntt.Service.RoomReviewService;
import com.ntt.ntt.Service.RoomService;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/hotel")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "HotelController", description = "유저가 보는 호텔 페이지")
public class HotelController {

    private final HotelService hotelService;
    private final RoomReviewService roomReviewService;
    private final PaginationUtil paginationUtil;
    private final RoomService roomService;

    //호텔목록
    @Operation(summary = "사용자용 호텔 목록", description = "전체 호텔 목록 페이지로 이동한다.")
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
    @Operation(summary = "사용자용 호텔 상세", description = "hotelId에 맞는 호텔 목록 페이지로 이동한다.")
    @GetMapping("/read")
    public String read(@RequestParam Integer hotelId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            // 호텔 정보 조회
            HotelDTO hotelDTO = hotelService.read(hotelId);
            if (hotelDTO == null) {
                redirectAttributes.addFlashAttribute("message", "해당 호텔이 존재하지 않습니다!");
                return "redirect:/hotel/list";
            }

            // 최신 리뷰 조회(3개)
            List<RoomReviewDTO> latestReviews = roomReviewService.getLatestReviewsByHotelId(hotelId);

            // 호텔 ID에 맞는 방 목록을 페이징 처리하여 가져옵니다.
            List<RoomDTO> roomsForHotel = hotelService.getRoomsByHotel(hotelId);

            // 상태 자동 업데이트: 예약 마감일이 지난 경우 예약 불가 처리
            LocalDate today = LocalDate.now();
            roomsForHotel.forEach(room -> {
                if (room.getReservationEnd() != null) {
                    LocalDate reservationEndDate = LocalDate.parse(room.getReservationEnd());
                    room.setRoomStatus(!reservationEndDate.isBefore(today));
                }
            });

            // 방 목록에 관련된 이미지와 가격 포맷팅 처리
            for (RoomDTO room : roomsForHotel) {
                if (room.getRoomImageDTOList() != null) {
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

            model.addAttribute("rooms", roomsForHotel);  // 현재 페이지의 객실 목록

            // 모델에 데이터 추가
            model.addAttribute("hotelDTO", hotelDTO);
            model.addAttribute("currentPage", page);  // 현재 페이지
            model.addAttribute("latestReviews", latestReviews); // 최신 리뷰 3개 추가

            return "/hotel/read";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다!");
            return "redirect:/hotel/list";
        }
    }

    // 해당 호텔 전체 객실

    /*@GetMapping("/read/rooms")
    @ResponseBody
    public Page<RoomDTO> getRoomsByHotel(
            @RequestParam Integer hotelId,
            @RequestParam int page,
            @RequestParam int size) {

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "roomId"));

        // 호텔 서비스에서 방 데이터 가져오기
        return hotelService.getRoomsByHotel(hotelId, pageable);
    }*/

    // 해당 호텔 전체 리뷰
    @Operation(summary = "사용자용 호텔 리뷰목록", description = "hotelId에 맞는 호텔 리뷰 목록 페이지로 이동한다.")
    @GetMapping("/reviewAll")
    public String reviewList(@RequestParam Integer hotelId, Model model,
                             RedirectAttributes redirectAttributes) {

        // 호텔 정보 조회
        HotelDTO hotelDTO = hotelService.read(hotelId);
        if (hotelDTO == null) {
            redirectAttributes.addFlashAttribute("message", "해당 호텔이 존재하지 않습니다!");
            return "redirect:/hotel/list";
        }
        model.addAttribute("hotelDTO", hotelDTO);

        return "/hotel/reviewList";
    }

}
