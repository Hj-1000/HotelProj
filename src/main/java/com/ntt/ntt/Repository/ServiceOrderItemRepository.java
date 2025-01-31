package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.ServiceOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOrderItemRepository extends JpaRepository<ServiceOrderItem, Integer> {

    // 특정 서비스 오더 아이템을 조회
    List<ServiceOrderItem> findByServiceOrder_ServiceOrderId(Integer serviceOrderID);

}
