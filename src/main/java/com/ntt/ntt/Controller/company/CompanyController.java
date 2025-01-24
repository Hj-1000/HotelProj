package com.ntt.ntt.Controller.company;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.Service.company.CompanyService;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/company")
@AllArgsConstructor
@Log4j2
public class CompanyController {

    private final CompanyService companyService;
    private final PaginationUtil paginationUtil;
    private final HotelService hotelService;

    //등록폼
    @GetMapping("/register")
    public String registerForm() {
        return "/chief/company/register";
    }
    //등록처리
    @PostMapping("/register")
    public String registerProc(@ModelAttribute CompanyDTO companyDTO, List<MultipartFile> imageFiles, RedirectAttributes redirectAttributes) {
        log.info("본사 등록 진입");

        companyService.register(companyDTO, imageFiles);
        redirectAttributes.addFlashAttribute("message", "본사 등록이 완료되었습니다.");
        return "redirect:/company/list";
    }

    @GetMapping("/list")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String searchType,
                       @PageableDefault(page = 1) Pageable page,
                       Model model) {

        // 검색 기능을 포함한 서비스 호출
        Page<CompanyDTO> companyDTOS = companyService.list(page, keyword, searchType);

        // 페이지 정보 계산
        Map<String, Integer> pageInfo = paginationUtil.pagination(companyDTOS);

        // 전체 페이지 수
        int totalPages = companyDTOS.getTotalPages();

        // 현재 페이지 번호
        int currentPage = pageInfo.get("currentPage");

        // 시작 페이지와 끝 페이지 계산 (현재 페이지를 기준으로 최대 10페이지까지)
        int startPage = Math.max(1, currentPage - 4); // 10개씩 끊어서 시작 페이지 계산
        int endPage = Math.min(startPage + 9, totalPages); // 최대 10페이지까지, 전체 페이지 수를 넘지 않도록

        // 페이지 정보 업데이트
        pageInfo.put("startPage", startPage);
        pageInfo.put("endPage", endPage);

        // 모델에 데이터 추가
        model.addAttribute("companyDTOS", companyDTOS);
        model.addAttribute("pageInfo", pageInfo);

        // 검색어와 검색 타입을 폼에 전달할 수 있도록 추가
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);

        return "/chief/company/list";
    }


//    //읽기
//    @GetMapping("/read")
//    public String read(@RequestParam Integer companyId, Model model, RedirectAttributes redirectAttributes) {
//        try {
//            // 서비스에서 CompanyDTO 객체를 받아옴
//            CompanyDTO companyDTO = companyService.read(companyId);
//            // companyDTO를 모델에 추가
//            model.addAttribute("companyDTO", companyDTO);
//            // "read" 뷰로 이동
//            return "/chief/company/read";
//
//        } catch (NullPointerException e) {
//            redirectAttributes.addFlashAttribute("message", "해당 본사 정보를 찾을 수 없습니다.");
//            return "redirect:/company/list";  // 회사 정보가 없을 경우 목록으로 이동
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다.");
//            return "redirect:/company/list";  // 기타 예외 처리
//        }
//    }

    //본사의 지사 목록
    @GetMapping("/hotel/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam(required = false) Integer companyId,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String searchType,
                                                    @RequestParam(required = false) Integer keyword1, // 별점 검색용
                                                    @PageableDefault(page = 1, size = 9) Pageable page) {

        Page<HotelDTO> hotelDTOS;

        // companyId가 null이 아니면 해당 companyId에 맞는 호텔만 조회
        if (companyId != null) {
            hotelDTOS = hotelService.listByCompany(page, keyword, keyword1, searchType, companyId);
        } else {
            // companyId가 없으면 모든 호텔을 조회
            hotelDTOS = hotelService.listByCompany(page, keyword, keyword1, searchType, companyId);
        }

        System.out.println("호텔 목록 조회 결과: " + hotelDTOS.getContent()); // 확인용 로그 추가

        Map<String, Integer> pageInfo = paginationUtil.pagination(hotelDTOS);

        int totalPages = hotelDTOS.getTotalPages();
        int currentPage = pageInfo.get("currentPage");
        int startPage = Math.max(1, currentPage - 4);
        int endPage = Math.min(startPage + 9, totalPages);

        pageInfo.put("startPage", startPage);
        pageInfo.put("endPage", endPage);

        Map<String, Object> response = new HashMap<>();
        response.put("hotelDTOS", hotelDTOS.getContent());
        response.put("pageInfo", pageInfo);
        response.put("keyword", keyword);
        response.put("searchType", searchType);
        response.put("companyId", companyId);

        List<CompanyDTO> companyDTOS = hotelService.getAllCompany();
        response.put("companyDTOS", companyDTOS);

        return ResponseEntity.ok(response);
    }



    // 기존 read 메서드에 호텔 목록 추가
    @GetMapping("/read")
    public String read(@RequestParam Integer companyId, Model model, RedirectAttributes redirectAttributes,
                       @PageableDefault(page = 1, size = 9) Pageable pageable) {
        try {
            // 회사 정보 조회
            CompanyDTO companyDTO = companyService.read(companyId);
            model.addAttribute("companyDTO", companyDTO);

            // 해당 companyId에 맞는 호텔 목록 조회
            Page<HotelDTO> hotelDTOS = hotelService.listByCompany(pageable, null, null, null, companyId);
            model.addAttribute("hotelDTOS", hotelDTOS.getContent());

            // "read" 뷰로 이동
            return "/chief/company/read";

        } catch (NullPointerException e) {
            redirectAttributes.addFlashAttribute("message", "해당 본사 정보를 찾을 수 없습니다.");
            return "redirect:/company/list";  // 회사 정보가 없을 경우 목록으로 이동
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다.");
            return "redirect:/company/list";  // 기타 예외 처리
        }
    }





    //수정폼
    @GetMapping("/modify")
    public String modifyServiceHTML(Integer companyId, Model model) {
        CompanyDTO companyDTO = companyService.read(companyId);
        model.addAttribute("companyDTO", companyDTO);
        return "/chief/company/modify";
    }
    //수정처리
    @PostMapping("/modify")
    public String modifyService(CompanyDTO companyDTO, List<MultipartFile> newImageFiles, RedirectAttributes redirectAttributes) {
        companyService.update(companyDTO, newImageFiles);
        redirectAttributes.addFlashAttribute("message", "본사 수정이 완료되었습니다.");
        return "redirect:/company/list";
    }

    //삭제
    @GetMapping("/delete")
    public String delete(Integer companyId, RedirectAttributes redirectAttributes) {
        companyService.delete(companyId);
        redirectAttributes.addFlashAttribute("message", "해당 본사 삭제가 완료되었습니다.");
        return "redirect:/company/list";
    }


}
