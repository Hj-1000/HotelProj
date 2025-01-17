package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Entity.ServiceOrder;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderItemDTO {

    private Integer serviceOrderItemId;

    private Integer orderCount;

    private Integer totalPrice;

    private ServiceMenu serviceMenuId;

    private ServiceOrder serviceOrderId;

}
