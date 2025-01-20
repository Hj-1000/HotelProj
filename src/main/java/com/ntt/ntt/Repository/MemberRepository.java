package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    Optional<Member> findByMemberEmail (String email);

    void deleteByMemberEmail(String memberEmail);
}
