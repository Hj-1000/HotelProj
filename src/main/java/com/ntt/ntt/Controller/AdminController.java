package com.ntt.ntt.Controller;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.CompanyDTO;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.NotificationRepository;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.company.CompanyService;
import com.ntt.ntt.Service.hotel.HotelService;
import com.ntt.ntt.Util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Tag(name = "adminController", description = "사이트 관리, 회원 관리 컨트롤러")
public class AdminController {

    private final HotelService hotelService;
    private final MemberService memberService;
    private final CompanyService companyService;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final UserDetailsService userDetailsService;
    private final PaginationUtil paginationUtil;

    @Operation(summary = "전체회원목록", description = "전체회원목록 페이지로 이동한다.")
    @GetMapping("/admin/memberList")
    public String getAllMembers(@RequestParam(required = false) String role,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        try {
            if ("전체".equals(role)) role = null;
            if ("전체".equals(status)) status = null;

            // 필터링된 회원 리스트 가져오기
            List<MemberDTO> filteredMembers = memberService.getFilteredMembers(role, email, status, name, phone, startDate, endDate);

            // memberId 기준 내림차순 정렬
            // filteredMembers.sort(Comparator.comparing(MemberDTO::getMemberId).reversed());

            // 페이징 처리
            int startIdx = page * size;
            int endIdx = Math.min(startIdx + size, filteredMembers.size());
            List<MemberDTO> pagedMembers = filteredMembers.subList(startIdx, endIdx);

            model.addAttribute("memberDTOList", pagedMembers);
            model.addAttribute("roles", Role.values());
            model.addAttribute("pageNumber", page);
            model.addAttribute("totalPages", (int) Math.ceil((double) filteredMembers.size() / size));
            model.addAttribute("size", size);

        } catch (Exception e) {
            model.addAttribute("error", "회원 목록을 가져오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return "admin/memberList";
    }

    // /admin/update url 접근 방지용
    @GetMapping("/admin/update")
    public String adminUpdateF() {
        return "redirect:/";
    }

    @Operation(summary = "회원정보 수정", description = "사용자의 회원정보를 새로 입력한 값으로 업데이트한다.")
    @PostMapping("/admin/update")
    public String adminUpdate(@ModelAttribute MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        try {
            // 회원 정보 업데이트
            memberService.adminUpdate(memberDTO);

            // 회원 정보가 수정되었으므로, 현재 로그인된 사용자 정보로 Authentication 객체 갱신
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = user.getUsername();

            Set<GrantedAuthority> authorities = new HashSet<>(userDetailsService.loadUserByUsername(username).getAuthorities());

            // 새롭게 갱신된 인증 객체 생성
            Authentication newAuth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), authorities);

            // 새 인증 객체를 SecurityContext에 설정
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            // 성공 메시지 설정
            String successMessage = memberDTO.getMemberName() + " 회원의 회원 정보가 성공적으로 수정되었습니다.";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/memberList";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/memberList";
        }
    }

    @Operation(summary = "임원관리", description = "임원관리 페이지로 이동한다.")
    @GetMapping("/admin/executiveList")
    public String getExecutives(@RequestParam(required = false) Integer companyId,
                                @RequestParam(required = false) Integer hotelId,
                                @RequestParam(required = false) String role,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model,
                                Principal principal) {
        try {
            // "전체" 선택 시 필터링을 적용하지 않도록 null 처리
            if ("전체".equals(role)) role = null;
            if ("전체".equals(status)) status = null;

            // 로그인한 사용자의 이메일 가져오기
            String userEmail = principal.getName();
            Member member = memberRepository.findByMemberEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

            // 로그인한 사용자가 등록한 본사 목록 가져오기
            List<CompanyDTO> companyList = companyService.getFilteredCompany(member.getMemberId());
            model.addAttribute("companyList", companyList);

            // 필터링된 호텔 리스트 가져오기
            List<HotelDTO> filteredHotels = new ArrayList<>();

            if (companyId != null) {
                // 특정 본사의 지사만 조회
                filteredHotels.addAll(hotelService.getFilteredHotelsByMember(
                        companyId, hotelId, role, email, status, name, phone, startDate, endDate));
            } else {
                // 전체 본사를 조회하여 각 본사에 속한 지사를 필터링
                for (CompanyDTO company : companyList) {
                    filteredHotels.addAll(hotelService.getFilteredHotelsByMember(
                            company.getCompanyId(), hotelId, role, email, status, name, phone, startDate, endDate));
                }
            }

            // 호텔 리스트를 hotelId 기준으로 내림차순 정렬
            // filteredHotels.sort(Comparator.comparing(HotelDTO::getHotelId).reversed());

            // 페이징 처리
            int startIdx = page * size;
            int endIdx = Math.min(startIdx + size, filteredHotels.size());
            List<HotelDTO> pagedHotels = filteredHotels.subList(startIdx, endIdx);

            // 모델에 데이터 추가
            model.addAttribute("hotelMemberIds", pagedHotels);
            model.addAttribute("roles", Role.values());
            model.addAttribute("pageNumber", page);
            model.addAttribute("totalPages", (int) Math.ceil((double) filteredHotels.size() / size));
            model.addAttribute("size", size);

        } catch (Exception e) {
            model.addAttribute("error", "회원 목록을 가져오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return "admin/executiveList";
    }

    @Operation(summary = "매니저의 회원정보 수정", description = "매니저의 회원정보를 새로 입력한 값으로 업데이트한다.")
    @PostMapping("/admin/executiveUpdate")
    public String managerUpdate(MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        try {
            // 회원 정보 업데이트
            memberService.managerUpdate(memberDTO);

            // 회원 정보가 수정되었으므로, 현재 로그인된 사용자 정보로 Authentication 객체 갱신
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = user.getUsername();

            Set<GrantedAuthority> authorities = new HashSet<>(userDetailsService.loadUserByUsername(username).getAuthorities());

            // 새롭게 갱신된 인증 객체 생성
            Authentication newAuth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), authorities);

            // 새 인증 객체를 SecurityContext에 설정
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            // 성공 메시지 설정
            String successMessage = memberDTO.getMemberName() + " 회원의 회원 정보가 성공적으로 수정되었습니다.";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/executiveList";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/executiveList";
        }
    }

