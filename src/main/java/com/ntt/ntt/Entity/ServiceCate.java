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
public class ServiceCate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceCateId;
    // 카테고리명(한식, 일식, 주류, ...)
    @Column(length = 50)
    private String serviceCateName;
    // 카테고리 대표 이미지
    private String serviceImg;

    @ManyToOne
    @JoinColumn(name = "hotelId")
    private Hotel hotelId;

}
