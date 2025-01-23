package com.ntt.ntt.Controller;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.ReplyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/reply")

public class ReplyController {


    private final ReplyService replyService;
    private final MemberService memberService;

    public ReplyController(ReplyService replyService, MemberService memberService) {
        this.replyService = replyService;
        this.memberService = memberService;
    }



    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/qna/reply/register/{qnaId}")
    public String registerReply(@PathVariable Integer qnaId, String replyContent, Authentication authentication) {
        Member member = (Member) authentication.getPrincipal();
        if (member == null) {
            return "errorPage";  // 회원이 없을 경우 처리
        }

        // 댓글 등록
        replyService.registerReply(replyContent, qnaId, member);

        // Q&A 상세보기 페이지로 리다이렉트
        return "redirect:/qna/read/" + qnaId;
    }

    @GetMapping("/list/{qnaId}")
    public String listReplies(@PathVariable Integer qnaId, Model model) {
        List<Reply> replies = replyService.readRepliesByQna(qnaId);  // 댓글 목록을 가져옴
        model.addAttribute("replies", replies);  // 댓글 목록을 모델에 추가

        // 해당 Qna도 모델에 추가해서 상세 페이지에서 함께 처리
        Qna qna = replyService.readQnaById(qnaId);  // Qna를 서비스에서 가져오기
        model.addAttribute("qna", qna);  // Qna 모델에 추가

        return "qna/read";  // 댓글 목록과 Qna 정보를 포함한 페이지로 리턴
    }
    }