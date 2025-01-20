package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaRepository extends JpaRepository<Qna, Integer> {
    Page<Qna> findByMember(Member member, Pageable pageable);

    // 제목으로 Qna 검색
    Page<Qna> findByQnaTitleContaining(String keyword, Pageable pageable);

    // 내용으로 Qna 검색
    Page<Qna> findByQnaContentContaining(String keyword, Pageable pageable);
}
