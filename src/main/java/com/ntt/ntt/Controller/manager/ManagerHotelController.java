package com.ntt.ntt.Controller.manager;

import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/manager/hotel")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "ManagerHotelController", description = "매니저가 보는 호텔 지사 페이지")
public class ManagerHotelController {

    private final HotelService hotelService;
    private final MemberRepository memberRepository;
    private final PaginationUtil paginationUtil;

    //목록

    @GetMapping("/list")
    public String listHotels(Pageable pageable, Model model) {
        try {
            // 현재 로그인한 사용자 ID 가져오기
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Member member = memberRepository.findByMemberEmail(email)
                    .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

            Integer memberId = member.getMemberId();

            // 호텔 목록을 검색 조건에 맞게 가져오기
            Page<HotelDTO> hotelDTOS = hotelService.listByManager(memberId, pageable);

            // 호텔 목록을 모델에 추가하여 뷰로 전달
            model.addAttribute("hotelDTOS", hotelDTOS);

            Map<String, Integer> pageInfo = paginationUtil.pagination(hotelDTOS);

            int totalPages = hotelDTOS.getTotalPages();
            int currentPage = pageInfo.get("currentPage");
            int startPage = Math.max(1, currentPage - 4);
            int endPage = Math.min(startPage + 9, totalPages);
            int prevPage = Math.max(1, currentPage - 1);
            int nextPage = Math.min(totalPages, currentPage + 1);
            int lastPage = totalPages;

            pageInfo.put("startPage", startPage);
            pageInfo.put("endPage", endPage);
            pageInfo.put("prevPage", prevPage);
            pageInfo.put("nextPage", nextPage);
            pageInfo.put("lastPage", lastPage);

            model.addAttribute("pageInfo", pageInfo);

            return "/manager/hotel/list"; // 호텔 목록을 표시할 뷰 이름
        } catch (Exception e) {
            model.addAttribute("message", "서버 오류가 발생했습니다.");
            return "admin/hotel/list"; // 오류 발생 시에도 호텔 목록 화면으로 돌아감
        }
    }



    //읽기
    @Operation(summary = "매니저 호텔 상세", description = "hotelId에 맞는 호텔 상세 페이지로 이동한다.")
    @GetMapping("/read")
    public String read(@RequestParam Integer hotelId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model, Pageable pageable,
                       RedirectAttributes redirectAttributes) {
        try {
            HotelDTO hotelDTO = hotelService.read(hotelId);
            if (hotelDTO == null) {
                redirectAttributes.addFlashAttribute("message", "해당 호텔이 존재하지 않습니다!");
                return "redirect:/manager/hotel/list";
            }

            //객실 가져오기
            // 현재 페이지 정보를 Pageable에 반영
            Pageable updatedPageable = PageRequest.of(page, pageable.getPageSize());

            // 검색 조건과 페이징 정보를 이용하여 데이터 가져오기
            Page<RoomDTO> roomDTOS;

            try {
                // 2. 검색 수행
                roomDTOS = hotelService.roomListByHotel(hotelId, updatedPageable);
            } catch (IllegalArgumentException e) {
                log.warn(" 검색 중 오류 발생: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", "검색 조건이 올바르지 않습니다.");
                return "redirect:/manager/room/list";
            }

            // 상태 자동 업데이트: 예약 마감일이 지난 경우 예약 불가 처리
            LocalDate today = LocalDate.now();
            roomDTOS.forEach(room -> {
                if (room.getReservationEnd() != null) {
                    LocalDate reservationEndDate = LocalDate.parse(room.getReservationEnd());
                    room.setRoomStatus(!reservationEndDate.isBefore(today));
                }
            });

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
            model.addAllAttributes(pageInfo); // 페이지 정보 추가

            // 가격 포맷팅
            for (RoomDTO room : roomDTOS) {
                if (room.getFormattedRoomPrice() == null) {
                    String formattedPrice = String.format("%,d", room.getRoomPrice());
                    room.setFormattedRoomPrice(formattedPrice);
                }
            }

            model.addAttribute("hotelDTO", hotelDTO);

            return "/manager/hotel/read";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다!");
            return "redirect:/manager/hotel/list";
        }
    }



}
