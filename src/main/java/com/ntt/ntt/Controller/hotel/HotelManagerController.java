package com.ntt.ntt.Controller.hotel;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/manager/hotel")
@RequiredArgsConstructor
@Log4j2
public class HotelManagerController {

    private final HotelService hotelService;
    private final PaginationUtil paginationUtil;


    //등록폼
    @GetMapping("/register")
    public String registerForm(Model model) {
        //검증처리가 필요하면 빈 companyDTO를 생성해서 전달한다.
        List<CompanyDTO> companyDTOS = hotelService.getAllCompany();
        model.addAttribute("companyDTOS", companyDTOS);
        model.addAttribute("companyDTO", new CompanyDTO());
        return "/manager/hotel/register";
    }
    //등록처리
    @PostMapping("/register")
    public String registerProc(@ModelAttribute HotelDTO hotelDTO, List<MultipartFile> imageFiles, RedirectAttributes redirectAttributes) {
        log.info("본사 등록 진입");

        // 지사 등록 서비스 호출
        hotelService.register(hotelDTO, imageFiles);

        // 등록된 지사의 companyId 가져오기
        Integer companyId = hotelDTO.getCompanyId().getCompanyId();  // hotelDTO에 companyId가 포함되어 있다고 가정

        // 성공 메시지와 함께 companyId도 전달
        redirectAttributes.addFlashAttribute("message", "지사 등록이 완료되었습니다.");
        redirectAttributes.addFlashAttribute("companyId", companyId); // companyId 전달

        return "redirect:/manager/hotel/list?companyId=" + companyId;  // companyId를 쿼리 파라미터로 전달
    }



    //호텔본사관리자 전용
    @GetMapping("/list")
    public String list(@RequestParam(required = false) Integer companyId,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) Integer keyword1,  // 별점 검색용
                       @RequestParam(defaultValue = "1") int page, // 기본값을 1로 설정 (1-based)
                       @PageableDefault(size = 10) Pageable pageable, // 한 페이지에 10개씩
                       Model model, HttpServletRequest request,
                       RedirectAttributes redirectAttributes
                       ) {

        try {
            // 페이지 번호를 0-based로 변환 (page는 1-based로 들어오므로 1을 빼줌)
            Pageable adjustedPageable = PageRequest.of(page - 1, pageable.getPageSize());

            // companyId가 존재하는 경우 해당 회사의 지사들만 조회
            Page<HotelDTO> hotelDTOS;

            if (companyId != null) {

                // 요청 URL 로그 출력
                System.out.println("호텔리스트!! Requested URL: " + request.getRequestURL());
                // companyId 값 출력 (디버깅용)
                System.out.println("호텔리스트!! companyId: " + companyId);

                hotelDTOS = hotelService.listByCompany(adjustedPageable, keyword, keyword1, searchType, companyId);
            } else {
                hotelDTOS = hotelService.listByCompany(adjustedPageable, keyword, keyword1, searchType, companyId); // 기본적으로 모든 지사 조회
            }

            // 페이지 정보 계산
            Map<String, Integer> pageInfo = paginationUtil.pagination(hotelDTOS);

            // 전체 페이지 수
            int totalPages = hotelDTOS.getTotalPages();
            int currentPage = pageInfo.get("currentPage");

            // 시작 페이지와 끝 페이지 계산 (현재 페이지를 기준으로 최대 10페이지까지)
            int startPage = Math.max(1, currentPage - 4); // 최대 10개씩 출력
            int endPage = Math.min(startPage + 9, totalPages); // 전체 페이지 수를 넘지 않도록

            // prevPage, nextPage, lastPage 계산
            int prevPage = Math.max(1, currentPage - 1);
            int nextPage = Math.min(totalPages, currentPage + 1);
            int lastPage = totalPages;

            // 페이지 정보 업데이트
            pageInfo.put("startPage", startPage);
            pageInfo.put("endPage", endPage);
            pageInfo.put("prevPage", prevPage);
            pageInfo.put("nextPage", nextPage);
            pageInfo.put("lastPage", lastPage);


            // 모델에 데이터 추가
            model.addAttribute("hotelDTOS", hotelDTOS);
            model.addAttribute("pageInfo", pageInfo);

            // 검색어와 검색 타입을 폼에 전달할 수 있도록 추가
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchType", searchType);

            // companyId를 쿼리 파라미터로 다시 전달
            model.addAttribute("companyId", companyId);

            // 회사 목록 추가
            List<CompanyDTO> companyDTOS = hotelService.getAllCompany();
            model.addAttribute("companyDTOS", companyDTOS);
            model.addAttribute("companyDTO", new CompanyDTO());

            return "/manager/hotel/list"; // 뷰 경로
        } catch (NullPointerException e) {
            // Flash Attribute로 메시지를 전달
            redirectAttributes.addFlashAttribute("message", "해당 본사가 없습니다!");
            return "redirect:/company/list"; // 목록 페이지로 리다이렉트
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/company/list"; // 목록 페이지로 리다이렉트
        }
    }


    //읽기
    @GetMapping("/read")
    public String read(@RequestParam Integer hotelId, Model model, RedirectAttributes redirectAttributes) {
        try {
            HotelDTO hotelDTO = hotelService.read(hotelId);
            model.addAttribute("hotelDTO", hotelDTO);
            return "/manager/hotel/read";

        } catch (NullPointerException e) {
            // Flash Attribute로 메시지를 전달
            redirectAttributes.addFlashAttribute("message", "해당 지사가 없습니다!");
            return "redirect:/manager/hotel/list"; // 목록 페이지로 리다이렉트
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/manager/hotel/list"; // 목록 페이지로 리다이렉트
        }
    }


    //수정폼
    @GetMapping("/update")
    public String updateForm(Integer hotelId, Model model) {
        HotelDTO hotelDTO = hotelService.read(hotelId);
        List<CompanyDTO> companyDTOS = hotelService.getAllCompany();
        model.addAttribute("companyDTOS", companyDTOS);
        model.addAttribute("companyDTO", new CompanyDTO());
        model.addAttribute("hotelDTO", hotelDTO);
        return "/manager/hotel/update";
    }
    //수정처리
    @PostMapping("/update")
    public String updateProc(HotelDTO hotelDTO, List<MultipartFile> newImageFiles, RedirectAttributes redirectAttributes) {
        hotelService.update(hotelDTO, newImageFiles);
        redirectAttributes.addFlashAttribute("message", "지사 수정이 완료되었습니다.");
        return "redirect:/manager/hotel/list";
    }

    //삭제
    @GetMapping("/delete")
    public String delete(Integer hotelId, RedirectAttributes redirectAttributes) {
        hotelService.delete(hotelId);
        redirectAttributes.addFlashAttribute("message", "해당 지사 삭제가 완료되었습니다.");
        return "redirect:/manager/hotel/list";
    }


}
