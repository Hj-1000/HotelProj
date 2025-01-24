package com.ntt.ntt.Entity;

import com.ntt.ntt.Constant.ServiceOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="serviceOrder")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceOrderId;
    // 주문상태
    @Enumerated(EnumType.STRING)
    private ServiceOrderStatus serviceOrderStatus;

    //주문의 총 금액
    private int totalPrice;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "roomId")
    private Room room;

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ServiceOrderItem> serviceOrderItemList = new ArrayList<>();

    public void setServiceOrderItemList(ServiceOrderItem serviceOrderItem) {
        this.serviceOrderItemList.add(serviceOrderItem);
    }

    public void setServiceOrderItemList(List<ServiceOrderItem> serviceOrderItemList) {
        this.serviceOrderItemList = serviceOrderItemList;
    }




}
