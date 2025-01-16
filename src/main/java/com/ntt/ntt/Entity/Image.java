package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="image")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;
    // 이미지 파일명
    private String imageName;
    // 이미지 경로
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "hotelId")
    private Hotel hotelId;

    @ManyToOne
    @JoinColumn(name = "roomId")
    private Room roomId;

    @ManyToOne
    @JoinColumn(name = "serviceCateId")
    private ServiceCate serviceCateId;

    @ManyToOne
    @JoinColumn(name = "serviceMenuId")
    private ServiceMenu serviceMenuId;

    @ManyToOne
    @JoinColumn(name = "noticeId")
    private Notice noticeId;

    @ManyToOne
    @JoinColumn(name = "bannerId")
    private Banner baannerId;

}
