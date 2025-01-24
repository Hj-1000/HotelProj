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
    private Integer count;

    // 카트를 ManyToOne 으로 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceCartId")
    private ServiceCart serviceCart;

    // 메뉴를 ManyToOne 으로 참조
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
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive.");
        }
        this.count += count;
    }
}
