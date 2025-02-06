package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.ServiceCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCartRepository extends JpaRepository<ServiceCart, Integer> {
    //사용자의 email을 통해서 장바구니 찾기
    //Member member = findByEmail(memberEmail);
    //serviceCartRepository.findByMemberId(memberId)

    public ServiceCart findByMember_MemberId(Integer memberID);

    public ServiceCart findByMember_MemberEmail(String memberEmail);
}
