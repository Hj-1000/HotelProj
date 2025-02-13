package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Tag(name = "memberController", description = "유저 정보 관리 컨트롤러")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입 유형 선택", description = "회원가입 유형 선택 페이지로 이동한다.")
    @GetMapping("/selectrole")
    public String selectrole() {
        return "selectrole";
    }

    @Operation(summary = "사용자 회원가입폼", description = "일반 유저 회원가입 페이지로 이동한다.")
    @GetMapping("/register")
    public String registerForm(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/";  // 로그인되어있으면 메인페이지로 리다이렉트
        }
        return "register";
    }

    @Operation(summary = "사용자 회원가입 요청", description = "입력한 유저 정보를 데이터에 저장하고 로그인 페이지로 이동한다.")
    @PostMapping("/register")
    public String registerProc(MemberDTO memberDTO) {
        try {
            memberService.saveUser(memberDTO);
            return "redirect:/login"; // 회원가입 성공 시 로그인 페이지로 리다이렉트
        } catch (IllegalStateException e) {
            // 예외가 발생한 경우 회원가입 페이지로 리다이렉트
            return "redirect:/register"; // 회원가입 페이지로 리다이렉트
        }
    }

    @Operation(summary = "관리자 회원가입폼", description = "관리자(어드민, 호텔장, 매니저) 회원가입 페이지로 이동한다.")
    @GetMapping("/admin/register")
    public String adminregisterForm(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/";  // 로그인되어있으면 메인페이지로 리다이렉트
        }
        return "admin/register";
    }

    @Operation(summary = "관리자 회원가입 요청", description = "입력한 유저 정보를 데이터에 저장하고 로그인 페이지로 이동한다.")
    @PostMapping("/admin/register")
    public String adminregisterProc(MemberDTO memberDTO) {
        try {
            memberService.saveAdmin(memberDTO);
            return "redirect:/login"; // 회원가입 성공 시 로그인 페이지로 리다이렉트
        } catch (IllegalStateException e) {
            // 예외가 발생한 경우 회원가입 페이지로 리다이렉트
            return "redirect:/admin/register"; // 회원가입 페이지로 리다이렉트
        }
    }

    @Operation(summary = "이메일 중복 확인", description = "입력한 이메일이 이미 데이터에 등록되어있는지 체크한다.")
    @GetMapping("/checkEmail")
    @ResponseBody
    public Map<String, Boolean> checkEmail(@RequestParam String email) {
        boolean isDuplicate = memberService.isEmailExists(email); // 이메일 중복 여부 체크
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", isDuplicate);
        return response;
    }

    @Operation(summary = "로그인", description = "로그인 페이지로 이동한다.")
    @GetMapping("/login")
    public String login(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/";  // 로그인되어있으면 메인페이지로 리다이렉트
        }
        return "login";
    }

    @Operation(summary = "회원정보 수정폼", description = "회원정보 수정 페이지로 이동한다.")
    @GetMapping("/myPage/update")
    public String updateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";  // 로그인되어있지 않으면 로그인 페이지로 리다이렉트
        }
        String memberEmail = userDetails.getUsername();
        MemberDTO memberDTO = memberService.read(memberEmail);
        model.addAttribute("memberDTO", memberDTO);
        return "myPage/update";
    }

    @Operation(summary = "회원정보 수정 요청", description = "로그인한 사용자의 회원정보를 새로 입력한 값으로 업데이트한다.")
    @PostMapping("/myPage/update")
    public String updateProc(@ModelAttribute MemberDTO memberDTO, RedirectAttributes redirectAttributes) {
        try {
            memberService.update(memberDTO);
            redirectAttributes.addFlashAttribute("successMessage", "회원정보 수정에 성공하였습니다.");
            return "redirect:/myPage/update"; // 성공 후 마이페이지로 이동
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            return "redirect:/myPage/update";
        }
    }

    @Operation(summary = "회원탈퇴", description = "회원탈퇴를 요청한 사용자의 정보를 삭제한다.")
    @PostMapping("/unregister")
    public String unregister(@AuthenticationPrincipal UserDetails userDetails, HttpSession session,
                             @RequestParam String currentPassword, RedirectAttributes redirectAttributes) {
        String memberEmail = userDetails.getUsername();

        try {
            memberService.delete(memberEmail, currentPassword, redirectAttributes); // 비밀번호를 함께 전달
        } catch (IllegalArgumentException e) {
            // 비밀번호 불일치 시 처리
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/myPage/update"; // 비밀번호 불일치 시 마이페이지로 이동
        }

        session.invalidate(); // 회원 탈퇴 후 세션 삭제
        return "redirect:/"; // 메인 페이지로 리디렉션
    }

    /* 이 아래로는 일단 임시로 멤버커트롤러에 다 넣어놨는데 나중에 어느 컨트롤러로 넣을지 생각하기(위에 회원정보 수정부분도 MyPageController 를 따로 만들어서 할지 생각해보기) */

    @Operation(summary = "호텔 예약내역 조회", description = "호텔 예약내역 조회 페이지로 이동한다.")
    @GetMapping("/myPage/reservationList")
    public String reservationList(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";  // 로그인되어있지 않으면 로그인 페이지로 리다이렉트
        }
        return "myPage/reservationList";
    }

    @Operation(summary = "룸서비스", description = "룸서비스 페이지로 이동한다.")
    @GetMapping("/myPage/roomService")
    public String roomService(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";  // 로그인되어있지 않으면 로그인 페이지로 리다이렉트
        }
        return "myPage/roomService";
    }

}
