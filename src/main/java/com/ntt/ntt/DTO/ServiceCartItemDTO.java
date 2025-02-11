package com.ntt.ntt.DTO;

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
public class ServiceCartItemDTO {
    private Integer ServiceCartItemId;

    @NotNull(message = "상품 아이디는 필수 입력 값 입니다.")
    private Integer ServiceMenuId;

    @Min(value = 1, message = "최소수량은 1개 입니다.")
    private Integer count;

}
