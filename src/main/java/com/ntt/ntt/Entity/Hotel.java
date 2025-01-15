package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="hotel")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hotel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer hotelId;
    // 호텔 이름
    @Column(length = 50, nullable = false)
    private String hotelName;
    // 호텔 지역
    @Column(length = 50)
    private String hotelLocation;
    // 호텔 주소
    @Column(length = 50)
    private String hotelAddress;
    // 호텔 설명
    @Column(length = 255)
    private String hotelInfo;
    // 호텔 평점
    private Integer hotelRating;
    // 호텔 전화번호
    @Column(length = 20)
    private String hotelPhone;
    // 호텔 이메일
    @Column(length = 50)
    private String hotelEmail;
    // 호텔 사진
    private String hotelImg;
    // 체크인 시간
    private String hotelCheckIn;
    // 체크아웃 시간
    private String hotelCheckOut;

    @ManyToOne
    @JoinColumn(name = "managerId")
    private Manager managerId;

}
