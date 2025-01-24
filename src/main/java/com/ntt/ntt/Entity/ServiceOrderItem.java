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
    private Integer orderCount;
    // 주문 당시 금액
    private Integer orderPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceMenuId")
    private ServiceMenu serviceMenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceOrderId")
    private ServiceOrder serviceOrder;


    //주문한 수량과 메뉴의 가격의 곱이 곧 당시 주문의 금액
    public void calculateOrderPrice() {
        this.orderPrice = this.orderCount * this.serviceMenu.getServiceMenuPrice();
    }
    public void addCount(int orderCount) {
        if (orderCount <= 0) {
            throw new IllegalArgumentException("Count must be positive.");
        }
        this.orderCount += orderCount;
        calculateOrderPrice(); // 수량이 변경될 때마다 가격 계산
    }

}
