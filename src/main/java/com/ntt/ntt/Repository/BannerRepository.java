package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {

    // 배너 순위 순으로 정렬된 리스트 반환
    List<Banner> findAllByOrderByBannerRankAsc();

    // 배너 상태에 따라 필터링
    List<Banner> findByBannerStatus(Boolean bannerStatus);

    // 특정 회사 ID로 배너 조회
    List<Banner> findByCompanyCompanyId(Integer companyId);
}
