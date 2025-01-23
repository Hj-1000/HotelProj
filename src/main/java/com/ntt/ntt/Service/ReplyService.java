package com.ntt.ntt.Service;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.QnaRepository;
import com.ntt.ntt.Repository.ReplyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional

public class ReplyService {

    private final ReplyRepository replyRepository;
    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    public ReplyService(ReplyRepository replyRepository, QnaRepository qnaRepository, MemberRepository memberRepository) {
        this.replyRepository = replyRepository;
        this.qnaRepository = qnaRepository;
        this.memberRepository = memberRepository;
    }

    // 댓글 등록
    public void registerReply(String replyContent, Integer qnaId, Member member) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid QnA ID: " + qnaId));
        Reply reply = new Reply();
        reply.setReplyContent(replyContent);
        reply.setRegDate(LocalDateTime.now());
        reply.setMember(member);
        reply.setQna(qna);
        replyRepository.save(reply);
    }

    // Qna 객체를 ID로 조회
    public Qna readQnaById(Integer qnaId) {
        return qnaRepository.findById(qnaId).orElseThrow(() -> new IllegalArgumentException("Qna not found"));
    }

    // 특정 Q&A에 대한 댓글 목록 조회
    public List<Reply> readRepliesByQna(Integer qnaId) {
        Qna qna = qnaRepository.findById(qnaId).orElseThrow(() -> new RuntimeException("찾을 수 없는 글입니다."));
        return replyRepository.findByQna(qna);  // Qna를 기준으로 댓글 목록 조회
    }
}