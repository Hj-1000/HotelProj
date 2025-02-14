package com.ntt.ntt.Controller.Room;


import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Repository.ReservationRepository;
import com.ntt.ntt.Service.RoomService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager/room")
@RequiredArgsConstructor
@Log4j2
public class RoomManagerController {

    private final RoomService roomService;
    private final ReservationRepository reservationRepository;


    /* -----------관리자 페이지----------- */

    // 1. Room 등록 페이지로 이동
    @GetMapping("/register")
    public String registerRoomForm(Model model) {
        //빈 RoomDTO 전달
        model.addAttribute("room", new RoomDTO());

        //hotelDTO hotelName 전달하기
        List<HotelDTO> hotelDTOS = roomService.getAllHotel();
        model.addAttribute("hotelDTOS", hotelDTOS);
        model.addAttribute("hotelDTO", new HotelDTO());

        //register.htm로 이동
        return "manager/room/register";
    }

    // Room 등록
    @PostMapping("/register")
    public String registerRoomProc(@ModelAttribute RoomDTO roomDTO, @RequestParam("imageFile") List<MultipartFile> imageFile) {

        log.info("Room registration data: {}", roomDTO); // 확인 로그 추가

        //Room 등록
        roomService.registerRoom(roomDTO, imageFile);

        //등록후 list 페이지로 이동하기
        return "redirect:/manager/room/list";
    }

    // 2. 모든 객실 조회
    @GetMapping("/list")
    public String listForm(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            Model model) {

        // 현재 페이지 정보를 Pageable에 반영
        Pageable updatedPageable = PageRequest.of(page, pageable.getPageSize());

        // 검색 조건과 페이징 정보를 이용하여 데이터 가져오기
        Page<RoomDTO> roomDTOS = roomService.searchRooms(keyword, category, updatedPageable);

        // 상태 자동 업데이트: 예약 마감일이 지난 경우 예약 불가 처리
        LocalDate today = LocalDate.now();
        roomDTOS.forEach(room -> {
            if (room.getReservationEnd() != null) {
                LocalDate reservationEndDate = LocalDate.parse(room.getReservationEnd());
                room.setRoomStatus(!reservationEndDate.isBefore(today));
            }
        });


        // 유효성 검사: 상태(category가 roomStatus일 경우)
        if ("roomStatus".equals(category)) {
            if (!"Available".equalsIgnoreCase(keyword) && !"Unavailable".equalsIgnoreCase(keyword)) {
                log.warn("Invalid room status keyword: {}", keyword);
                model.addAttribute("errorMessage", "유효하지 않은 텍스트입니다. 'Available' or 'Unavailable' 입력해주세요.");
                return "manager/room/list"; // 에러 메시지를 추가한 리스트 페이지로 리다이렉트
            }
        }

        // 로그로 이미지 확인
        for (RoomDTO room : roomDTOS) {
            if (room.getRoomImageDTOList() != null && !room.getRoomImageDTOList().isEmpty()) {
                log.info("Room ID: {} - 이미지 개수: {}", room.getRoomId(), room.getRoomImageDTOList().size());
            } else {
                log.info("Room ID: {} - 이미지 없음", room.getRoomId());
            }
        }

        // 페이지네이션 정보 생성
        Map<String, Integer> pageInfo = PaginationUtil.pagination(roomDTOS);

        // 전체 페이지 수
        int totalPages = roomDTOS.getTotalPages();

        // 현재 페이지 번호
        int currentPage = pageInfo.get("currentPage");

        // 시작 페이지와 끝 페이지 계산 (현재 페이지를 기준으로 최대 10페이지까지)
        int startPage = Math.max(1, currentPage - 4); // 10개씩 끊어서 시작 페이지 계산
        int endPage = Math.min(startPage + 9, totalPages); // 최대 10페이지까지, 전체 페이지 수를 넘지 않도록

        // 페이지 정보 업데이트
        pageInfo.put("startPage", startPage);
        pageInfo.put("endPage", endPage);


        // 모델에 데이터 추가
        model.addAttribute("currentPage", page);
        model.addAttribute("list", roomDTOS); // 페이징된 RoomDTO 리스트
        model.addAttribute("keyword", keyword); // 검색 키워드 전달
        model.addAttribute("category", category); // 검색 카테고리 전달
        model.addAllAttributes(pageInfo); // 페이지 정보 추가

        // 가격 포맷팅
        for (RoomDTO room : roomDTOS) {
            if (room.getFormattedRoomPrice() == null) {
                String formattedPrice = String.format("%,d", room.getRoomPrice());
                room.setFormattedRoomPrice(formattedPrice);
            }
        }

        return "manager/room/list";
    }

    // 특정 룸 조회
    @GetMapping("/{roomId}")
    public String getRoomDetailsForm(@PathVariable Integer roomId, Model model) {
        log.info("Fetching details for roomId: {}", roomId);

        RoomDTO room = roomService.readRoom(roomId);
        log.info("Fetched RoomDTO: {}", room);

        String formattedPrice = String.format("%,d KRW", room.getRoomPrice());
        model.addAttribute("room", room);
        model.addAttribute("formattedPrice", formattedPrice);

        return "manager/room/read";
    }

    // 3. Room 수정 페이지로 이동
    @GetMapping("/update/{roomId}")
    public String updateRoomForm(@PathVariable Integer roomId, Model model) {
        RoomDTO room = roomService.readRoom(roomId);

        // 방의 예약 여부를 `reserved` 필드로 설정
        room.setReserved(reservationRepository.existsByRoom_RoomId(roomId));

        model.addAttribute("room", room);

        return "manager/room/update";
    }

    // 4. Room 수정
    @PostMapping("/update/{roomId}")
    public String updateRoomProc(@PathVariable Integer roomId,
                                 @ModelAttribute RoomDTO roomDTO,
                                 @RequestParam("imageFile") List<MultipartFile> imageFile,
                                 RedirectAttributes redirectAttributes) {

        log.info("Updating Room with ID: {}", roomId);

        // 객실 상태 자동 변경 (예약 마감일 기준)
        LocalDate today = LocalDate.now();
        LocalDate newReservationEnd = roomDTO.getReservationEnd() != null ? LocalDate.parse(roomDTO.getReservationEnd()) : null;

        if (newReservationEnd != null && newReservationEnd.isBefore(today)) {
            roomDTO.setRoomStatus(false); // 예약 불가
        } else {
            roomDTO.setRoomStatus(true); // 예약 가능
        }

        roomService.updateRoom(roomId, roomDTO, imageFile);

        //
        redirectAttributes.addFlashAttribute("successMessage", "객실 정보가 성공적으로 수정되었습니다.");

        return "redirect:/manager/room/list";
    }




    // 5. Room 삭제
    @GetMapping("/delete/{roomId}")
    public String deleteRoomForm(@PathVariable Integer roomId, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(roomId);
            redirectAttributes.addFlashAttribute("successMessage", "삭제가 완료되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "객실 삭제 실패: " + e.getMessage());
        }
        return "redirect:/manager/room/list";
    }

}
