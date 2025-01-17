package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="serviceCate")
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
    // 카테고리명(한식, 중식, 주류, ...)
    @Column(length = 50, nullable = false)
    private String serviceCateName;

    @ManyToOne
    @JoinColumn(name = "hotelId")
    private Hotel hotelId;

}
