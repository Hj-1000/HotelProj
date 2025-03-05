package com.ntt.ntt.Controller.admin;

import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.DTO.RoomDTO;
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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/admin")
@AllArgsConstructor
@Log4j2
public class AdminCompanyHotelController {

    private final CompanyService companyService;
    private final HotelService hotelService;
    private final ImageService imageService;
    private final MemberService memberService;
    private final PaginationUtil paginationUtil;

    //등록폼
    @Operation(summary = "관리자용 본사 등록 폼", description = "본사 등록 폼 페이지로 이동한다.")
    @GetMapping("/company/register")
    public String compnayRegisterForm(Model model) {

        List<MemberDTO> memberDTOS = companyService.getAllChiefs();
        model.addAttribute("memberDTOS", memberDTOS);
        model.addAttribute("memberDTO", new MemberDTO());

        return "/chief/company/register";
    }
    //등록처리
    @Operation(summary = "관리자용 본사 등록 처리", description = "본사를 등록 처리 한다.")
    @PostMapping("/company/register")
    public String companyRegisterProc(@Valid @ModelAttribute CompanyDTO companyDTO,
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
        companyService.register(companyDTO, imageFiles);

        redirectAttributes.addFlashAttribute("message", "본사 등록이 완료되었습니다.");
        return "redirect:/admin/company/list";
    }


