package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="image")
@Getter
@Setter
@ToString(exclude = "hotel")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;
    // 이미지 파일명
    private String imageName;
    // 원본 이미지명
    private String imageOriginalName;
    // 이미지 경로
    private String imagePath;
    // 대표이미지 여부
    private String imageMain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyId")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotelId")
    private Hotel hotel;

    @ManyToOne
    @JoinColumn(name = "roomId")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "serviceCateId")
    private ServiceCate serviceCate;

    @ManyToOne
    @JoinColumn(name = "serviceMenuId")
    private ServiceMenu serviceMenu;

    @ManyToOne
    @JoinColumn(name = "noticeId")
    private Notice notice;

    @ManyToOne
    @JoinColumn(name = "bannerId")
    private Banner banner;

}
