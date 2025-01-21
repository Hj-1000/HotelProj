package com.ntt.ntt.Repository.hotel;

import com.ntt.ntt.Entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {

    Page<Hotel> findByHotelNameLike (String keyword, Pageable pageable); //호텔이름검색
    Page<Hotel> findByHotelAddressLike (String keyword, Pageable pageable); //호텔주소검색
    Page<Hotel> findByHotelLocationLike (String keyword, Pageable pageable); //호텔지역검색
    Page<Hotel> findByHotelRating (Integer keyword, Pageable pageable); //호텔별점검색

}
