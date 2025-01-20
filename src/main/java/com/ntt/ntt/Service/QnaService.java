package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.QnaDTO;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Repository.QnaRepository;
import com.ntt.ntt.Repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class QnaService {

    /*  필요기능 : 글쓰기 글목록 글조회 글수정 글삭제 페이징처리
     생성 : register
     조회 : read
     수정 : update
     삭제 : delete
     */

    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public QnaService(QnaRepository qnaRepository, MemberRepository memberRepository) {
        this.qnaRepository = qnaRepository;
        this.memberRepository = memberRepository;
    }

    // 전체 Qna 리스트 조회 (페이징 처리 포함)
    public Page<Qna> readAllQna(Pageable pageable) {
        return qnaRepository.findAll(pageable);
    }

    // 제목으로 Qna 조회
    public Page<Qna> readTitle(String keyword, Pageable pageable) {
        return qnaRepository.findByQnaTitleContaining(keyword, pageable);
    }

    // 내용으로 Qna 조회
    public Page<Qna> readContent(String keyword, Pageable pageable) {
        return qnaRepository.findByQnaContentContaining(keyword, pageable);
    }

    // Qna 생성
    public Qna registerQna(QnaDTO qnaDTO) {
        Qna qna = new Qna();
        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());
        return qnaRepository.save(qna);
    }

    // Qna 단일 조회 (ID로 조회)
    public Qna readQna(Integer id) {
        return qnaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Qna 를 찾을수 없습니다."));
    }

    // Qna 수정
    public Qna updateQna(Integer id, QnaDTO qnaDTO) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qna 를 찾을수 없습니다."));

        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());
        return qnaRepository.save(qna);
    }

    // Qna 삭제
    public void deleteQna(Integer id) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qna 를 찾을수 없습니다"));

        qnaRepository.delete(qna);
    }

    // 특정 회원에 속한 Qna 리스트 조회
    public Page<Qna> getMemberQnas(Integer memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member를 찾을 수 없습니다"));

        return qnaRepository.findByMember(member, pageable); // Member에 속한 Qna 리스트를 조회
    }

}
