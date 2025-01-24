package com.ntt.ntt.Controller.hotel;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
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
    public String read(@RequestParam Integer hotelId, Model model, RedirectAttributes redirectAttributes) {
        try {
            HotelDTO hotelDTO = hotelService.read(hotelId);
            model.addAttribute("hotelDTO", hotelDTO);
            return "/hotel/read";

        } catch (NullPointerException e) {
            // Flash Attribute로 메시지를 전달
            redirectAttributes.addFlashAttribute("message", "해당 호텔이 없습니다!");
            return "redirect:/hotel/list"; // 목록 페이지로 리다이렉트
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/hotel/list"; // 목록 페이지로 리다이렉트
        }
    }



}
