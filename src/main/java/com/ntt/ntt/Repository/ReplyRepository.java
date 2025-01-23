package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Qna;
import com.ntt.ntt.Entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    List<Reply> findByQna(Qna qna);
}