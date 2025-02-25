package com.ntt.ntt.Service;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import com.ntt.ntt.Repository.MemberRepository;
import com.ntt.ntt.Repository.QnaRepository;
import com.ntt.ntt.Repository.ReplyRepository;
import groovy.util.logging.Log4j2;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Log4j2
@AllArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final QnaRepository qnaRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;


    // 댓글 등록 (알림 생성 추가)
    public void registerReply(String replyContent, Integer qnaId, Member member) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid QnA ID: " + qnaId));

        Reply reply = new Reply();
        reply.setReplyContent(replyContent);
        reply.setRegDate(LocalDateTime.now());
        reply.setMember(member);
        reply.setQna(qna);

        Reply savedReply = replyRepository.save(reply);
        System.out.println("✅ 저장된 댓글 ID: " + savedReply.getReplyId());

        // ✅ 댓글이 달린 글의 작성자에게만 알림 생성
        if (!qna.getMember().equals(member)) { // 본인이 댓글 단 경우 제외
            String message = member.getMemberName() + " 님이 당신의 Q&A 글에 댓글을 남겼습니다.";
            notificationService.createNotification(qna.getMember(), message, qna);
        }
    }

    // Qna 객체를 ID로 조회
    public Qna readQnaById(Integer qnaId) {
        return qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("Qna not found with id: " + qnaId));
    }

    // 댓글 찾기
    public Reply findById(Integer id) {
        return replyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found with id: " + id));
    }

    // 특정 Q&A에 대한 댓글 목록 조회
    public List<Reply> readRepliesByQna(Integer qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new RuntimeException("Qna not found with id: " + qnaId)); // 예외 처리 추가
        return replyRepository.findByQna(qna);  // Qna를 기준으로 댓글 목록 조회
    }

    // 댓글 수정
    public void updateReply(Integer replyId, String replyContent) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found with id: " + replyId));
        reply.setReplyContent(replyContent);
        reply.setRegDate(LocalDateTime.now());
        replyRepository.save(reply);
    }

    //댓글 삭제
    public void deleteReply(Integer replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));


        replyRepository.delete(reply);
    }


}
