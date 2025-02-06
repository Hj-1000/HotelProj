package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    List<Reply> findByQna(Qna qna);

    Optional<Reply> findById(Integer id);

    // Qna에 해당하는 모든 댓글을 삭제
    void deleteByQna(Qna qna);


}