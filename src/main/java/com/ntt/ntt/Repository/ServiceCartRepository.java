package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.ServiceCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCartRepository extends JpaRepository<ServiceCart, Integer> {
    //특정 회원의 장바구니를 조회
    ServiceCart findByMember_MemberId(Integer memberId);

    //특정회원의 카트를 조회하면서 거기 속한 카트아이템도 함께 가져오는 메서드
    //JOIN FETCH 는 불러온 memberId로 조회한 serviceCart 객체를 조회할 때 serviceCartItemList도 함께 조회함
    @Query("SELECT sc FROM ServiceCart sc JOIN FETCH sc.serviceCartItemList sci WHERE sc.member.memberId = :memberId")
    ServiceCart findCartByMemberId(@Param("memberId") Integer memberId);
}
