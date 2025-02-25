package com.ntt.ntt.DTO;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ServiceOrderUpdateDTO {
    private Integer serviceOrderId;
    private ServiceOrderStatus serviceOrderStatus;
    private List<ServiceOrderItemUpdateDTO> orderItems;
}
