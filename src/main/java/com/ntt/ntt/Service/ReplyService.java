package com.ntt.ntt.Service;

import com.ntt.ntt.DTO.ReplyDTO;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import com.ntt.ntt.Repository.QnaRepository;
import com.ntt.ntt.Repository.ReplyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final QnaRepository qnaRepository;

    @Autowired
    public ReplyService(ReplyRepository replyRepository, QnaRepository qnaRepository) {
        this.replyRepository = replyRepository;
        this.qnaRepository = qnaRepository;
    }

    // Reply 리스트 조회 (특정 Qna에 대한 답변 조회)
    public List<Reply> readReplylist (Integer qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Qna ID " + qnaId + "를 찾을 수 없습니다."));
        return replyRepository.findByqnaId(qna);
    }

    // Reply 조회
    public Reply readReply(Integer id) {
        return replyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reply ID " + id + "를 찾을 수 없습니다."));
    }

    // Reply 생성
    public Reply registerReply(ReplyDTO replyDTO) {
        Qna qna = qnaRepository.findById(replyDTO.getQnaId().getQnaId())
                .orElseThrow(() -> new EntityNotFoundException("Qna ID " + replyDTO.getQnaId().getQnaId() + "를 찾을 수 없습니다."));

        Reply reply = Reply.builder()
                .replyContent(replyDTO.getReplyContent())
                .qna(qna)
                .build();

        return replyRepository.save(reply);
    }

    // Reply 수정
    public Reply updateReply(Integer id, ReplyDTO replyDTO) {
        Reply existingReply = replyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reply ID " + id + "를 찾을 수 없습니다."));

        existingReply.setReplyContent(replyDTO.getReplyContent());
        return replyRepository.save(existingReply);
    }

    // Reply 삭제
    public void deleteReply(Integer id) {
        Reply existingReply = replyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reply ID " + id + "를 찾을 수 없습니다."));

        replyRepository.delete(existingReply);
    }
}
