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

    public Page<Qna> getQnaPage(int page, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<Qna> qnaPage;

        if (keyword == null || keyword.isEmpty()) {
            qnaPage = qnaRepository.findAll(pageable);
        } else {
            qnaPage = qnaRepository.findByQnaTitleContainingOrQnaContentContaining(keyword, keyword, pageable);
        }

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