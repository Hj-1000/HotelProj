package com.ntt.ntt.Repository;

import com.ntt.ntt.DTO.ServiceCartDetailDTO;
import com.ntt.ntt.Entity.ServiceCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCartItemRepository extends JpaRepository<ServiceCartItem, Integer> {

    //카트의 pk, serviceMenuId를 기본키로 serviceCartItem을 찾는다. 내 카트에 담긴 몇번 아이템
    // 내카트에 담긴 아이템 = serviceCartItem
    public ServiceCartItem findByServiceCart_ServiceCartIdAndServiceMenu_ServiceMenuId(Integer serviceCartId, Integer serviceMenuId);

    public List<ServiceCartItem> findByServiceCart_ServiceCartId(Integer serviceCartId);

    //장바구니 id를 받아와서
    //dto로 sql결과를 받아내는 쿼리문
    //serviceCartDetailDTO 에 담을 것임
    @Query("SELECT new com.ntt.ntt.DTO.ServiceCartDetailDTO(sci.serviceCartItemId, sm.serviceMenuId, sm.serviceMenuName, sm.serviceMenuPrice, sci.count, i.imagePath) " +
            "FROM ServiceCartItem sci " +
            "JOIN sci.serviceMenu sm " +
            "LEFT JOIN Image i ON i.serviceMenu.serviceMenuId = sm.serviceMenuId " +
            "WHERE sci.serviceCart.serviceCartId = :serviceCartId " +
            "ORDER BY sci.serviceCartItemId ASC")
    List<ServiceCartDetailDTO> findByServiceCartDetailDTOList(@Param("serviceCartId") Integer serviceCartId);

}
