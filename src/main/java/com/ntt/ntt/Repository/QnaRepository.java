package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Qna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaRepository extends JpaRepository<Qna, Integer> {
    // Member와 관련된 질문을 찾는 메소드
    Page<Qna> findByMember(Member member, Pageable pageable);

    // 제목으로 질문을 찾는 메소드
    Page<Qna> findByQnaTitleContaining(String keyword, Pageable pageable);

    // 내용으로 질문을 찾는 메소드
    Page<Qna> findByQnaContentContaining(String keyword, Pageable pageable);

    // 회원탈퇴시 해당 회원이 작성한 QnA 삭제하기 위해 추가
    void deleteByMember(Member member);
}
