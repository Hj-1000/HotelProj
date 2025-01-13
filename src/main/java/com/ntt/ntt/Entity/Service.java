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
public class Service extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomId;
    // 룸서비스 이름(치킨, 음료, ...)
    private String serviceName;
    // 룸서비스 설명
    private String serviceInfo;
    // 룸서비스 가격
    private Integer servicePrice;
    // 룸서비스 사진
    private String serviceImg;

    @ManyToOne
    @JoinColumn(name = "hotelId")
    private Hotel hotelId;

}
