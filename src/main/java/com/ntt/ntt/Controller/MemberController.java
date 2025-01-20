package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberSevice;
    private final MemberService memberService;

    // 회원가입 유형 선택 페이지
    @GetMapping("/selectrole")
    public String selectrole() {
        return "selectrole";
    }

    // 일반 유저 회원가입
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerProc(MemberDTO memberDTO) {
        memberSevice.saveUser(memberDTO);

        return "redirect:/login";
    }

    // 관리자 회원가입
    @GetMapping("/admin/register")
    public String adminregisterForm() {
        return "admin/register";
    }

    @PostMapping("/admin/register")
    public String adminregisterProc(MemberDTO memberDTO) {
        memberSevice.saveManager(memberDTO);

        return "redirect:/login";
    }

    // 로그인
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 회원정보 수정 페이지
    @GetMapping("/myPage/update")
    public String updateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String memberEmail = userDetails.getUsername();
        MemberDTO memberDTO = memberService.read(memberEmail);
        model.addAttribute("memberDTO", memberDTO);
        return "myPage/update";
    }

    // 회원정보 수정 처리
    @PostMapping("/myPage/update")
    public String updateMember(@ModelAttribute MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        try {
            memberService.update(memberDTO);
            redirectAttributes.addFlashAttribute("successMessage", "회원정보 수정 성공");
            return "redirect:/myPage/update"; // 성공 후 마이페이지로 이동
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원정보 수정 중 오류가 발생했습니다.");
            return "redirect:/myPage/update";
        }
    }

    // 회원탈퇴
    @PostMapping("/delete")
    public String deleteForm(@AuthenticationPrincipal UserDetails userDetails, HttpSession session) {
        String memberEmail = userDetails.getUsername();
        memberService.delete(memberEmail);
        session.invalidate(); // 회원 탈퇴 후 세션 삭제
        return "redirect:/";
    }
}
