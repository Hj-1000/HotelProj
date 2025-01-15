package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="service")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceMenu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceMenuId;
    // 룸서비스 메뉴 이름(치킨, 음료, ...)
    @Column(length = 50)
    private String serviceMenuName;
    // 메뉴 설명
    @Column(length = 255)
    private String serviceMenuInfo;
    // 메뉴 상태(품절, 주문가능, ...)
    private Integer serviceMenuStatus;
    // 메뉴 가격
    private Integer serviceMenuPrice;
    // 메뉴 사진
    private String serviceImg;

    @ManyToOne
    @JoinColumn(name = "serviceCateId")
    private ServiceCate serviceCateId;

}
