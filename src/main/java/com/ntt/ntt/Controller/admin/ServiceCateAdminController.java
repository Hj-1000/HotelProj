package com.ntt.ntt.Controller.admin;

import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.ServiceCateDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.ServiceCateService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("admin/roomService/category") //url roomService아래에
@Tag(name = "ServiceCateAdminController", description = "어드민이 보는 룸서비스 카테고리 정보")
public class ServiceCateAdminController {
    private final ServiceCateService serviceCateService;
    private final MemberService memberService;
    private final PaginationUtil paginationUtil;

    @Operation(summary = "등록폼", description = "등록폼 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerForm(@RequestParam(required = false) Integer hotelId, Model model){
        //검증처리가 필요하면 빈 CateDTO를 생성해서 전달한다.


        model.addAttribute("serviceCateDTO", new ServiceCateDTO());

        //hotelDTO hotelName 전달하기
        List<HotelDTO> hotelDTOS = serviceCateService.getAdminListByHotel();
        model.addAttribute("selectedHotelId", hotelId);
        model.addAttribute("hotelDTOS", hotelDTOS);
        model.addAttribute("hotelDTO", new HotelDTO());

        return "/manager/roomService/category/register";
    }

    @Operation(summary = "등록창", description = "데이터 등록 후 목록페이지로 이동한다.")
    @PostMapping("/register")
    public String registerProc(ServiceCateDTO serviceCateDTO,
                               @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        log.info("post에서 등록할 serviceCateDTO" + serviceCateDTO);
        serviceCateService.register(serviceCateDTO, imageFiles);

        // 등록된 지사의 hotelId 가져오기
        Integer hotelId = serviceCateDTO.getHotelId().getHotelId();  // 2025-02-11 추가

        redirectAttributes.addFlashAttribute("message", "카테고리 등록이 완료되었습니다.");
        return "redirect:/admin/roomService/category/list";
        // hotelId를 쿼리 파라미터로 전달 2025-02-11 추가
    }

    @Operation(summary = "어드민의 카테고리 목록", description = "등록된 모든 카테고리 목록을 조회한다.")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHIEF', 'MANAGER')")
    @GetMapping("/list")
    public String listSearch(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String searchType,
                             @RequestParam(required = false) Integer hotelId, // hotelId 2024-02-11 추가
                             @PageableDefault(page = 1) Pageable page,
                             HttpSession session,
                             Model model) {


        // 검색 기능을 포함한 서비스 호출
        Page<ServiceCateDTO> serviceCateDTOS = serviceCateService.listByAdmin(page, keyword, searchType, hotelId);

        // 페이지 정보 계산 (PaginationUtil 사용)
        Map<String, Integer> pageInfo = paginationUtil.pagination(serviceCateDTOS);

        // 전체 페이지 수
        int totalPages = serviceCateDTOS.getTotalPages();
        // 현재 페이지 수
        int currentPage = pageInfo.get("currentPage");

        // 시작 페이지와 끝 페이지 계산 (현재 페이지를 기준으로 최대 10페이지까지)
        int startPage = Math.max(1, currentPage - 4); // 10개씩 끊어서 시작 페이지 계산
        int endPage = Math.min(startPage + 9, totalPages); // 최대 페이지 수를 넘기지 않도록

        // hotelDTO hotelName 전달하기
        List<HotelDTO> hotelDTOS = serviceCateService.getAdminListByHotel();

        // 페이지 정보 업데이트 (동적으로 계산된 startPage, endPage)
        pageInfo.put("startPage", startPage);
        pageInfo.put("endPage", endPage);

        // 모델에 데이터 추가
        model.addAttribute("serviceCateDTOS", serviceCateDTOS);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("hotelId", hotelId); // 2024-02-11 추가
        model.addAttribute("hotelDTOS", hotelDTOS);
        model.addAttribute("hotelDTO", new HotelDTO());
        model.addAttribute("selectedHotelId", hotelId);

        return "/manager/roomService/category/list";
    }


    @Operation(summary = "개별조회", description = "해당번호의 데이터를 조회한다.")
    @GetMapping("/read")
    public String read(Integer serviceCateId, Model model) {
        try {
            ServiceCateDTO serviceCateDTO =
                    serviceCateService.read(serviceCateId);
            model.addAttribute("serviceCateDTO", serviceCateDTO);
            return "/manager/roomService/category/read";
        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "해당 카테고리를 찾을 수 없습니다");
            return "/manager/roomService/category/list";
        } catch (Exception e) {
            model.addAttribute("error", "서버 오류가 발생했습니다.");
            return "/manager/roomService/category/list";
        }
    }
    @Operation(summary = "수정폼", description = "해당 데이터를 조회 후 수정폼페이지로 이동한다.")
    @GetMapping("/update")
    public String updateForm(Integer serviceCateId, Model model) {
        ServiceCateDTO serviceCateDTO =
                serviceCateService.read(serviceCateId);
        model.addAttribute("serviceCateDTO",serviceCateDTO);


        return "/manager/roomService/category/update";
    }

    @Operation(summary = "수정창", description = "수정할 내용을 데이터베이스에 저장 후 목록페이지로 이동한다.")
    @PostMapping("/update")
    public String updateProc(ServiceCateDTO serviceCateDTO, @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                             RedirectAttributes redirectAttributes) {
        serviceCateService.update(serviceCateDTO, imageFiles);
        redirectAttributes.addFlashAttribute("message", "카테고리 수정이 완료되었습니다.");
        return "redirect:/admin/roomService/category/list";
    }

    @Operation(summary = "삭제처리", description = "해당 데이터를 삭제 후 목록페이지로 이동한다.")
    @GetMapping("/delete")
    public String deleteForm(Integer serviceCateId, RedirectAttributes redirectAttributes) {
        //삭제 전 카테고리 정보 조회
        ServiceCateDTO serviceCateDTO = serviceCateService.read(serviceCateId);

        serviceCateService.delete(serviceCateId);
        redirectAttributes.addFlashAttribute("message", "카테고리 삭제가 완료되었습니다.");
        Integer hotelId = serviceCateDTO.getHotelId().getHotelId();

        return "redirect:/admin/roomService/category/list";
    }
}
