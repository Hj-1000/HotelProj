package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.DTO.NotificationDTO;
import com.ntt.ntt.DTO.QnaDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Notification;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import com.ntt.ntt.Repository.NotificationRepository;
import com.ntt.ntt.Repository.QnaRepository;
import com.ntt.ntt.Service.MemberService;
import com.ntt.ntt.Service.NotificationService;
import com.ntt.ntt.Service.QnaService;
import com.ntt.ntt.Service.ReplyService;
import com.ntt.ntt.Util.PaginationUtil;
import groovy.util.logging.Log4j2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
@Tag(name = "qnaController", description = "Q&A페이지")
public class QnaController {

    private final MemberService memberService;
    private final QnaService qnaService;
    private final QnaRepository qnaRepository;
    private final PaginationUtil paginationUtil;
    private final ReplyService replyService;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;


    // Q&A 페이지 이동
    @Operation(summary = "메인페이지", description = "Q&A 페이지로 이동한다.")
    @GetMapping("/qna")
    public String qnaForm() {
        return null;
    }

    @PostMapping("/add")
    public String addQnaProc(@ModelAttribute Qna qna, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            // 로그인된 사용자의 정보를 얻어옴
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member member = dtoToEntity(memberDTO);

            // Qna 객체에 회원 정보 설정
            qna.setMember(member);  // 반드시 Member 정보가 설정되었는지 확인
            qnaService.save(qna);  // Q&A 저장
        }
        return "redirect:/qna/list";
    }

    // Qna 목록 조회
    @GetMapping("/qna/list")
    public String qnaListPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String qnaCategory,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        // 모든 Q&A 게시글 가져오기 (role 상관없이)
        Page<Qna> qnaPage = qnaService.getQnaPage(page, keyword, qnaCategory);

        // 페이지네이션 정보 계산
        Map<String, Integer> pagination = paginationUtil.pagination(qnaPage);
        int totalPages = qnaPage.getTotalPages();
        int currentPage = pagination.get("currentPage");

        int startPage = Math.max(1, currentPage - 4);
        int endPage = Math.min(startPage + 9, totalPages);

        pagination.put("startPage", startPage);
        pagination.put("endPage", endPage);

        model.addAttribute("qnaList", qnaPage.getContent());
        model.addAttribute("pagination", pagination);
        model.addAttribute("keyword", keyword);
        model.addAttribute("qnaCategory", qnaCategory);

        // 알림 목록 추가
        if (userDetails != null) {
            List<NotificationDTO> notifications = notificationService.getAllNotifications();
            model.addAttribute("notifications", notifications);
        }

        // 로그인된 사용자 정보 추가
        if (userDetails != null) {
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member currentMember = dtoToEntity(memberDTO);
            model.addAttribute("currentMember", currentMember);
        } else {
            model.addAttribute("errorMessage", "로그인 후 이용해주세요.");
        }

        return "qna/list";
    }

    // 질문 작성 페이지로 이동
    @Operation(summary = "등록폼", description = "등록폼 페이지로 이동한다.")
    @GetMapping("/qna/register")
    public String qnaRegisterPageForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            model.addAttribute("errorMessage", "로그인 후 질문을 남길 수 있습니다.");
            return "redirect:/login";  // 로그인 페이지로 리다이렉트
        }

        // 카테고리 목록 추가
        model.addAttribute("categories", Arrays.asList("일반 질문", "호텔 관련", "서비스 관련", "계정 관련", "기타"));
        return "qna/register";  // qna/register.html로 넘겨줌
    }

    // 질문 제출 처리
    @Operation(summary = "등록창", description = "데이터 등록 후 리스트 페이지로 이동한다.")
    @PostMapping("/qna/register")
    public String submitQuestionProc(@RequestParam String title,
                                     @RequestParam String content,
                                     @RequestParam String qnaCategory,
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
            qnaDTO.setQnaCategory(qnaCategory);  // 카테고리 설정
            Qna qna = qnaService.registerQna(qnaDTO, member); // Qna 객체 생성

            // 🔹 알림 생성 (관리자에게 알림 보내기)
            notificationService.createNotification(member, "새로운 Q&A가 등록되었습니다.", qna);

            return "redirect:/qna/list";  // 질문 목록 페이지로 리다이렉트
        } else {
            model.addAttribute("errorMessage", "로그인 후 질문을 남길 수 있습니다.");
            return "redirect:/login";  // 로그인되지 않으면 로그인 페이지로 리다이렉트
        }
    }

    // 질문 상세보기 페이지로 이동
    @Operation(summary = "상세보기창", description = "상세보기창으로 이동한다.")
    @GetMapping("/qna/read/{id}")
    public String readQnaForm(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        // id가 null이거나 유효하지 않으면 400 오류를 처리하거나 리다이렉트
        if (id == null) {
            return "redirect:/error"; // 예시로 에러 페이지로 리다이렉트
        }

        Qna qna = qnaService.findById(id); // 이미 마스킹된 이름이 포함됨

        // Qna가 null일 경우에도 에러 페이지로 리다이렉트 처리
        if (qna == null) {
            return "redirect:/error"; // Qna가 존재하지 않으면 에러 페이지로 리다이렉트
        }


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
    @Operation(summary = "수정폼", description = "질문을 수정할수 있다.")
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
    @Operation(summary = "수정창", description = "데이터 수정 후 목록페이지로 이동한다.")
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
    @Operation(summary = "삭제", description = "데이터를 삭제할수있다.")
    public String deleteQnaPorc(@PathVariable("id") Integer id, @AuthenticationPrincipal UserDetails userDetails) {
        // 질문 찾기
        Qna qna = qnaRepository.findById(id).orElse(null);
        if (qna != null) {
            // 로그인된 사용자 정보가 없으면 리스트로 리다이렉트
            if (userDetails == null) {
                return "redirect:/qna/list";  // 로그인 후 다시 시도하도록 유도
            }

            // 작성자가 존재하는지 확인
            if (qna.getMember() != null && qna.getMember().getMemberId().equals(dtoToEntity(memberService.read(userDetails.getUsername())).getMemberId())) {
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
    @Operation(summary = "댓글등록", description = "상세보기 창에서 댓글을 등록한다.")
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

    // 댓글 수정
    @GetMapping("/reply/update/{id}")
    @Operation(summary = "댓글수정폼", description = "댓글을 수정한다.")
    public String updateReply(@PathVariable Integer id, Model model) {
        Reply reply = replyService.findById(id);
        model.addAttribute("reply", reply);
        return "/reply/update"; // 댓글 수정 폼으로 이동
    }

    // 댓글 삭제
    @GetMapping("/reply/delete/{id}")
    @Operation(summary = "댓글삭제", description = "댓글삭제 후 해당 상세보기 페이지로 이동한다.")
    public String deleteReply(@PathVariable Integer id) {
        replyService.deleteReply(id);
        return "redirect:/qna/read/{qnaId}"; // 댓글 삭제 후 Q&A 페이지로 리디렉션
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
