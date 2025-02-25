package com.ntt.ntt.Controller.company;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Service.ImageService;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.company.CompanyService;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chief/company")
@AllArgsConstructor
@Log4j2
public class CompanyController {

    private final CompanyService companyService;
    private final PaginationUtil paginationUtil;
    private final ImageService imageService;
    private final MemberService memberService;

    //등록폼
    @Operation(summary = "관리자용 본사 등록 폼", description = "본사 등록 폼 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerForm() {
        return "/chief/company/register";
    }
    //등록처리
    @Operation(summary = "관리자용 본사 등록 처리", description = "본사를 등록 처리 한다.")
    @PostMapping("/register")
    public String registerProc(@Valid @ModelAttribute CompanyDTO companyDTO,
                               List<MultipartFile> imageFiles,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication,
                               BindingResult result) {
        log.info("본사 등록 진입");

        if (result.hasErrors()) {
            return "/chief/company/register";  // 입력 오류 시 다시 폼으로
        }

        // Authentication 객체에서 UserDetails를 가져와 이름을 추출
        String memberName = ((UserDetails) authentication.getPrincipal()).getUsername();

        // memberName을 companyService.register()에 전달
        companyService.register(companyDTO, imageFiles, memberName);

        redirectAttributes.addFlashAttribute("message", "본사 등록이 완료되었습니다.");
        return "redirect:/chief/company/list";
    }

    //목록
    @Operation(summary = "호텔장 용 본사 목록", description = "본사 목록 페이지로 이동한다.")
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String searchType,
                       @PageableDefault(page = 1) Pageable page,
                       Model model,
                       Authentication authentication) {

        // 로그인한 사용자의 memberId를 가져오는 방법
        Integer memberId = getLoggedInMemberId(authentication); // 로그인한 사용자의 memberId를 가져오는 메서드 호출

        // memberId가 null이라면 로그인 실패 처리 (옵션)
        if (memberId == null) {
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        // 검색 기능을 포함한 서비스 호출
        Page<CompanyDTO> companyDTOS = companyService.listByChief(page, keyword, searchType, memberId);

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

    // 로그인한 사용자의 memberId를 가져오는 메서드
    private Integer getLoggedInMemberId(Authentication authentication) {
        // authentication이 null이 아니고, 인증된 사용자가 있는지 확인
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("로그인된 사용자가 없습니다.");
        }

        // authentication.getName()을 memberName으로 대체
        String memberEmail = authentication.getName();

        // memberEmail이 null이거나 비어있을 경우 처리
        if (memberEmail == null || memberEmail.isEmpty()) {
            throw new RuntimeException("회원 정보가 존재하지 않습니다.");
        }

        // memberName을 통해 Member 조회
        Member member = memberService.findMemberByMemberEmail(memberEmail);

        // member가 null인 경우 처리
        if (member == null) {
            throw new RuntimeException("회원 정보가 존재하지 않습니다.");
        }

        return member.getMemberId(); // memberId 반환
    }



    //읽기
    @Operation(summary = "관리자용 본사 상세", description = "companyId에 맞는 본사 상세 페이지로 이동한다.")
    @GetMapping("/read")
    public String read(@RequestParam Integer companyId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            // 회사 정보 조회
            CompanyDTO companyDTO = companyService.read(companyId);
            if (companyDTO == null) {
                redirectAttributes.addFlashAttribute("message", "해당 본사가 존재하지 않습니다!");
                return "redirect:/company/list";
            }

            // Pageable 객체 생성
            Pageable pageable = PageRequest.of(page, 10);  // 10개씩 표시

            // 본사 ID에 맞는 호텔(지사) 목록을 페이징 처리하여 가져옵니다.
            Page<HotelDTO> hotelDTOS = companyService.hotelListBycompany(companyId, pageable);

            // 호텔(지사) 목록에 관련된 이미지 포맷팅 처리
            for (HotelDTO hotelDTO : hotelDTOS) {
                if (hotelDTO.getHotelImgDTOList() != null && !hotelDTO.getHotelImgDTOList().isEmpty()) {
                    log.info("Room ID: {} - 이미지 개수: {}", hotelDTO.getHotelId(), hotelDTO.getHotelImgDTOList().size());
                } else {
                    log.info("Room ID: {} - 이미지 없음", hotelDTO.getHotelId());
                }
            }

            model.addAttribute("companyDTO", companyDTO);
            model.addAttribute("hotelDTOS", hotelDTOS.getContent());  // 현재 페이지의 객실 목록
            model.addAttribute("totalPages", hotelDTOS.getTotalPages());  // 총 페이지 수
            model.addAttribute("currentPage", page);  // 현재 페이지

            return "/chief/company/read";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다!");
            return "redirect:/chief/company/list";
        }
    }


    //수정폼
    @Operation(summary = "관리자용 본사 수정 처리", description = "본사를 수정 처리 한다.")
    @GetMapping("/update")
    public String updateHTML(Integer companyId, Model model) {
        CompanyDTO companyDTO = companyService.read(companyId);
        model.addAttribute("companyDTO", companyDTO);
        return "/chief/company/update";
    }
    //수정처리
    @Operation(summary = "관리자용 본사 수정 처리", description = "본사를 수정 처리 한다.")
    @PostMapping("/update")
    public String updateProc(CompanyDTO companyDTO, List<MultipartFile> newImageFiles, RedirectAttributes redirectAttributes) {

        companyService.update(companyDTO, newImageFiles);
        redirectAttributes.addFlashAttribute("message", "본사 수정이 완료되었습니다.");

        Integer companyId = companyDTO.getCompanyId();
        redirectAttributes.addFlashAttribute("companyId", companyId);

        return "redirect:/chief/company/read?companyId=" + companyId;
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
    @Operation(summary = "관리자용 본사 삭제 처리", description = "본사를 삭제 처리 한다.")
    @GetMapping("/delete")
    public String delete(Integer companyId, RedirectAttributes redirectAttributes) {
        companyService.delete(companyId);
        redirectAttributes.addFlashAttribute("message", "해당 본사 삭제가 완료되었습니다.");
        return "redirect:/chief/company/list";
    }


}