    //관리자 본사 목록
    @Operation(summary = "관리자용 본사 목록", description = "본사 목록 페이지로 이동한다.")
    @GetMapping("/company/list")
    public String companyList(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String searchType,
                       @PageableDefault(page = 1) Pageable page,
                       Model model, RedirectAttributes redirectAttributes) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 로그인 여부 확인 (익명 사용자일 경우 authentication이 null이거나 인증되지 않음)
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                throw new AuthenticationCredentialsNotFoundException("로그인이 필요합니다.");
            }

            // 검색 기능을 포함한 서비스 호출
            Page<CompanyDTO> companyDTOS = companyService.listByAdmin(page, keyword, searchType);

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

        } catch (AuthenticationCredentialsNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/admin/executiveList"; // 임원관리 페이지로 리다이렉트
        }

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
    @GetMapping("/company/read")
    public String companyRead(@RequestParam Integer companyId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            // 회사 정보 조회
            CompanyDTO companyDTO = companyService.read(companyId);
            if (companyDTO == null) {
                redirectAttributes.addFlashAttribute("message", "해당 본사가 존재하지 않습니다!");
                return "redirect:/admin/company/list";
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
            return "redirect:/admin/company/list";
        }
    }

    //수정폼
    @Operation(summary = "관리자용 본사 수정 처리", description = "본사를 수정 처리 한다.")
    @GetMapping("/company/update")
    public String updateHTML(Integer companyId, Model model) {
        CompanyDTO companyDTO = companyService.read(companyId);
        model.addAttribute("companyDTO", companyDTO);
        return "/chief/company/update";
    }
    //수정처리
    @Operation(summary = "관리자용 본사 수정 처리", description = "본사를 수정 처리 한다.")
    @PostMapping("/company/update")
    public String updateProc(CompanyDTO companyDTO, List<MultipartFile> newImageFiles, RedirectAttributes redirectAttributes) {

        companyService.update(companyDTO, newImageFiles);
        redirectAttributes.addFlashAttribute("message", "본사 수정이 완료되었습니다.");

        Integer companyId = companyDTO.getCompanyId();
        redirectAttributes.addFlashAttribute("companyId", companyId);

        return "redirect:/admin/company/read?companyId=" + companyId;
    }


    //본사 삭제
    @Operation(summary = "관리자용 본사 삭제 처리", description = "본사를 삭제 처리 한다.")
    @GetMapping("/company/delete")
    public String delete(Integer companyId, RedirectAttributes redirectAttributes) {
        companyService.delete(companyId);
        redirectAttributes.addFlashAttribute("message", "해당 본사 삭제가 완료되었습니다.");
        return "redirect:/admin/company/list";
    }



    // ------------------ hotel ---------------------
    //등록폼
    @Operation(summary = "관리자용 호텔 등록 폼", description = "호텔 등록 페이지로 이동한다.")
    @GetMapping("/hotel/register")
    public String registerForm(Model model) {
        //검증처리가 필요하면 빈 companyDTO를 생성해서 전달한다.
        List<CompanyDTO> companyDTOS = hotelService.getAllCompany();
        model.addAttribute("companyDTOS", companyDTOS);
        model.addAttribute("companyDTO", new CompanyDTO());

        return "/manager/hotel/register";
    }

    //등록처리
    @Operation(summary = "관리자용 호텔 등록 처리", description = "호텔을 등록 처리한다.")
    @PostMapping("/hotel/register")
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
        return "redirect:/admin/hotel/list";  // 목록으로 이동
    }


    //관리자 호텔 목록
    @Operation(summary = "관리자용 호텔 목록", description = "전체 호텔 목록 페이지로 이동한다.")
    @GetMapping("/hotel/list")
    public String hotelList(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) Integer keyword1,  // 별점 검색용
                       @RequestParam(defaultValue = "1") int page, // 기본값을 1로 설정 (1-based)
                       @PageableDefault(size = 10) Pageable pageable, // 한 페이지에 10개씩
                       Model model, HttpServletRequest request,
                       RedirectAttributes redirectAttributes
    ) {

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 로그인 여부 확인 (익명 사용자일 경우 authentication이 null이거나 인증되지 않음)
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                throw new AuthenticationCredentialsNotFoundException("로그인이 필요합니다.");
            }

            // 페이지 번호를 0-based로 변환 (page는 1-based로 들어오므로 1을 빼줌)
            Pageable adjustedPageable = PageRequest.of(page - 1, pageable.getPageSize());

            Page<HotelDTO> hotelDTOS;
            hotelDTOS = hotelService.listByAdmin(adjustedPageable, keyword, keyword1, searchType);

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

            return "/manager/hotel/list"; // 뷰 경로
        } catch (AuthenticationCredentialsNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/admin/executiveList"; // 임원관리 페이지로 리다이렉트
        }
    }

    //읽기
    @Operation(summary = "관리자용 호텔 상세", description = "hotelId에 맞는 호텔 상세 페이지로 이동한다.")
    @GetMapping("/hotel/read")
    public String hotelRead(@RequestParam Integer hotelId,
                       @RequestParam(defaultValue = "0") int page,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            HotelDTO hotelDTO = hotelService.read(hotelId);
            if (hotelDTO == null) {
                redirectAttributes.addFlashAttribute("message", "해당 호텔이 존재하지 않습니다!");
                return "redirect:/admin/hotel/list";
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
            return "redirect:/admin/hotel/list";
        }
    }

    //수정폼
    @Operation(summary = "관리자용 호텔 수정폼", description = "hotelId에 맞는 호텔 수정폼 페이지로 이동한다.")
    @GetMapping("/hotel/update")
    public String updateForm(Integer hotelId, Model model) {
        HotelDTO hotelDTO = hotelService.read(hotelId);
        List<CompanyDTO> companyDTOS = hotelService.getMyCompany();
        model.addAttribute("companyDTOS", companyDTOS);
        model.addAttribute("companyDTO", new CompanyDTO());
        model.addAttribute("hotelDTO", hotelDTO);
        return "/manager/hotel/update";
    }
    //수정처리
    @Operation(summary = "관리자용 호텔 수정 처리", description = "hotelId에 맞는 호텔 수정 처리한다.")
    @PostMapping("/hotel/update")
    public String updateProc(HotelDTO hotelDTO, List<MultipartFile> newImageFiles, RedirectAttributes redirectAttributes) {
        hotelService.update(hotelDTO, newImageFiles);
        redirectAttributes.addFlashAttribute("message", "지사 수정이 완료되었습니다.");

        Integer hotelId = hotelDTO.getHotelId();
        redirectAttributes.addFlashAttribute("hotelId", hotelId);

        return "redirect:/admin/hotel/read?hotelId=" + hotelId;
    }

    //삭제
    @Operation(summary = "관리자용 호텔 삭제", description = "hotelId에 맞는 호텔을 삭제한다.")
    @GetMapping("/hotel/delete")
    public String hotelDelete(Integer hotelId, RedirectAttributes redirectAttributes) {

        // 삭제 전 호텔 정보 조회
        HotelDTO hotelDTO = hotelService.findById(hotelId);  // 호텔 정보를 먼저 가져옴

        if (hotelDTO == null) {
            redirectAttributes.addFlashAttribute("error", "해당 호텔을 찾을 수 없습니다.");
            return "redirect:/admin/hotel/list";
        }

        // 삭제 수행
        hotelService.delete(hotelId);

        // 삭제한 지사의 companyId 가져오기
        Integer companyId = hotelDTO.getCompanyId();

        redirectAttributes.addFlashAttribute("message", "해당 지사 삭제가 완료되었습니다.");

        return "redirect:/admin/hotel/list";  // companyId를 쿼리 파라미터로 전달
    }






}
