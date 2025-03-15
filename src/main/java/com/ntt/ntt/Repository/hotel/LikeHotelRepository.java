package com.ntt.ntt.Repository.hotel;


import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.LikeHotel;
import com.ntt.ntt.Entity.Likes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LikeHotelRepository extends JpaRepository<LikeHotel, Integer> {

    // 특정 장바구니(Likes)에 특정 호텔이 이미 있는지 확인
    Optional<LikeHotel> findByLikesAndHotel(Likes likes, Hotel hotel);

    // 특정 회원의 찜한 호텔을 삭제하는 메소드 (JPQL 사용)
    @Modifying
    @Transactional
    @Query("DELETE FROM LikeHotel lh WHERE lh.hotel.hotelId = :hotelId AND lh.likes.member.memberId = :memberId")
    void deleteByHotel_HotelIdAndMember_MemberId(@Param("hotelId") Integer hotelId, @Param("memberId") Integer memberId);

    Optional<LikeHotel> findByHotel_HotelIdAndLikes_Member_MemberId(Integer hotelHotelId, Integer likesMemberMemberId);

//    List<LikeHotel> findByHotel_HotelId(Integer hotelId);

    Page<LikeHotel> findByLikes_Member_MemberEmail(String email, Pageable pageable);

//    void deleteByLikes_Member_MemberEmail(String email);

//    void deleteByHotel_HotelId(Integer hotelId);


}
