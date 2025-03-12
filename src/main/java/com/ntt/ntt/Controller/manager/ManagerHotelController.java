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
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            HotelDTO hotelDTO = hotelService.read(hotelId);
            if (hotelDTO == null) {
                redirectAttributes.addFlashAttribute("message", "해당 호텔이 존재하지 않습니다!");
                return "redirect:/manager/hotel/list";
            }

            // Pageable 객체 생성
            Pageable pageable = PageRequest.of(page, 10);  // 10개씩 표시

            // 호텔 ID에 맞는 방 목록을 페이징 처리하여 가져옵니다.
            Page<RoomDTO> roomsForHotel = hotelService.roomListByHotel(hotelId, pageable);

            // 방 목록에 관련된 이미지와 가격 포맷팅 처리
            for (RoomDTO room : roomsForHotel) {
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

            model.addAttribute("hotelDTO", hotelDTO);
            model.addAttribute("rooms", roomsForHotel.getContent());  // 현재 페이지의 객실 목록
            model.addAttribute("totalPages", roomsForHotel.getTotalPages());  // 총 페이지 수
            model.addAttribute("currentPage", page);  // 현재 페이지

            return "/manager/hotel/read";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다!");
            return "redirect:/manager/hotel/list";
        }
    }



}
