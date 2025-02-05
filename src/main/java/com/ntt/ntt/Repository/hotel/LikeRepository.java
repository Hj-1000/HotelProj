package com.ntt.ntt.Repository.hotel;

import com.ntt.ntt.Entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Likes, Integer> {


    Likes findByMember_MemberEmail(String email);

    void deleteByMember_MemberEmail(String email);


}