    @Operation(summary = "매니저 회원가입 페이지", description = "매니저 회원가입 페이지로 이동한다.")
    @GetMapping("/admin/executiveRegister")
    public String executiveRegisterForm(Model model, Principal principal) {
        try {
            // 로그인한 사용자의 이메일 가져오기
            String userEmail = principal.getName();

            // 이메일로 회원 정보 조회
            Member member = memberRepository.findByMemberEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

            // 현재 로그인한 사용자가 등록한 본사 목록 가져오기
            List<CompanyDTO> companyList = companyService.getFilteredCompany(member.getMemberId());

            // 모델에 companyList 추가
            model.addAttribute("companyList", companyList);

        } catch (Exception e) {
            model.addAttribute("error", "회원 목록을 가져오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return "admin/executiveRegister";
    }

    @Operation(summary = "특정 본사의 지사 목록 가져오기", description = "선택한 본사 companyId 에 해당하는 지사(호텔) 목록을 가져온다.")
    @GetMapping("/admin/hotels")
    @ResponseBody
    public List<HotelDTO> getHotelsByCompany(@RequestParam Integer companyId) {
        return hotelService.getFilteredHotel(companyId);
    }

    @Operation(summary = "매니저 회원가입 요청", description = "입력한 유저 정보를 데이터에 저장하고 로그인 페이지로 이동한다.")
    @PostMapping("/admin/executiveRegister")
    public String executiveRegisterProc(MemberDTO memberDTO, @RequestParam Integer hotelId) {
        try {
            // 1. 매니저 회원 등록 후 Member 객체 받기
            Member member = memberService.saveManager(memberDTO); // Member 객체 반환

            // 2. Member 객체에서 memberId 가져오기
            Integer memberId = member.getMemberId();

            // 3. 호텔 DTO에 Member 객체 설정
            HotelDTO hotelDTO = new HotelDTO();
            hotelDTO.setHotelId(hotelId);
            hotelDTO.setMemberId(member);  // 방금 생성된 Member 객체로 설정

            // 4. 호텔의 memberId 업데이트
            hotelService.updateHotelMemberId(hotelDTO);  // 호텔의 memberId 업데이트

            return "redirect:/admin/executiveList"; // 회원가입 성공 시 현재 페이지로 리다이렉트
        } catch (IllegalStateException e) {
            // 예외가 발생한 경우 회원가입 페이지로 리다이렉트
            return "redirect:/admin/executiveRegister"; // 회원가입 페이지로 리다이렉트
        }
    }

    // 설정한 주소가 지도로 잘 나오는지 테스트용으로 만듦, 기능 구현 완성 이후에는 삭제하기
    @GetMapping("/admin/mapTest")
    public String read(@RequestParam Integer hotelId, Model model, RedirectAttributes redirectAttributes) {
        try {
            HotelDTO hotelDTO = hotelService.read(hotelId);
            model.addAttribute("hotelDTO", hotelDTO);
            return "/admin/mapTest";

        } catch (NullPointerException e) {
            // Flash Attribute로 메시지를 전달
            redirectAttributes.addFlashAttribute("message", "해당 지사가 없습니다!");
            return "redirect:/manager/hotel/list"; // 목록 페이지로 리다이렉트
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/manager/hotel/list"; // 목록 페이지로 리다이렉트
        }
    }

    // 알림기능 구현
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        int unreadCount = notificationRepository.countByIsReadFalse();
        model.addAttribute("unreadCount", unreadCount);
        return "layout"; // Thymeleaf 템플릿 이름
    }


    //관리자 본사 목록
    @Operation(summary = "관리자용 본사 목록", description = "본사 목록 페이지로 이동한다.")
    @GetMapping("/admin/company/list")
    public String list(@RequestParam(required = false) String keyword,
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

            return "/admin/company/list";

        } catch (AuthenticationCredentialsNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/admin/executiveList"; // 임원관리 페이지로 리다이렉트
        }

    }



    //관리자 호텔 목록
    @Operation(summary = "관리자용 호텔 목록", description = "전체 호텔 목록 페이지로 이동한다.")
    @GetMapping("/admin/hotel/list")
    public String list(@RequestParam(required = false) String keyword,
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

            // 회사 목록 추가
            List<CompanyDTO> companyDTOS = hotelService.getAllCompany();
            model.addAttribute("companyDTOS", companyDTOS);
            model.addAttribute("companyDTO", new CompanyDTO());

            return "/admin/hotel/list"; // 뷰 경로
        } catch (AuthenticationCredentialsNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "서버 오류가 있습니다!");
            return "redirect:/admin/executiveList"; // 임원관리 페이지로 리다이렉트
        }
    }



}
