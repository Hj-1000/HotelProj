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





    // findById 메서드 추가
    public Qna findById(Integer qnaID) {
        return qnaRepository.findById(qnaID)
                .orElseThrow(() -> new EntityNotFoundException("Qna not found with id: " + qnaID));
    }

    public Page<Qna> getQnaPage(Integer page, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, 5, Sort.by(Sort.Order.desc("regDate")));

        if (keyword == null || keyword.isEmpty()) {
            return qnaRepository.findAll(pageable);  // 검색어가 없으면 모든 Q&A를 반환
        } else {
            return qnaRepository.findByQnaTitleContainingOrQnaContentContainingOrMemberMemberNameContaining(
                    keyword, keyword, keyword, pageable );
        }
    }


    // Qna 질문 등록
    public void registerQna(QnaDTO qnaDTO, Member member) {
        Qna qna = new Qna();
        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());
        qna.setMember(member);
        qna.setRegDate(LocalDateTime.now());
        qna.setModDate(LocalDateTime.now());
        qnaRepository.save(qna); // 이 부분에서 데이터베이스에 저장됨
        qnaRepository.flush(); // 즉시 데이터베이스에 반영
    }


    // Qna 수정
    public Qna updateQna(Integer id, QnaDTO qnaDTO) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qna를 찾을 수 없습니다."));
        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());
        qna.setModDate(LocalDateTime.now());
        return qnaRepository.save(qna);
    }

    // Qna 삭제
    public void deleteQna(Integer id) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qna를 찾을 수 없습니다."));
        qnaRepository.delete(qna);
    }
}