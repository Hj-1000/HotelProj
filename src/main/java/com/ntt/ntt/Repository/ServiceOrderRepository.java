package com.ntt.ntt.Repository;


import com.ntt.ntt.Entity.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Integer> {
    // 특정 회원의 주문 목록을 조회
    List<ServiceOrder> findByMember_MemberId(Integer memberId);

    List<ServiceOrder> findByServiceOrderStatus(String serviceOrderStatus);
}
