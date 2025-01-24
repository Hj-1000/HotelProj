package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Entity.ServiceOrder;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceOrderDTO {

    private Integer serviceOrderId;
    private Integer totalPrice; // 총 주문 금액
    private Integer memberId; // 사용자 ID
    private Integer roomId; // 방 ID
    private String serviceOrderStatus; // 주문 상태
    private List<ServiceOrderItemDTO> serviceOrderItemList; // 주문 항목 리스트

    public static ServiceOrderDTO fromEntity(ServiceOrder serviceOrder) {
        return ServiceOrderDTO.builder()
                .serviceOrderId(serviceOrder.getServiceOrderId())
                .totalPrice(serviceOrder.getTotalPrice())
                .memberId(serviceOrder.getMember().getMemberId())
                .roomId(serviceOrder.getRoom().getRoomId())
                .serviceOrderStatus(serviceOrder.getServiceOrderStatus().name()) // Enum을 String으로 변환
                .serviceOrderItemList(serviceOrder.getServiceOrderItemList().stream()
                        .map(ServiceOrderItemDTO::fromEntity)
                        .toList())
                .build();
    }
}
