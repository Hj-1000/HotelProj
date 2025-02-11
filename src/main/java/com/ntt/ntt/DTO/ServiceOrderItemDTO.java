package com.ntt.ntt.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ServiceOrderItemDTO {

    private Integer serviceOrderItemId;

    private ServiceOrderDTO serviceOrderDTO; //부모객체

    private String serviceMenuName; // 주문메뉴 이름

    private Integer orderPrice; //주문가격

    private Integer count;

    private String imagePath;

}
