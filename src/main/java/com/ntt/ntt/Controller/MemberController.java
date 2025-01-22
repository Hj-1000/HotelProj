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
import org.springframework.web.bind.annotation.RequestParam;
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
            redirectAttributes.addFlashAttribute("successMessage", "회원정보 수정에 성공하였습니다.");
            return "redirect:/myPage/update"; // 성공 후 마이페이지로 이동
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            return "redirect:/myPage/update";
        }
    }

    // 회원탈퇴
    @PostMapping("/delete")
    public String deleteForm(@AuthenticationPrincipal UserDetails userDetails, HttpSession session,
                             @RequestParam String currentPassword, RedirectAttributes redirectAttributes) {
        String memberEmail = userDetails.getUsername();

        try {
            memberService.delete(memberEmail, currentPassword); // 비밀번호를 함께 전달
        } catch (IllegalArgumentException e) {
            // 비밀번호 불일치 시 처리
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/myPage/update"; // 비밀번호 불일치 시 마이페이지로 이동
        }

        session.invalidate(); // 회원 탈퇴 후 세션 삭제
        return "redirect:/"; // 메인 페이지로 리디렉션
    }


}
