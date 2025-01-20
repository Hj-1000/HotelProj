package com.ntt.ntt.Repository;


import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    // 변수 이름을 qna 라 설정               qna에 qna아이디
    @Query("SELECT r FROM Reply r WHERE r.qna.qnaId = :qnaId")
    List<Reply> findByqnaId(Qna qna);
}
