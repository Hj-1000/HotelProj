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
    @Column(nullable = false)
    private Integer orderCount;
    // 총 금액
    private Integer totalPrice;

    @ManyToOne
    @JoinColumn(name = "serviceMenuId")
    private ServiceMenu serviceMenuId;

    @ManyToOne
    @JoinColumn(name = "serviceOrderId")
    private ServiceOrder serviceOrderId;

}
