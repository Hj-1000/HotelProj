package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="serviceMenu")
@Getter
@Setter
@ToString(exclude = "serviceCate")
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    @Column(nullable = false)
    private String serviceMenuStatus;
    // 메뉴 가격
    @Column(nullable = false)
    private Integer serviceMenuPrice;

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
