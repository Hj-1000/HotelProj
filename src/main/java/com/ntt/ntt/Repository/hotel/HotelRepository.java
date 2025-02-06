package com.ntt.ntt.Repository.hotel;

import com.ntt.ntt.Entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {

    // 특정회사 X
    Page<Hotel> findByHotelNameLike (String keyword, Pageable pageable); //호텔이름검색
    Page<Hotel> findByHotelAddressLike (String keyword, Pageable pageable); //호텔주소검색
    Page<Hotel> findByHotelRating (Integer keyword, Pageable pageable); //호텔별점검색
    Page<Hotel> findByHotelLocationLike (String keyword, Pageable pageable); //호텔위치검색

    // 정확히 일치하는 location을 찾는 메서드로 변경
    Page<Hotel> findByHotelLocationEquals(String keyword, Pageable pageable);

    // 주어진 회사 ID에 해당하는 호텔 수를 카운트하는 메서드
    Integer countByCompany_CompanyId(Integer companyId);

    //회사 ID를 토대로 회사명을 가져오는 메서드
    @Query("SELECT h FROM Hotel h JOIN h.company c WHERE h.company.companyId = :companyId")
    Page<Hotel> findByCompany_CompanyIdWithCompanyName(Integer companyId, Pageable pageable);

    // 회사 ID에 해당하는 호텔 조회 (이름 포함)
    Page<Hotel> findByCompany_CompanyIdAndHotelNameLike(Integer companyId, String keyword, Pageable pageable);

    // 회사 ID에 해당하는 호텔 조회 (지역 포함)
    Page<Hotel> findByCompany_CompanyIdAndHotelLocationLike(Integer companyId, String keyword, Pageable pageable);

    // 회사 ID에 해당하는 호텔 조회 (주소 포함)
    Page<Hotel> findByCompany_CompanyIdAndHotelAddressLike(Integer companyId, String keyword, Pageable pageable);

    // 회사 ID에 해당하는 호텔 조회 (별점 포함)
    Page<Hotel> findByCompany_CompanyIdAndHotelRating(Integer companyId, Integer rating, Pageable pageable);

    Page<Hotel> findByCompany_CompanyId(Integer companyId, Pageable pageable);
}
