package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ReplyDTO;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.QnaRepository;
import com.ntt.ntt.Repository.ReplyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public ReplyService(ReplyRepository replyRepository, QnaRepository qnaRepository, MemberRepository memberRepository) {
        this.replyRepository = replyRepository;
        this.qnaRepository = qnaRepository;
        this.memberRepository = memberRepository;
    }

    // 댓글 등록
    public void registerReply(String replyContent, Member member, Integer qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("Qna를 찾을 수 없습니다"));

        Reply reply = new Reply();
        reply.setReplyContent(replyContent);
        reply.setMember(member);
        reply.setQna(qna);
        reply.setRegDate(LocalDateTime.now());
        reply.setModDate(LocalDateTime.now());

        replyRepository.save(reply);
    }

    // 질문에 대한 댓글 목록 조회
    public List<Reply> readQnalsit(Qna qna) {
        return replyRepository.findByQna(qna);
    }
}
