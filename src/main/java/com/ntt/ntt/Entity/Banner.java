package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="banner")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Banner extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bannerId;
    // 배너 제목
    private String bannerTitle;
    // 배너 상태
    private Boolean bannerStatus;
    // 배너 순위
    private Integer bannerRank;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member memberId;

}
