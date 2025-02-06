package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.ServiceOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOrderItemRepository extends JpaRepository<ServiceOrderItem, Integer> {

    //구매이력
    List<ServiceOrderItem> findByServiceOrder_ServiceOrderId(Integer serviceOrderId);
}
