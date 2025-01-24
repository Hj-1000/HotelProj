package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Entity.ServiceOrder;
import com.ntt.ntt.Entity.ServiceOrderItem;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceOrderItemDTO {

    private Integer serviceOrderItemId;
    private Integer serviceMenuId; // 서비스 메뉴 ID
    private String serviceMenuName; // 서비스 메뉴 이름
    private Integer orderCount; // 주문 수량
    private Integer orderPrice; // 주문 당시 금액

    public static ServiceOrderItemDTO fromEntity(ServiceOrderItem serviceOrderItem) {
        return ServiceOrderItemDTO.builder()
                .serviceOrderItemId(serviceOrderItem.getServiceOrderItemId())
                .orderCount(serviceOrderItem.getOrderCount())
                .orderPrice(serviceOrderItem.getOrderPrice())
                .serviceMenuId(serviceOrderItem.getServiceMenu().getServiceMenuId())
                .serviceMenuName(serviceOrderItem.getServiceMenu().getServiceMenuName())
                .build();
    }
}
