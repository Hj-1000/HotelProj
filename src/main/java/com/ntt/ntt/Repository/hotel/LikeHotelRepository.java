package com.ntt.ntt.Repository.hotel;

import com.ntt.ntt.DTO.LikeDetailDTO;
import com.ntt.ntt.Entity.LikeHotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LikeHotelRepository extends JpaRepository<LikeHotel, Integer> {

    LikeHotel findByLikes_LikesIdAndHotel_HotelId (Integer likesId, Integer hotelId);
    //이미 호텔이 즐겨찾기에 있다면 add를 해주기 위해서 findByLikes_LikesIdAndHotel_HotelI


    // 즐겨찾기 페이지에 전달할 LikeDetailDTO 리스트를 쿼리 하나로 조회하는 jpQL문
    //Long cartItemId, String itemNm, int price, int count, String imgUrl
    @Query("select new com.ntt.ntt.DTO.LikeDetailDTO(lh.likeHotelId, lh.hotel.hotelId, h.hotelName, i.imagePath, h.hotelCheckIn, h.hotelLocation) " +
            "from LikeHotel lh , Image i join lh.hotel h " +
            "where lh.likes.likesId = :likesId and i.hotel.hotelId = lh.hotel.hotelId ")
    Page<LikeDetailDTO> likeList(Integer likesId, Pageable pageable);


}
