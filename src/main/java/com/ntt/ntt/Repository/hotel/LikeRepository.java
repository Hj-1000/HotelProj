package com.ntt.ntt.Repository.hotel;

import com.ntt.ntt.Entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Likes, Integer> {


    // memberId로 저장된 즐겨찾기 객체 가져오기
    // 장바구니의 id를 likeHotel 참조
    Likes findByMember_MemberId (Integer memberId);


}
