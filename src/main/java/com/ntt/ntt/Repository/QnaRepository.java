package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QnaRepository extends JpaRepository<Qna, Integer> {
    // 제목, 내용, 작성자 이름에서 검색
    Page<Qna> findByQnaTitleContainingOrQnaContentContainingOrMemberMemberNameContaining(
            String titleKeyword, String contentKeyword, String memberNameKeyword, Pageable pageable);
}
