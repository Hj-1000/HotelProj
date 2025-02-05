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
public class ServiceOrderDTO {

    @NotNull(message = "정상적인 주문페이지가 아닙니다. 상품페이지로 다시 와주세요.")
    private Integer serviceMenuId; // 서비스 메뉴의 id로 받기로 함

    @Min(value = 1, message = "최소 주문 수량은 1개입니다.")
    @Max(value = 999, message = "최대 주문 수량은 999개 입니다.")
    private int count;
}
