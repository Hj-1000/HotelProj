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
}
