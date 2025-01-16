package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(nullable = false)
    private Integer cartCount;

    @OneToOne
    @JoinColumn(name = "usersId")
    private Users usersId;

    // 카트아이템으로 수정하기
//    @OneToMany
//    @JoinColumn(name = "serviceMenuId")
//    private ServiceMenu serviceMenuId;

}
