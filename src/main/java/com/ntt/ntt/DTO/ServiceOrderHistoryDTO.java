package com.ntt.ntt.DTO;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderHistoryDTO {
    //주문내역에 필요한 serviceOrder에서 필요한
    //내용만큼 담은 serviceOrderHistoryDTO 브라우저로 전달할 내용들
    //serviceOrder에서 검색한 내용을 가지고 해당 필드값을 set

    private Integer serviceOrderId; // 주문아이디

    private LocalDateTime regDate; //주문 날짜 생성날짜

    private ServiceOrderStatus serviceOrderStatus; //주문 상태

    private ReservationDTO reservationDTO; // 부모객체 불러오기 위함 2025-02-24추가 천현종

    private MemberDTO memberDTO; //주문의 멤버 정보를 불러오기 위함 2025-02-24추가 천현종

    private List<ServiceOrderItemDTO> serviceOrderItemDTOList = new ArrayList<>();
    //구매이력에 담길 구매 아이템들




    public void addServiceOrderItemDTO(ServiceOrderItemDTO serviceOrderItemDTO) {
        serviceOrderItemDTOList.add(serviceOrderItemDTO);
    }

    public ServiceOrderHistoryDTO setServiceOrderItemDtoList(List<ServiceOrderItemDTO> serviceOrderItemDTOList) {
        this.serviceOrderItemDTOList = serviceOrderItemDTOList;
        return this;
    }
}
