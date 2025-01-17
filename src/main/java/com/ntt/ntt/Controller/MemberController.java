package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Service.MemberSevice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberSevice memberSevice;

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
    @GetMapping("/adminregister")
    public String adminregisterForm() {
        return "adminregister";
    }

    @PostMapping("/adminregister")
    public String adminregisterProc(MemberDTO memberDTO) {
        memberSevice.saveManager(memberDTO);

        return "redirect:/login";
    }

    // 로그인
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
