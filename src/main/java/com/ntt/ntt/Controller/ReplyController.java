package com.ntt.ntt.Controller;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Service.QnaService;
import com.ntt.ntt.Service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final QnaService qnaService;

    //  관리자만 댓글 작성 가능하도록 설정
    @PostMapping("/reply")
    public String postReply(@RequestParam String replyContent, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            // 댓글 작성 로직
        } else {
            // 권한 없음 처리
            return "redirect:/error";  // 권한이 없는 경우
        }
         // 권한 없음 처리 후 로그인 페이지로 리다이렉트
        return "redirect:/login";  // 로그인 페이지로 리다이렉트
    }
    // 댓글 작성
    @PostMapping("/qna/{id}/reply")
    public String postReply(@PathVariable("id") Integer qnaId, @RequestParam String replyContent,
                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            // 로그인한 사용자 정보 가져오기
            Member member = new Member(); // 실제로 Member를 불러오는 로직을 작성해야 합니다.
            member.setMemberEmail(userDetails.getUsername());

            // 댓글 등록
            replyService.registerReply(replyContent, member, qnaId);

            return "redirect:/qna/list";  // 댓글 작성 후 질문 목록 페이지로 리다이렉트
        } else {
            return "redirect:/error";  // 권한 없음 처리
        }
    }
}