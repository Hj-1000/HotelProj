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




    // 제목, 내용, 질문 유형으로 검색
    Page<Qna> findByQnaCategoryContainingOrQnaTitleContainingOrQnaContentContaining(
            String qnaCategory, String qnaTitle, String qnaContent, Pageable pageable);



       // Qna를 ID로 조회하는 메서드 (JPA에서 자동으로 제공됨)
                Optional<Qna> findById(Integer id);

    //오류

    // 회원탈퇴시 해당 회원이 작성한 QnA 삭제하기 위해 추가
      void deleteByMember(Member member);

}


