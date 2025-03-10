package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Entity.ServiceCate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCateRepository extends JpaRepository<ServiceCate, Integer> {

    //Admin용
    Page<ServiceCate> findByServiceCateNameLikeAndHotel_HotelId(String keyword,Integer hotelId, Pageable pageable);
    Page<ServiceCate> findByServiceCateNameLike(String keyword,Pageable pageable);
    Page<ServiceCate> findByHotel_HotelId(Integer hotelId, Pageable pageable);

    //CHIEF용
    Page<ServiceCate> findByServiceCateNameLikeAndHotel_Member_MemberId(String keyword, Integer memberId,Pageable pageable);

    // hotelId 기준 검색
    Page<ServiceCate> findByHotel_HotelIdAndHotel_Member_MemberId(Integer hotelId, Integer memberId,Pageable pageable);

    Page<ServiceCate> findByHotel_HotelIdAndHotel_Company_Member_MemberId(Integer hotelId, Integer memberId, Pageable pageable);


    List<ServiceCate> findByHotel_HotelIdAndHotel_Member_MemberId(Integer hotelId, Integer memberId);

    // 검색어 + hotelId 기준 검색 manager용
    Page<ServiceCate> findByServiceCateNameLikeAndHotel_HotelIdAndHotel_Member_MemberId(String keyword, Integer hotelId, Integer memberId,Pageable pageable);
    //CHIEF용
    Page<ServiceCate> findByServiceCateNameLikeAndHotel_HotelIdAndHotel_Company_Member_MemberId(String keyword, Integer hotelId, Integer memberId,Pageable pageable);


    // memberId로만 카테고리 조회 manager 용
    Page<ServiceCate> findByHotel_Member_MemberId(Integer memberId, Pageable pageable);
    //  chief용
    Page<ServiceCate> findByHotel_Company_Member_MemberId(Integer memberId, Pageable pageable);

    // keyword + memberId 필터링
    Page<ServiceCate> findByServiceCateNameLikeAndHotel_Company_Member_MemberId(String serviceCateName, Integer memberId, Pageable pageable);


    //user용
    List<ServiceCate> findByHotel_HotelId(Integer hotelId);

}
