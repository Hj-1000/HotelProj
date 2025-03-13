package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.ReplyService;
import groovy.util.logging.Log4j2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Log4j2
@AllArgsConstructor
@RequestMapping("/reply")
@Tag(name = "ReplyController", description = "Q&A페이지에 댓글")
public class ReplyController {

    private final ReplyService replyService;
    private final MemberService memberService;

    // 댓글 등록
    @Operation(summary = "댓글등록", description = "ROLE=운영자시 댓글을 등록할수있다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/qna/reply/register/{qnaId}")
    public String registerReplyProc(@PathVariable Integer qnaId, String replyContent, Authentication authentication, RedirectAttributes redirectAttributes) {
        Member member = (Member) authentication.getPrincipal();
        if (member == null) {
            return "errorPage";  // 회원이 없을 경우 처리
        }

        // 댓글 등록
        replyService.registerReply(replyContent, qnaId, member);

        // 리다이렉트 시 qnaId를 URL에 포함시키기 위해 addFlashAttribute 사용
        redirectAttributes.addFlashAttribute("qnaId", qnaId);  // FlashAttribute로 전달

        // Q&A 상세보기 페이지로 리다이렉트
        return "redirect:/qna/read/{qnaId}";
    }

    // 댓글 목록 불러오기
    @Operation(summary = "댓글목록", description = "댓글등록시 상세보기창에서 댓글을 볼수있다.")
    @GetMapping("/list/{qnaId}")
    public String listRepliesForm(@PathVariable Integer qnaId, Model model) {
        List<Reply> replies = replyService.readRepliesByQna(qnaId);
        model.addAttribute("replies", replies);

        Qna qna = replyService.readQnaById(qnaId);
        model.addAttribute("qna", qna);

        return "qna/read";
    }

    // 댓글수정
    @Operation(summary = "댓글수정", description = "댓글 수정시 상세보기페이지에 댓글이 수정된다.")
    @GetMapping("/reply/update/{id}")
    public String updateReplyForm(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Reply reply = replyService.findById(id);
        if (reply == null) {
            model.addAttribute("errorMessage", "댓글을 찾을 수 없습니다.");
            return "errorPage"; // 댓글이 없으면 에러 페이지로 이동
        }

        model.addAttribute("reply", reply);

        if (userDetails != null) {
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member currentMember = dtoToEntity(memberDTO);
            model.addAttribute("currentMember", currentMember);
        }

        return "/reply/update"; // 댓글 수정 폼
    }

    // 댓글 수정 처리
    @Operation(summary = "댓글수정창", description = "수정시 데이터 등록 후 상세보기 페이지로 이동한다.")
    @PostMapping("/update/{id}")
    public String updateReplyProc(@PathVariable("id") Integer replyId, @RequestParam("replyContent") String replyContent) {
        Reply reply = replyService.findById(replyId);
        if (reply != null) {
            replyService.updateReply(replyId, replyContent); // 댓글 수정
        }
        // 댓글 수정 후 해당 Qna 페이지로 리다이렉트
        Integer qnaId = reply.getQna().getQnaId(); // 수정된 댓글이 속한 Qna의 ID
        return "redirect:/qna/read/" + qnaId; // QnA 상세 페이지로 리다이렉트
    }

    // 댓글 삭제 처리
    @Operation(summary = "댓글삭제", description = "등록된 댓글데이터 삭제후 상세보기창 이동한다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{replyId}")
    public String deleteReplyProc(@PathVariable("replyId") Integer replyId, RedirectAttributes redirectAttributes)  {
        Reply reply = replyService.findById(replyId);
        if (reply == null) {
            redirectAttributes.addFlashAttribute("message", "댓글을 찾을 수 없습니다.");
            return "redirect:/qna/list";
        }

        replyService.deleteReply(replyId);

        Integer qnaId = reply.getQna().getQnaId();
        redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");

        return "redirect:/qna/read/" + qnaId;
    }

    private Member dtoToEntity(MemberDTO memberDTO) {
        if (memberDTO == null) {
            return null;
        }
        Member member = new Member();
        member.setMemberId(memberDTO.getMemberId());
        member.setMemberName(memberDTO.getMemberName());
        member.setMemberEmail(memberDTO.getMemberEmail());
        return member;
    }
}
