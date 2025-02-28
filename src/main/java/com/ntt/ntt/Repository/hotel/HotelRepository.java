package com.ntt.ntt.Repository.hotel;

import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    // 회사 ID에 해당하는 호텔 조회 (이름 포함)
    Page<Hotel> findByCompany_CompanyIdAndHotelNameLike(Integer companyId, String keyword, Pageable pageable);

    // 회사 ID에 해당하는 호텔 조회 (지역 포함)
    Page<Hotel> findByCompany_CompanyIdAndHotelLocationLike(Integer companyId, String keyword, Pageable pageable);

    // 회사 ID에 해당하는 호텔 조회 (주소 포함)
    Page<Hotel> findByCompany_CompanyIdAndHotelAddressLike(Integer companyId, String keyword, Pageable pageable);

    // 회사 ID에 해당하는 호텔 조회 (별점 포함)
    Page<Hotel> findByCompany_CompanyIdAndHotelRating(Integer companyId, Integer rating, Pageable pageable);

    Page<Hotel> findByCompany_CompanyId(Integer companyId, Pageable pageable);

    List<Hotel> findByCompany_CompanyId(Integer companyId);

    @Query("SELECT h FROM Hotel h WHERE h.company.companyId = :companyId")
    List<Hotel> findByCompanyId(@Param("companyId") Integer companyId);

    List<Hotel> findByMember(Member member);

    void deleteByMember(Member member);

    List<Hotel> findByHotelEmail(String email);

    @Query("SELECT h FROM Hotel h WHERE h.member.memberId = :memberId")
    Page<Hotel> findByMember_MemberId(@Param("memberId") Integer memberId, Pageable pageable);

    Page<Hotel> findByMember_MemberIdAndHotelNameLike(Integer memberId, String hotelName, Pageable pageable);
    Page<Hotel> findByMember_MemberIdAndHotelLocationLike(Integer memberId, String hotelLocation, Pageable pageable);
    Page<Hotel> findByMember_MemberIdAndHotelAddressLike(Integer memberId, String hotelAddress, Pageable pageable);
    Page<Hotel> findByMember_MemberIdAndHotelRating(Integer memberId, Integer hotelRating, Pageable pageable);

    @Query("SELECT h.hotelId FROM Hotel h WHERE h.member.memberId = :memberId")
    List<Integer> findHotelIdsByMemberId(@Param("memberId") Integer memberId);
}
