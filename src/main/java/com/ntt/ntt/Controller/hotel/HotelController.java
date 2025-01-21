package com.ntt.ntt.Controller.hotel;

import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/hotel")
@RequiredArgsConstructor
@Log4j2
public class HotelController {

    private final HotelService hotelService;
    private final PaginationUtil paginationUtil;


    //등록폼
    @GetMapping("/register")
    public String registerForm() {
        return "/hotel/register";
    }
    //등록처리
    @PostMapping("/register")
    public String registerProc(@ModelAttribute HotelDTO hotelDTO, List<MultipartFile> imageFiles) {
        log.info("본사 등록 진입");

        hotelService.register(hotelDTO, imageFiles);

        return "redirect:/hotel/list";
    }

    @GetMapping("/list")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer keyword1,
                       @RequestParam(required = false) String searchType,
                       @PageableDefault(page = 1) Pageable page,
                       Model model) {

        // 검색 기능을 포함한 서비스 호출
        Page<HotelDTO> hotelDTOS = hotelService.list(page, keyword, keyword1, searchType);

        // 페이지 정보 계산
        Map<String, Integer> pageInfo = paginationUtil.pagination(hotelDTOS);

        // 만약 글이 10개 이하라면, 페이지 2는 표시되지 않도록 수정
        if (hotelDTOS.getTotalPages() <= 1) {
            pageInfo.put("startPage", 1);
            pageInfo.put("endPage", 1);
        }

        // 모델에 데이터 추가
        model.addAttribute("hotelDTOS", hotelDTOS);
        model.addAttribute("pageInfo", pageInfo);

        // 검색어와 검색 타입을 폼에 전달할 수 있도록 추가
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);

        return "/hotel/list";
    }

    //읽기
    @GetMapping("/read")
    public String read(@RequestParam Integer hotelId, Model model) {
        try {
            // 서비스에서 hotelDTO 객체를 받아옴
            HotelDTO hotelDTO = hotelService.read(hotelId);

            // hotelDTO를 모델에 추가
            model.addAttribute("hotelDTO", hotelDTO);

            // "read" 뷰로 이동
            return "/hotel/read";

        } catch (EntityNotFoundException e) {
            model.addAttribute("error", "해당 회사 정보를 찾을 수 없습니다.");
            return "/hotel/list";  // 회사 정보가 없을 경우 목록으로 이동
        } catch (Exception e) {
            model.addAttribute("error", "서버 오류가 발생했습니다.");
            return "/hotel/list";  // 기타 예외 처리
        }
    }




}
