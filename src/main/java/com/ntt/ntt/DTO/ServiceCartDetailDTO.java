package com.ntt.ntt.DTO;

import com.ntt.ntt.Constant.ServiceMenuStatus;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCartDetailDTO {

    private Integer serviceCartItemId; // 장바구니 상품의 아이디
    private String serviceMenuName;  // 장바구니에 담길 메뉴 이름
    private Integer serviceMenuPrice;; // 메뉴의 금액
    private Integer count; //수량
    private String imagePath; //상품 이미지 경로
}
