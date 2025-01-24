package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="serviceCartItem")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceCartItemId;

    // 장바구니에 넣은 서비스의 수량
    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceCartId")
    private ServiceCart serviceCart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceMenuId")
    private ServiceMenu serviceMenu;

    public static ServiceCartItem serviceCartItem(ServiceCart serviceCart, ServiceMenu serviceMenu, int count) {
        ServiceCartItem serviceCartItem = new ServiceCartItem();
        serviceCartItem.setServiceCart(serviceCart);
        serviceCartItem.setServiceMenu(serviceMenu);
        serviceCartItem.setCount(count);

        return serviceCartItem;
    }

    //count의 증가
    public void addCount(int count) {
        this.count += count;
    }
}
