package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.ServiceCart;
import com.ntt.ntt.Entity.ServiceCartItem;
import com.ntt.ntt.Entity.ServiceMenu;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCartItemDTO {

    private Integer serviceCartItemId;
    private Integer count; // 서비스 수량
    private Integer serviceMenuId; // 서비스 메뉴 ID
    private String serviceMenuName; // 서비스 메뉴 이름
    private Integer serviceMenuPrice; // 서비스 메뉴 가격

    public static ServiceCartItemDTO fromEntity(ServiceCartItem serviceCartItem) {
        return ServiceCartItemDTO.builder()
                .serviceCartItemId(serviceCartItem.getServiceCartItemId())
                .count(serviceCartItem.getCount())
                .serviceMenuId(serviceCartItem.getServiceMenu().getServiceMenuId())
                .serviceMenuName(serviceCartItem.getServiceMenu().getServiceMenuName())
                .serviceMenuPrice(serviceCartItem.getServiceMenu().getServiceMenuPrice())
                .build();
    }
}
