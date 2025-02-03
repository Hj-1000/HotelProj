package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.QnaDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.QnaRepository;
import groovy.util.logging.Log4j2;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@Transactional
@Log4j2
public class QnaService {

    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public QnaService(QnaRepository qnaRepository, MemberRepository memberRepository) {
        this.qnaRepository = qnaRepository;
        this.memberRepository = memberRepository;
    }

    public Page<Qna> getQnaPage(int page, String keyword, String qnaCategory, String memberNameKeyword) {
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc("regDate")));  // 페이지 번호는 1부터 시작하지만, Pageable은 0부터 시작하므로 -1 해줌
        Page<Qna> qnaPage;

        // keyword와 qnaCategory가 모두 null이거나 빈 문자열일 경우 전체 검색
        if ((keyword == null || keyword.isEmpty()) && (qnaCategory == null || qnaCategory.isEmpty())) {
            qnaPage = qnaRepository.findAll(pageable);  // 모든 Q&A 검색
        } else {
            // qnaCategory와 keyword를 포함하여 검색
            qnaPage = qnaRepository.findByQnaCategoryContainingOrQnaTitleContainingOrQnaContentContaining(
                    qnaCategory, keyword, keyword, pageable);
        }

        // 마스킹 처리
        qnaPage.getContent().forEach(qna -> {
            String maskedName = applyNameMasking(qna.getMember().getMemberName());
            qna.setMemberName(maskedName);
        });

        return qnaPage;
    }


    public Qna findById(Integer qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Qna not found with id: " + qnaId));

        String maskedName = applyNameMasking(qna.getMember().getMemberName());
        qna.setMemberName(maskedName);

        return qna;
    }

    public void registerQna(QnaDTO qnaDTO, Member member) {
        Qna qna = new Qna();
        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());
        qna.setQnaCategory(qnaDTO.getQnaCategory());  // 카테고리 추가
        qna.setMember(member);

        String maskedName = applyNameMasking(member.getMemberName());
        qna.setMemberName(maskedName);

        qna.setRegDate(LocalDateTime.now());
        qna.setModDate(LocalDateTime.now());

        qnaRepository.save(qna);
        qnaRepository.flush();
    }

    private String applyNameMasking(String memberName) {
        if (memberName == null || memberName.length() <= 2) return memberName;
        int start = 1;
        int end = memberName.length() - 1;
        return memberName.substring(0, start) + "*" + memberName.substring(end);
    }

    public Qna updateQna(Integer id, QnaDTO qnaDTO) {
        Qna qna = findById(id);
        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());
        qna.setModDate(LocalDateTime.now());

        String maskedName = applyNameMasking(qna.getMember().getMemberName());
        qna.setMemberName(maskedName);

        return qnaRepository.save(qna);
    }

    public void deleteQna(Integer id) {
        Qna qna = findById(id);
        qnaRepository.delete(qna);
    }
}