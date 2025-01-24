package com.ntt.ntt.Repository;

import com.ntt.ntt.Entity.ServiceCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCartItemRepository extends JpaRepository<ServiceCartItem, Integer> {

    //특정 서비스 카트에 포함된 아이템 조회
    List<ServiceCartItem> findByServiceCart_ServiceCartId(Integer serviceCartId);
}
