package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.QnaDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.QnaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QnaService {

    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public QnaService(QnaRepository qnaRepository, MemberRepository memberRepository) {
        this.qnaRepository = qnaRepository;
        this.memberRepository = memberRepository;
    }

    // 질문 목록 페이징 처리 및 검색
    public Page<Qna> getQnaPage(int page, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, 10);  // page는 1부터 시작하므로 1을 빼서 처리
        Page<Qna> qnaPage;

        if (keyword == null || keyword.isEmpty()) {
            qnaPage = qnaRepository.findAll(pageable);  // 검색어가 없으면 전체 목록
        } else {
            qnaPage = qnaRepository.findByQnaTitleContainingOrQnaContentContaining(keyword, keyword, pageable);  // 검색어로 필터링
        }

        // Qna 객체에 대해 마스킹 처리된 이름을 설정
        qnaPage.getContent().forEach(qna -> {
            String maskedName = applyNameMasking(qna.getMember().getMemberName());
            qna.setMemberName(maskedName);  // 마스킹된 이름을 설정
        });

        return qnaPage;
    }

    public Qna findById(Integer qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Qna not found with id: " + qnaId));

        // 마스킹된 이름 처리
        String maskedName = applyNameMasking(qna.getMember().getMemberName());
        qna.setMemberName(maskedName);  // 마스킹된 이름을 설정

        System.out.println("Masked Name: " + qna.getMemberName()); // 마스킹된 이름 출력

        return qna;
    }



    // 이름만 마스킹 처리
    private String applyNameMasking(String memberName) {
        if (memberName == null || memberName.length() <= 2) return memberName;  // 이름이 두 글자 이하일 경우 마스킹하지 않음
        int start = 1;  // 첫 글자는 그대로 두기
        int end = memberName.length() - 1;  // 마지막 글자는 그대로 두기
        return memberName.substring(0, start) + "*" + memberName.substring(end);  // 가운데 부분 마스킹
    }

    // Qna 질문 등록 (이름만 마스킹 적용)
    public void registerQna(QnaDTO qnaDTO, Member member) {
        Qna qna = new Qna();
        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());
        qna.setMember(member);

        // 마스킹된 이름 처리
        String maskedName = applyNameMasking(member.getMemberName());
        qna.setMemberName(maskedName);  // 마스킹된 이름을 설정

        qna.setRegDate(LocalDateTime.now());
        qna.setModDate(LocalDateTime.now());

        // DB에 저장
        qnaRepository.save(qna);
        qnaRepository.flush();  // 즉시 데이터베이스에 반영

        // 마스킹된 이름 확인 출력
        System.out.println("Masked Name: " + maskedName);
    }

    // Qna 수정 (이름만 마스킹 적용)
    public Qna updateQna(Integer id, QnaDTO qnaDTO) {
        Qna qna = findById(id); // findById 메서드를 사용하여 Qna 객체를 찾음
        qna.setQnaTitle(qnaDTO.getQnaTitle());  // 제목은 그대로
        qna.setQnaContent(qnaDTO.getQnaContent());  // 내용은 그대로
        qna.setModDate(LocalDateTime.now());

        // 회원의 이름 마스킹 처리 후 저장
        String maskedName = applyNameMasking(qna.getMember().getMemberName());
        qna.setMemberName(maskedName);  // 수정된 이름을 마스킹하여 저장

        return qnaRepository.save(qna);  // 수정 후 저장
    }

    // Qna 삭제
    public void deleteQna(Integer id) {
        Qna qna = findById(id); // findById 메서드를 사용하여 Qna 객체를 찾음
        qnaRepository.delete(qna); // 삭제 처리
    }
}