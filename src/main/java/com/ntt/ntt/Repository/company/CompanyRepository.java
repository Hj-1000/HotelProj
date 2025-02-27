package com.ntt.ntt.Repository.company;

import com.ntt.ntt.Entity.Company;
import com.ntt.ntt.Entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Integer> {

    Page<Company> findByCompanyNameLike(String keyword, Pageable pageable);  // 회사명 LIKE 검색
    Page<Company> findByCompanyManagerLike(String keyword, Pageable pageable);  // 관리자명 LIKE 검색
    Page<Company> findByCompanyNameLikeOrCompanyManagerLike(String keyword, String keyword1, Pageable pageable);  // 회사명 OR 관리자명 LIKE 검색

    List<Company> findByMember_MemberId(Integer memberId);

    void deleteByMember(Member member);

    // 회사명 검색 + memberId 필터링
    Page<Company> findByCompanyNameLikeAndMember_MemberId(String companyName, Integer memberId, Pageable pageable);
    // 관리자명 검색 + memberId 필터링
    Page<Company> findByCompanyManagerLikeAndMember_MemberId(String companyManager, Integer memberId, Pageable pageable);
    // 회사명 또는 관리자명 검색 + memberId 필터링
    Page<Company> findByCompanyNameLikeOrCompanyManagerLikeAndMember_MemberId(String companyName, String companyManager, Integer memberId, Pageable pageable);
    // memberId로만 회사 조회
    Page<Company> findByMember_MemberId(Integer memberId, Pageable pageable);

}
