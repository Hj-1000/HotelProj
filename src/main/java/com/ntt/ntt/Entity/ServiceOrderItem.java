package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="serviceOrderItem")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceOrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceOrderItemId;
    // 주문한 서비스의 수량
    private int orderCount;
    // 총 금액
    private int orderPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceMenuId")
    private ServiceMenu serviceMenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceOrderId")
    private ServiceOrder serviceOrder;

}
