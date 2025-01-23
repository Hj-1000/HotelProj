package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.DTO.QnaDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Repository.QnaRepository;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.QnaService;
import com.ntt.ntt.Service.ReplyService;
import com.ntt.ntt.Util.PaginationUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class QnaController {

    private final MemberService memberService;
    private final QnaService qnaService;
    private final QnaRepository qnaRepository;
    private final PaginationUtil paginationUtil;
    private final ReplyService replyService;

    // Q&A 페이지 이동
    @GetMapping("/qna")
    public String qnaForm() {
        return null;
    }


    // 질문 목록 페이지로 이동
    @GetMapping("/qna/list")
    public String qnaListPage(
            @RequestParam(defaultValue = "1") int page, // 기본 페이지 번호 1
            @RequestParam(required = false, defaultValue = "") String keyword, // 기본 검색어 빈 문자열
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        // 페이지 처리 및 검색어 적용
        Page<Qna> qnaPage = qnaService.getQnaPage(page, keyword);

        // PaginationUtil을 이용해 페이지네이션 정보 생성
        Map<String, Integer> pagination = paginationUtil.pagination(qnaPage);

        // 모델에 데이터 추가
        model.addAttribute("qnaList", qnaPage.getContent()); // 현재 페이지의 Q&A 리스트
        model.addAttribute("pagination", pagination); // 페이지네이션 정보 추가
        model.addAttribute("keyword", keyword); // 검색 키워드

        // 로그인된 사용자 정보 처리
        if (userDetails != null) {
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member currentMember = dtoToEntity(memberDTO);
            model.addAttribute("currentMember", currentMember); // 현재 로그인된 사용자
        } else {
            model.addAttribute("errorMessage", "로그인 후 이용해주세요.");
        }

        return "qna/list"; // qna/list.html로 이동
    }

    // 질문 작성 페이지로 이동
    @GetMapping("/qna/register")
    public String qnaRegisterPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            model.addAttribute("errorMessage", "로그인 후 질문을 남길 수 있습니다.");
            return "redirect:/login";  // 로그인 페이지로 리다이렉트
        }
        return "qna/register";  // qna/register.html로 넘겨줌
    }

    // 질문 제출 처리
    @PostMapping("/qna/register")
    public String submitQuestionProc(@RequestParam String title,
                                     @RequestParam String content,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     Model model) {
        if (userDetails != null) {
            if (title == null || title.isEmpty() || content == null || content.isEmpty()) {
                model.addAttribute("errorMessage", "제목과 내용은 필수 입력 사항입니다.");
                return "qna/register";  // 오류가 발생한 경우 질문 등록 페이지로 다시 이동
            }

            // 로그인된 사용자 정보 가져오기
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member member = dtoToEntity(memberDTO);

            // QnaDTO 객체 생성
            QnaDTO qnaDTO = new QnaDTO();
            qnaDTO.setQnaTitle(title);
            qnaDTO.setQnaContent(content);

            // QnaService를 사용하여 질문 저장
            qnaService.registerQna(qnaDTO, member);

            return "redirect:/qna/list";  // 질문 목록 페이지로 리다이렉트
        } else {
            model.addAttribute("errorMessage", "로그인 후 질문을 남길 수 있습니다.");
            return "redirect:/login";  // 로그인되지 않으면 로그인 페이지로 리다이렉트
        }
    }

    // 질문 상세보기 페이지로 이동
    @GetMapping("/qna/read/{id}")
    public String readQnaForm(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Qna qna = qnaService.findById(id);

        // 날짜가 null인 경우 처리
        if (qna.getRegDate() == null) {
            qna.setRegDate(LocalDateTime.now());  // 예시로 현재 날짜를 설정
        }

        // 날짜 포맷 처리
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = qna.getRegDate().format(formatter);
        model.addAttribute("formattedRegDate", formattedDate);

        model.addAttribute("qna", qna);

        // 댓글 목록 조회
        model.addAttribute("replies", replyService.readRepliesByQna(id));  // 댓글 목록 추가

        // 로그인된 사용자 정보 처리
        if (userDetails != null) {
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member currentMember = dtoToEntity(memberDTO);
            model.addAttribute("currentMember", currentMember); // 현재 로그인된 사용자
        }

        // 관리자인 경우에만 댓글 작성 폼을 보여줌
        if (userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("canPostReply", true);  // 관리자에게 댓글 작성 권한 부여
        } else {
            model.addAttribute("canPostReply", false); // 일반 사용자에게 댓글 작성 권한 부여 안 함
        }

        return "qna/read"; // qna/read.html로 이동
    }



    // 질문 수정 페이지로 이동
    @GetMapping("/qna/update/{id}")
    public String updateQnaForm(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Qna qna = qnaService.findById(id);
        model.addAttribute("qna", qna);

        // 로그인된 사용자 정보 처리
        if (userDetails != null) {
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member currentMember = dtoToEntity(memberDTO);
            model.addAttribute("currentMember", currentMember); // 현재 로그인된 사용자
        }

        return "qna/update"; // 수정 폼 페이지로 이동
    }



    // Qna 수정 처리
    @PostMapping("/qna/update/{id}")
    public String updateQnaPorc(@PathVariable Integer id, QnaDTO qnaDTO, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member currentMember = dtoToEntity(memberDTO);

            // 현재 사용자가 작성한 Qna인지 확인
            Qna qna = qnaService.findById(id);
            if (qna.getMember() != null && qna.getMember().getMemberId().equals(currentMember.getMemberId())) {
                qnaService.updateQna(id, qnaDTO); // Qna 업데이트 처리
            }
        }
        return "redirect:/qna/list"; // 업데이트 후 Q&A 목록으로 리다이렉트
    }

    // 질문 삭제 처리
    @PostMapping("/qna/delete/{id}")
    public String deleteQnaPorc(@PathVariable("id") Integer id, @AuthenticationPrincipal UserDetails userDetails) {
        Qna qna = qnaRepository.findById(id).orElse(null);
        if (qna != null) {
            // 현재 로그인한 사용자가 해당 질문 작성자인지 확인
            if (userDetails != null && qna.getMember().getMemberId().equals(dtoToEntity(memberService.read(userDetails.getUsername())).getMemberId())) {
                qnaService.deleteQna(id);  // QnaService에서 삭제 처리
                return "redirect:/qna/list";  // 삭제 후 질문 목록 페이지로 리다이렉트
            } else {
                return "redirect:/qna/list";  // 삭제 권한이 없으면 목록 페이지로 리다이렉트
            }
        } else {
            return "redirect:/qna/list";  // 질문이 없으면 목록 페이지로 리다이렉트
        }
    }

    // 댓글 작성 처리
    @PostMapping("/qna/reply/register/{qnaId}")
    public String registerReply(@PathVariable Integer qnaId, String replyContent, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member member = dtoToEntity(memberDTO);

            // 댓글 등록
            replyService.registerReply(replyContent, qnaId, member);
        } else {
            model.addAttribute("errorMessage", "관리자만 댓글을 작성할 수 있습니다.");
        }
        return "redirect:/qna/read/" + qnaId;  // Q&A 상세보기 페이지로 리다이렉트
    }

    // MemberDTO를 Member 엔티티로 변환하는 메소드
    private Member dtoToEntity(MemberDTO memberDTO) {
        Member member = new Member();
        member.setMemberId(memberDTO.getMemberId());
        member.setMemberName(memberDTO.getMemberName());
        member.setMemberEmail(memberDTO.getMemberEmail());
        member.setMemberPhone(memberDTO.getMemberPhone());
        member.setRole(memberDTO.getRole());
        member.setRegDate(memberDTO.getRegDate());
        member.setModDate(memberDTO.getModDate());
        return member;
    }
}
