package com.ntt.ntt.Controller.chief;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.DTO.RoomDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Service.ImageService;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chief/hotel")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "ChiefHotelController", description = "호텔장이 보는 호텔 지사 페이지")
public class ChiefHotelController {

    private final HotelService hotelService;

    private final MemberRepository memberRepository;

    private final PaginationUtil paginationUtil;
    private final ImageService imageService;


    //등록폼
    @Operation(summary = "호텔장 호텔 등록 폼", description = "호텔 등록 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerForm(Model model) {
        //검증처리가 필요하면 빈 companyDTO를 생성해서 전달한다.
        List<CompanyDTO> companyDTOS = hotelService.getMyCompany();
        model.addAttribute("companyDTOS", companyDTOS);
        model.addAttribute("companyDTO", new CompanyDTO());

        return "/manager/hotel/register";
    }
    //등록처리
    @Operation(summary = "호텔장 호텔 등록 처리", description = "호텔을 등록 처리 후 목록으로 이동한다.")
    @PostMapping("/register")
    public String registerProc(@ModelAttribute HotelDTO hotelDTO,
                               List<MultipartFile> imageFiles,
                               RedirectAttributes redirectAttributes,
                               Principal principal) {
        log.info("본사 등록 진입");

        // 현재 로그인한 사용자의 이메일 가져오기
        String userEmail = principal.getName();

        // 호텔 등록 서비스 호출 (memberEmail 전달)
        hotelService.register(hotelDTO, imageFiles, userEmail);

        // 등록된 지사의 companyId 가져오기
        Integer companyId = hotelDTO.getCompanyId();  // hotelDTO에 companyId가 포함되어 있다고 가정

        // 성공 메시지와 함께 companyId도 전달
        redirectAttributes.addFlashAttribute("message", "지사 등록이 완료되었습니다.");
        redirectAttributes.addFlashAttribute("companyId", companyId); // companyId 전달

//        return "redirect:/manager/hotel/list?companyId=" + companyId;  // companyId를 쿼리 파라미터로 전달
        return "redirect:/chief/hotel/list";  // 목록으로 이동
    }


    //호텔장 호텔 목록
    @Operation(summary = "호텔장 호텔 목록", description = "호텔장이 속한 본사의 호텔 목록을 조회한다.")
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) Integer keyword1,
                       @RequestParam(defaultValue = "1") int page,
                       @PageableDefault(size = 10) Pageable pageable,
                       Model model, HttpServletRequest request,
                       RedirectAttributes redirectAttributes) {
        try {
            // 현재 로그인한 사용자 ID 가져오기
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Member member = memberRepository.findByMemberEmail(email)
                    .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
            Integer memberId = member.getMemberId();

            Pageable adjustedPageable = PageRequest.of(page - 1, 10);  // 페이지 크기 명시적으로 지정
            Page<HotelDTO> hotelDTOS = hotelService.listByChief(adjustedPageable, keyword, keyword1, searchType, memberId);

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

            model.addAttribute("hotelDTOS", hotelDTOS);
            model.addAttribute("pageInfo", pageInfo);
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchType", searchType);

            List<CompanyDTO> companyDTOS = hotelService.getMyCompany();
            model.addAttribute("companyDTOS", companyDTOS);
            model.addAttribute("companyDTO", new CompanyDTO());

            return "/manager/hotel/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/chief/company/list";
        }
    }

    //읽기
    @Operation(summary = "호텔장 호텔 상세", description = "hotelId에 맞는 호텔 상세 페이지로 이동한다.")
    @GetMapping("/read")
    public String read(@RequestParam Integer hotelId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model, Pageable pageable,
                       RedirectAttributes redirectAttributes) {
        try {
            HotelDTO hotelDTO = hotelService.read(hotelId);
            if (hotelDTO == null) {
                redirectAttributes.addFlashAttribute("message", "해당 호텔이 존재하지 않습니다!");
                return "redirect:/chief/hotel/list";
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
            return "redirect:/chief/hotel/list";
        }
    }


    //수정폼
    @Operation(summary = "관리자용 호텔 수정폼", description = "hotelId에 맞는 호텔 수정폼 페이지로 이동한다.")
    @GetMapping("/update")
    public String updateForm(Integer hotelId, Model model) {
        HotelDTO hotelDTO = hotelService.read(hotelId);
        List<CompanyDTO> companyDTOS = hotelService.getMyCompany();
        model.addAttribute("companyDTOS", companyDTOS);
        model.addAttribute("companyDTO", new CompanyDTO());
        model.addAttribute("hotelDTO", hotelDTO);
        return "/manager/hotel/update";
    }
    //수정처리
    @Operation(summary = "관리자용 호텔 수정 처리", description = "hotelId에 맞는 호텔 수정 처리한 후 수정한 호텔 상세로 이동한다.")
    @PostMapping("/update")
    public String updateProc(HotelDTO hotelDTO, List<MultipartFile> newImageFiles, RedirectAttributes redirectAttributes) {
        hotelService.update(hotelDTO, newImageFiles);
        redirectAttributes.addFlashAttribute("message", "지사 수정이 완료되었습니다.");

        Integer hotelId = hotelDTO.getHotelId();
        redirectAttributes.addFlashAttribute("hotelId", hotelId);

        return "redirect:/chief/hotel/read?hotelId=" + hotelId;
    }

    // 이미지 삭제 (REST API 형식으로 처리)
    @RequestMapping(value = "/image/delete/{imageId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteImage(@PathVariable Integer imageId) {
        Map<String, String> response = new HashMap<>();
        try {
            // 서비스에서 이미지 삭제 호출
            imageService.deleteImage(imageId);

            // 삭제 성공 시 응답
            response.put("success", "true");
            response.put("message", "Image deleted successfully");
        } catch (EntityNotFoundException e) {
            // 이미지 없을 때 처리
            response.put("success", "false");
            response.put("message", "Image not found with id: " + imageId);
        } catch (Exception e) {
            // 기타 예외 처리
            response.put("success", "false");
            response.put("message", "Error deleting image");
        }
        return response;
    }




    //삭제
    @Operation(summary = "호텔장 호텔 삭제", description = "hotelId에 맞는 호텔을 삭제한다.")
    @GetMapping("/delete")
    public String delete(Integer hotelId, RedirectAttributes redirectAttributes) {

        // 삭제 전 호텔 정보 조회
        HotelDTO hotelDTO = hotelService.findById(hotelId);  // 호텔 정보를 먼저 가져옴

        if (hotelDTO == null) {
            redirectAttributes.addFlashAttribute("error", "해당 호텔을 찾을 수 없습니다.");
            return "redirect:/chief/hotel/list";
        }

        // 삭제 수행
        hotelService.delete(hotelId);

        // 삭제한 지사의 companyId 가져오기
        Integer companyId = hotelDTO.getCompanyId();

        redirectAttributes.addFlashAttribute("message", "해당 지사 삭제가 완료되었습니다.");

        return "redirect:/chief/hotel/list";  // companyId를 쿼리 파라미터로 전달
    }


}
