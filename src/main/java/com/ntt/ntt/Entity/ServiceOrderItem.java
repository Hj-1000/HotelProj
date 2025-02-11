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
    // 주문한 서비스메뉴의 수량
    private Integer count;
    // 주문 금액
    private Integer orderPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceMenuId")
    private ServiceMenu serviceMenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceOrderId")
    private ServiceOrder serviceOrder;


}
