package com.ntt.ntt.Controller;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Optional;

@Controller
public class PasswordResetController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // PasswordEncoder 주입

    // 비밀번호 찾기 페이지
    @GetMapping("/findPassword")
    public String findPassword() {
        return "findPassword";
    }

    // 비밀번호 찾기 요청 (POST)
    @PostMapping("/findPassword")
    public String findPassword(@RequestParam("memberEmail") String email, Model model, HttpSession session) {
        // 1. 이메일이 회원목록에 있는지 확인
        Optional<Member> member = memberRepository.findByMemberEmail(email);
        if (member.isEmpty()) {
            model.addAttribute("errorMessage", "이메일을 찾을 수 없습니다.");
            return "findPassword";  // 다시 비밀번호 찾기 페이지로 돌아감
        }

        // 2. 랜덤 코드 생성
        String resetCode = generateRandomCode();

        // 3. 이메일 전송
        emailService.sendPasswordResetEmail(email, resetCode);

        // 4. 생성된 코드 세션에 저장
        session.setAttribute("resetCode", resetCode);
        session.setAttribute("email", email); // 이메일도 저장하여 후속 작업에 사용

        // 5. 코드 입력 페이지로 리디렉션
        return "redirect:/verifyCode"; // 코드 입력 페이지로 리디렉션
    }

    // 랜덤 코드 생성 함수
    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    // 코드 확인 페이지 (GET 요청 처리)
    @GetMapping("/verifyCode")
    public String verifyCodePage() {
        return "verifyCode"; // 코드 입력 페이지로 이동
    }

    // 코드 확인 요청 (POST)
    @PostMapping("/verifyCode")
    public String verifyCode(@RequestParam("verificationCode") String userCode, HttpSession session, Model model) {
        // 세션에서 저장된 resetCode와 사용자가 입력한 코드 비교
        String resetCode = (String) session.getAttribute("resetCode");

        if (resetCode != null && resetCode.equals(userCode)) {
            // 코드가 일치하면 비밀번호 재설정 페이지로 리디렉션
            return "redirect:/resetPassword";  // 비밀번호 재설정 페이지로 이동
        } else {
            // 코드가 일치하지 않으면 오류 메시지 표시
            model.addAttribute("errorMessage", "입력한 코드가 올바르지 않습니다.");
            return "verifyCode"; // 코드 입력 페이지로 돌아감
        }
    }

    // 비밀번호 재설정 페이지 (GET)
    @GetMapping("/resetPassword")
    public String resetPasswordPage() {
        return "resetPassword";  // 비밀번호 재설정 페이지를 보여줍니다.
    }

    // 비밀번호 재설정 처리 (POST)
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam("newPassword") String newPassword, HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");

        Optional<Member> member = memberRepository.findByMemberEmail(email);
        if (member.isPresent()) {
            Member m = member.get();
            // 새 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(newPassword);
            m.setMemberPassword(encodedPassword);  // 암호화된 비밀번호 설정
            memberRepository.save(m);
            model.addAttribute("successMessage", "비밀번호가 성공적으로 재설정되었습니다.");
            return "redirect:/login";  // 로그인 페이지로 리디렉션
        } else {
            model.addAttribute("errorMessage", "회원 정보를 찾을 수 없습니다.");
            return "resetPassword";  // 비밀번호 재설정 페이지로 돌아감
        }
    }
}