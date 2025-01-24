package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.ServiceCart;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCartDTO {

    private Integer serviceCartId;
    private Integer memberId; // Member ID만 저장
    private List<ServiceCartItemDTO> serviceCartItemList; // 장바구니 아이템 목록

    public static ServiceCartDTO fromEntity(ServiceCart serviceCart) {
        return ServiceCartDTO.builder()
                .serviceCartId(serviceCart.getServiceCartId())  // 서비스 카트 ID
                .memberId(serviceCart.getMember().getMemberId())  // Member ID
                .serviceCartItemList(serviceCart.getServiceCartItemList().stream()
                        .map(ServiceCartItemDTO::fromEntity)
                        .toList())
                .build();
    }
}
