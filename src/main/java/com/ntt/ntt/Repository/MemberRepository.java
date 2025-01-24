package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    Optional<Member> findByMemberEmail(String memberEmail);  // 이메일 기준으로 회원 조회

    Optional<Member> findByMemberName(String memberName);  // 사용자명으로 Member 조회

    // 모든 회원을 조회
    List<Member> findAll();
}
