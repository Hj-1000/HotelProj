package com.ntt.ntt.Controller;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@Tag(name = "passwordResetController", description = "비밀번호 재설정 컨트롤러")
public class PasswordResetController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // PasswordEncoder 주입

    @Operation(summary = "비밀번호 찾기폼", description = "비밀번호 찾기 페이지로 이동한다.")
    @GetMapping("/user/findPassword")
    public String findPasswordForm(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/";  // 로그인되어있으면 메인페이지로 리다이렉트
        }
        return "user/findPassword";
    }

    @Operation(summary = "비밀번호 찾기 요청", description = "입력한 이메일이 데이터에 존재하는지 확인 후 해당 이메일로 인증코드를 발송한다.")
    @PostMapping("/user/findPassword")
    public String findPasswordProc(@RequestParam("memberEmail") String email, Model model, HttpSession session) {

        // 1. 입력한 이메일이 회원목록에 있는지 확인
        Optional<Member> member = memberRepository.findByMemberEmail(email);
        if (member.isEmpty()) { // 입력한 이메일이 목록에 없을 경우
            model.addAttribute("errorMessage", "해당 이메일로 가입된 정보가 없습니다.");
            return "user/findPassword"; // 다시 비밀번호 찾기 페이지로 돌아감
        }

        // 2. 이메일로 발송할 랜덤 코드 생성
        String resetCode = generateRandomCode();

        // 3. 세션에 코드 저장 (비동기 메서드는 즉시 실행되므로, 세션 저장을 먼저 함)
        session.setAttribute("resetCode", resetCode);
        session.setAttribute("resetCodeTimestamp", System.currentTimeMillis());
        session.setAttribute("email", email);

        // 4. 비동기 이메일 전송 (비밀번호 찾기를 위한 이메일 발송 속도가 느려서 비동기 메서드 사용, 이메일이 발송되는 동안에도 컨트롤러는 바로 응답 가능)
        emailService.sendPasswordResetEmail(email, resetCode);

        // 5. 코드 입력 페이지로 리디렉션
        return "redirect:/user/verifyCode"; // 코드 입력 페이지로 리디렉션
    }

    @Operation(summary = "랜덤 코드 생성", description = "이메일로 발송할 랜덤 코드를 생성한다.")
    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        // 랜덤 코드에 사용될 문자
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        // 랜덤 코드 길이 (6글자)
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    @Operation(summary = "인증 코드 확인폼", description = "인증 코드 입력 페이지로 이동한다.")
    @GetMapping("/user/verifyCode")
    public String verifyCodeForm(HttpSession session) {
        // 세션에 저장된 인증 코드와 타임스탬프를 가져와서
        String resetCode = (String) session.getAttribute("resetCode");
        Long timestamp = (Long) session.getAttribute("resetCodeTimestamp");

        // 인증 코드나 타임스탬프가 세션에 없으면 비밀번호 찾기 페이지로 리디렉션
        if (resetCode == null || timestamp == null) {
            return "redirect:/user/findPassword";
        }

        // 30초가 지나면 인증 코드 만료 -> 테스트하느라 30초로 했는데 실제 적용시에는 3분(180000ms) 로 수정하기
        if (System.currentTimeMillis() - timestamp > 30000) {
            // 인증 코드가 만료되면 세션에서 인증 코드와 타임스탬프를 삭제하고 비밀번호 찾기 페이지로 리디렉션
            session.removeAttribute("resetCode");
            session.removeAttribute("resetCodeTimestamp");
            return "redirect:/user/findPassword";
        }

        // 인증 코드가 유효하면 verifyCode 페이지를 반환
        return "user/verifyCode";
    }

    @Operation(summary = "인증 코드 확인 요청", description = "인증 코드를 검증해 비밀번호 재설정 페이지로 이동한다.")
    @PostMapping("/user/verifyCode")
    public String verifyCodeProc(@RequestParam("verificationCode") String userCode, HttpSession session, Model model) {
        // 세션에서 저장된 인증 코드와 타임스탬프를 가져와서
        String resetCode = (String) session.getAttribute("resetCode");
        Long timestamp = (Long) session.getAttribute("resetCodeTimestamp");

        // 인증 코드와 타임스탬프가 세션에 있으면
        if (resetCode != null && timestamp != null) {
            // 30초(30000ms) 초과 여부 확인 -> 테스트하느라 30초로 했는데 실제 적용시에는 3분(180000ms) 로 수정하기
            if (System.currentTimeMillis() - timestamp > 30000) {
                // 만약 30초가 지났다면 코드 만료, 세션에서 인증 코드와 타임스탬프 삭제 후 다시 비밀번호 찾기 페이지로 리디렉션
                model.addAttribute("errorMessage", "인증 코드가 만료되었습니다.");
                session.removeAttribute("resetCode");
                session.removeAttribute("resetCodeTimestamp");
                return "redirect:/user/findPassword";
            }

            // 세션에 저장된 인증 코드와 사용자가 입력한 인증 코드가 일치하면 인증 완료 표시를 세션에 저장 후 비밀번호 재설정 페이지로 리디렉션
            if (resetCode.equals(userCode)) {
                session.setAttribute("isVerified", true);
                return "redirect:/user/resetPassword";
            }
        }

        // 세션에 저장된 인증 코드와 사용자가 입력한 인증 코드가 일치하지 않으면 오류 메시지 표시
        model.addAttribute("errorMessage", "입력한 인증 코드가 올바르지 않습니다.");
        return "user/verifyCode"; // 코드 입력 페이지로 돌아감
    }

    @Operation(summary = "인증 코드 재전송", description = "이메일로 발송할 랜덤 코드를 다시 생성해 발송한다.")
    @PostMapping("/user/resendCode")
    @ResponseBody
    public Map<String, Object> resendCode(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // 세션에서 이메일 정보 가져오기
        String email = (String) session.getAttribute("email");

        if (email == null) {
            response.put("success", false);
            response.put("message", "이메일 정보가 없습니다. 다시 시도해주세요.");
            return response;
        }

        // 랜덤 코드 생성
        String resetCode = generateRandomCode();

        // 이메일 전송
        emailService.sendPasswordResetEmail(email, resetCode);

        // 세션에 새로운 코드 및 타임스탬프 저장
        session.setAttribute("resetCode", resetCode);
        session.setAttribute("resetCodeTimestamp", System.currentTimeMillis());

        response.put("success", true);
        return response;
    }

    @Operation(summary = "비밀번호 재설정폼", description = "비밀번호 재설정 페이지로 이동한다.")
    @GetMapping("/user/resetPassword")
    public String resetPasswordForm(HttpSession session, Model model) {
        // 세션에 저장된 인증 상태 확인
        Boolean isVerified = (Boolean) session.getAttribute("isVerified");

        // 인증되지 않은 경우 인증 페이지로 리디렉션
        if (isVerified == null || !isVerified) {
            model.addAttribute("errorMessage", "인증을 먼저 완료하세요.");
            return "redirect:/user/verifyCode";  // 인증을 먼저 완료해야 비밀번호 재설정 페이지에 접근 가능
        }

        // 인증되어있는 경우, 세션에서 인증코드와 인증상태를 지우고 비밀번호 재설정 페이지로 이동
        session.removeAttribute("resetCode");
        session.removeAttribute("isVerified");
        return "user/resetPassword";
    }

    @Operation(summary = "비밀번호 재설정 요청", description = "새로운 비밀번호를 암호화 한 후 기존 데이터를 수정한다.")
    @PostMapping("/user/resetPassword")
    public String resetPasswordProc(@RequestParam("newPassword") String newPassword, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("email");

        Optional<Member> member = memberRepository.findByMemberEmail(email);
        if (member.isPresent()) {
            Member m = member.get();
            // 새 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(newPassword);
            m.setMemberPassword(encodedPassword);  // 암호화된 비밀번호 설정
            memberRepository.save(m);

            // 비밀번호 재설정 후 세션에서 이메일을 제거
            session.removeAttribute("email");

            // 비밀번호 재설정 완료 메시지 추가
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 재설정되었습니다.");

            return "redirect:/login";  // 로그인 페이지로 리디렉션
        } else {
            model.addAttribute("errorMessage", "회원 정보를 찾을 수 없습니다.");
            return "user/resetPassword";  // 비밀번호 재설정 페이지로 돌아감
        }
    }

    @Operation(summary = "세션 만료", description = "세션 만료 시간을 전달한다.")
    @GetMapping("/user/getSessionExpiry")
    @ResponseBody
    public long getSessionExpiry(HttpSession session) {
        Long timestamp = (Long) session.getAttribute("resetCodeTimestamp");
        if (timestamp != null) {
            // 30초 (30000ms) 후 만료되는 시간 계산 -> 테스트하느라 30초로 했는데 실제 적용시에는 3분(180000ms) 로 수정하기
            long expiryTime = timestamp + 30000;
            return expiryTime;
        }
        return 0;
    }
}