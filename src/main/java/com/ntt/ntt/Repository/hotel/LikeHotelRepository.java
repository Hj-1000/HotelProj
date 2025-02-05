package com.ntt.ntt.Repository.hotel;


import com.ntt.ntt.Entity.LikeHotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeHotelRepository extends JpaRepository<LikeHotel, Integer> {


    List<LikeHotel> findByHotel_HotelId(Integer hotelId);

    List<LikeHotel> findByLikes_Member_MemberEmail(String email);

    void deleteByLikes_Member_MemberEmail(String email);

    void deleteByHotel_HotelId(Integer hotelId);


}
