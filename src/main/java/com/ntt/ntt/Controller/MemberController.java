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
import org.springframework.web.bind.annotation.PostMapping;

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
    public String updateProc(MemberDTO memberDTO, Model model) {
        try {
            memberService.update(memberDTO);
            return "redirect:/myPage/update"; // 수정 후 다시 해당 페이지로 리다이렉트
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage()); // 오류 메시지 전달
            return "myPage/update"; // 오류 발생 시 수정 페이지로 돌아가기
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
