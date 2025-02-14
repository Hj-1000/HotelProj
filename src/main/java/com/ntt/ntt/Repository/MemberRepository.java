package com.ntt.ntt.Repository;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    Optional<Member> findByMemberEmail(String memberEmail);  // 이메일 기준으로 회원 조회

    @Query("SELECT m FROM Member m WHERE m.memberEmail = :memberEmail")
    Member findByEmail(@Param("memberEmail") String memberEmail);

    Optional<Member> findByRole(Role role);

    // 모든 회원을 조회
    List<Member> findAll();

    @Query("SELECT m.memberId FROM Member m WHERE m.memberId IN (SELECT h.member.memberId FROM Hotel h WHERE h.hotelId = :hotelId)")
    List<Long> findMemberIdsByHotel(@Param("hotelId") Integer hotelId);

    boolean existsById(Integer memberId); // memberId로 존재 여부를 확인
}
