package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    Optional<Member> findByMemberEmail (String email);



    void deleteByMemberEmail(String memberEmail);

    // 모든 회원을 조회하는 메서드
    List<Member> findAll();
}
