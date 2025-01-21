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
                                @RequestParam(required = false) String startDate,  // 추가된 부분
                                @RequestParam(required = false) String endDate,    // 추가된 부분
                                Model model) {
        try {
            // "전체" 상태에 대한 예외 처리 (필터링하지 않음)
            if ("전체".equals(role)) {
                role = null;
            }
        } catch (Exception e) {
            model.addAttribute("error", "회원 목록을 가져오는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }

        try {
            // "전체" 상태에 대한 예외 처리 (필터링하지 않음)
            if ("전체".equals(status)) {
                status = null;
            }

            // startDate와 endDate를 서비스 메서드에 전달하여 필터링을 적용
            List<MemberDTO> memberDTOList = memberService.getFilteredMembers(role, email, status, name, phone, startDate, endDate);

            // 결과 없으면 메시지 전달
            if (memberDTOList.isEmpty()) {
                model.addAttribute("message", "검색 조건에 맞는 회원이 없습니다.");
            } else {
                model.addAttribute("memberDTOList", memberDTOList);
            }

            // Role enum 전달
            model.addAttribute("roles", Role.values());
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

    @GetMapping("/roomList")
    public String e(){
        return "roomList";
    }
}
