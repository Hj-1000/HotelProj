package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="serviceCate")
@Getter
@Setter
@ToString(exclude = "hotel")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceCate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceCateId;
    // 카테고리명(한식, 중식, 주류, ...)
    @Column(length = 50, nullable = false)
    private String serviceCateName;

    @ManyToOne
    @JoinColumn(name = "hotelId")
    private Hotel hotel;

    @OneToMany(mappedBy = "serviceCate", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Image> serviceCateImageList;

    @OneToMany(mappedBy = "serviceCate", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ServiceMenu> serviceMenuList;

    //이미지 서비스 사용할 때 필요한 부모 생성자 매서드
    public ServiceCate(Integer serviceCateId) {
        this.serviceCateId = serviceCateId;
    }
}
