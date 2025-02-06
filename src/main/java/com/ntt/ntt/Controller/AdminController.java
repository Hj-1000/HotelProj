package com.ntt.ntt.Controller;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.HotelDTO;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.NotificationService;
import com.ntt.ntt.Service.hotel.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final HotelService hotelService;
    private final MemberService memberService;

    @Operation(summary = "전체회원목록", description = "전체회원목록 페이지로 이동한다.")
    @GetMapping("/admin/memberList")
    public String getAllMembers(@RequestParam(required = false) String role,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                @RequestParam(defaultValue = "0") int page,  // 추가됨
                                @RequestParam(defaultValue = "10") int size, // 추가됨
                                Model model) {
        try {
            if ("전체".equals(role)) role = null;
            if ("전체".equals(status)) status = null;

            // 필터링된 회원 리스트 가져오기
            List<MemberDTO> filteredMembers = memberService.getFilteredMembers(role, email, status, name, phone, startDate, endDate);

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

    @Operation(summary = "회원정보 수정", description = "모든 사용자의 회원정보를 새로 입력한 값으로 업데이트한다.")
    @PostMapping("/admin/update")
    public String adminUpdate(MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        try {
            memberService.adminUpdate(memberDTO);
            redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 성공적으로 수정되었습니다.");
            return "redirect:/admin/memberList";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/memberList";
        }
    }

    @GetMapping("/admin/executive")
    public String a(){
        return "admin/executive";
    }

    @GetMapping("/admin/executiveRegister")
    public String b(){
        return "admin/executiveRegister";
    }

    @GetMapping("/admin/hotelHeadquarters")
    public String c(){
        return "admin/hotelHeadquarters";
    }

    @GetMapping("/admin/hotelHeadquartersRegister")
    public String d(){
        return "admin/hotelHeadquartersRegister";
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

}
