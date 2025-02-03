package com.ntt.ntt.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LikeHotelDTO {

    @NotNull(message = "상품 아이디는 필수 입력 값입니다.")
    private Integer HotelId;

    private String createBy;

}
