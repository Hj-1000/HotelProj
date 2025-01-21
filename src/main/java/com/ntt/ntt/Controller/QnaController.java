package com.ntt.ntt.Controller;

import com.ntt.ntt.DTO.QnaDTO;
import com.ntt.ntt.Service.QnaService;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.DTO.MemberDTO;
import com.ntt.ntt.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class QnaController {

    private final MemberService memberService;
    private final QnaService qnaService;

    // 질문 작성 페이지로 이동
    @GetMapping("/qna")
    public String qnaForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            // 로그인된 사용자의 정보를 가져옴
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());  // MemberDTO 가져오기
            Member member = convertToEntity(memberDTO);  // MemberDTO를 Member 엔티티로 변환
            model.addAttribute("currentMember", member); // 모델에 추가
        } else {
            return "redirect:/login";  // 로그인되지 않으면 로그인 페이지로 리다이렉트
        }
        return "qna"; // qna.html로 넘겨줌
    }

    // 질문 제출 처리
    @PostMapping("/qna")
    public String submitQuestion(@RequestParam(required = false) String title,
                                 @RequestParam(required = false) String content,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (userDetails != null) {
            if (title == null || title.isEmpty() || content == null || content.isEmpty()) {
                model.addAttribute("errorMessage", "제목과 내용은 필수 입력 사항입니다.");
                return "qna";  // 오류가 발생한 경우 질문 페이지로 다시 이동
            }

            // 로그인된 사용자 정보 가져오기
            MemberDTO memberDTO = memberService.read(userDetails.getUsername());
            Member member = convertToEntity(memberDTO);

            // QnaDTO 객체 생성
            QnaDTO qnaDTO = new QnaDTO();
            qnaDTO.setQnaTitle(title);
            qnaDTO.setQnaContent(content);

            // QnaService를 사용하여 질문 저장
            qnaService.registerQna(qnaDTO, member);

            // 질문이 저장된 후 목록 갱신
            List<QnaDTO> qnaList = qnaService.readAllQna();
            model.addAttribute("qnaList", qnaList);

            // 성공적으로 저장된 후 qna 목록 페이지로 이동
            return "qna";  // qna.html로 넘겨줌
        } else {
            model.addAttribute("errorMessage", "로그인 후 질문을 남길 수 있습니다.");
            return "redirect:/login";  // 로그인되지 않으면 로그인 페이지로 리다이렉트
        }
    }
    @GetMapping("/qna/list")
    public String qnaList(Model model) {
        List<QnaDTO> qnaList = qnaService.readAllQna();

        // 로그로 확인
        System.out.println("Qna List: " + qnaList);

        model.addAttribute("qnaList", qnaList);
        return "qna";  // qna.html로 넘겨줌
    }


    // MemberDTO를 Member 엔티티로 변환하는 메소드
    private Member convertToEntity(MemberDTO memberDTO) {
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
