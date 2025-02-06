package com.ntt.ntt.Entity;

import com.ntt.ntt.Constant.ServiceMenuStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.List;

@Entity
@Table(name="serviceMenu")
@Getter
@Setter
@ToString(exclude = "serviceCate")
@AllArgsConstructor
@NoArgsConstructor

public class ServiceMenu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceMenuId;
    // 메뉴 이름(치킨, 음료, ...)
    @Column(length = 50, nullable = false)
    private String serviceMenuName;
    // 메뉴 설명
    @Column(length = 255)
    private String serviceMenuInfo;
    // 메뉴 상태(품절, 주문가능, ...)

    @Enumerated(EnumType.STRING)
    private ServiceMenuStatus serviceMenuStatus;
    // 메뉴 가격
    @Column(nullable = false)
    private Integer serviceMenuPrice = 0;

    //재고수량
    @NotNull
    @PositiveOrZero
    private Integer serviceMenuQuantity;  // 메뉴 재고수량 추가 2025-02-06

    @ManyToOne
    @JoinColumn(name = "serviceCateId")
    private ServiceCate serviceCate;

    @OneToMany(mappedBy = "serviceMenu", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Image> serviceMenuImageList;

    public ServiceMenu(Integer serviceMenuId) {
        this.serviceMenuId = serviceMenuId;
    }
}
