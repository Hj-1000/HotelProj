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

    // 카트를 ManyToOne 으로 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceCartId")
    private ServiceCart serviceCart;

    // 메뉴를 ManyToOne 으로 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serviceMenuId")
    private ServiceMenu serviceMenu;

    // 장바구니에 넣은 서비스의 수량
    private Integer count;

    // 카트에 담길 아이템을 참조하는 ServiceCartItem
    public static ServiceCartItem createCartItem(ServiceCart serviceCart, ServiceMenu serviceMenu, Integer count) {
        ServiceCartItem serviceCartItem = new ServiceCartItem();
        serviceCartItem.setServiceCart(serviceCart);
        serviceCartItem.setServiceMenu(serviceMenu);
        serviceCartItem.setCount(count);

        return serviceCartItem;
    }

    //count의 증가
    public void addCount(Integer count) {
        this.count += count;
    }
}
