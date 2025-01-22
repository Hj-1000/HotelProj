package com.ntt.ntt.Repository;


import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    // 특정 질문에 대한 댓글을 찾는 메소드
    List<Reply> findByQna(Qna qna);
}
