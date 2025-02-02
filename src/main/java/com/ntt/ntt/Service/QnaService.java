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

    public Page<Qna> getQnaPage(int page, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, 10);  // page는 1부터 시작하므로 1을 빼서 처리
        if (keyword == null || keyword.isEmpty()) {
            return qnaRepository.findAll(pageable);  // 검색어가 없으면 전체 목록
        } else {
            return qnaRepository.findByQnaTitleContainingOrQnaContentContaining(keyword, keyword, pageable);  // 검색어로 필터링
        }
    }



    // Qna ID로 찾는 메서드
    public Qna findById(Integer qnaID) {
        Optional<Qna> qnaOptional = qnaRepository.findById(qnaID);
        if (qnaOptional.isPresent()) {
            return qnaOptional.get();
        } else {
            throw new EntityNotFoundException("Qna not found with id: " + qnaID);
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
        // id로 Qna를 찾음
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qna를 찾을 수 없습니다."));

        // 수정할 내용 설정
        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());
        qna.setModDate(LocalDateTime.now());

        // 수정 후 다시 저장 (id는 변경되지 않음)
        return qnaRepository.save(qna);  // 이미 존재하는 엔티티를 저장하므로 id 변경 없음
    }

    // Qna 삭제
    public void deleteQna(Integer id) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qna를 찾을 수 없습니다."));
        qnaRepository.delete(qna);
    }
}