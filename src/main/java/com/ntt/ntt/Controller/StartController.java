package com.ntt.ntt.Controller;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Service.MemberService;
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
public class StartController {

    private final MemberService memberService;

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

    // 관리자가 회원정보 수정하기
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

}
