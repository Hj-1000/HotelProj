package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="serviceCart")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCart extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceCartId;
    // 장바구니에 넣은 서비스의 수량

    @OneToOne
    @JoinColumn(name = "member")
    private Member member;

    @OneToMany(mappedBy = "serviceCart", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServiceCartItem> serviceCartItemList = new ArrayList<>();


    //카트 생성 메소드
    // 이 메소드는 정적메소드로 실행시 Member 타입의 객체를 입력받아(파라미터)
    // Cart 타입을 반환한다.
    public static ServiceCart createCart(Member member) {
        ServiceCart serviceCart = new ServiceCart();
        serviceCart.setMember(member);

        return serviceCart;
    }

}
