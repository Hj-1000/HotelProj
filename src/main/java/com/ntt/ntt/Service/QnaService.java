package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.QnaDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.QnaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // 모든 Qna 질문을 가져올 때 Member 정보도 포함해서 반환
    public List<QnaDTO> readAllQna() {
        List<Qna> qnaList = qnaRepository.findAll();  // 모든 Qna 엔티티 가져오기
        List<QnaDTO> qnaDTOList = new ArrayList<>();

        for (Qna qna : qnaList) {
            QnaDTO qnaDTO = new QnaDTO();
            qnaDTO.setQnaTitle(qna.getQnaTitle());
            qnaDTO.setQnaContent(qna.getQnaContent());
            qnaDTO.setRegDate(qna.getRegDate());

            // Member 정보 추가 (작성자 이름 등)
            Member member = qna.getMember();  // Qna에 연관된 Member 가져오기
            qnaDTO.setMemberName(member.getMemberName());  // MemberDTO의 memberName을 설정

            qnaDTOList.add(qnaDTO);
        }
        return qnaDTOList;
    }

    // Qna 질문 등록
    public void registerQna(QnaDTO qnaDTO, Member member) {
        Qna qna = new Qna();
        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());
        qna.setMember(member);

        // 날짜 설정 (등록 날짜 설정)
        qna.setRegDate(LocalDateTime.now());
        qna.setModDate(LocalDateTime.now());  // 처음 등록할 때는 수정일도 동일하게 설정

        qnaRepository.save(qna);
    }

    // Qna 수정
    public Qna updateQna(Integer id, QnaDTO qnaDTO) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qna 를 찾을 수 없습니다."));

        qna.setQnaTitle(qnaDTO.getQnaTitle());
        qna.setQnaContent(qnaDTO.getQnaContent());

        // 수정일자 업데이트
        qna.setModDate(LocalDateTime.now());
        return qnaRepository.save(qna);
    }

    // Qna 삭제
    public void deleteQna(Integer id) {
        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Qna 를 찾을 수 없습니다"));

        qnaRepository.delete(qna);
    }
}
