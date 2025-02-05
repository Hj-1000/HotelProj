package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.BaseEntity;
import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.ServiceCartItem;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor

public class ServiceCartOrderDTO extends BaseEntity {
    private Integer serviceCartItemId;

    private List<ServiceCartOrderDTO> orderDTOList;
    // 장바구니는 여러개의 상품을 주문하므로
    // 자기 자신을 List로 가지고 있는다.

}
