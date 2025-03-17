package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

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
    private Double  hotelRating;
    // 호텔 전화번호
    @Column(length = 20)
    private String hotelPhone;
    // 호텔 이메일
    @Column(length = 50)
    private String hotelEmail;
    // 체크인 시간
    private String hotelCheckIn;
    // 체크아웃 시간
    private String hotelCheckOut;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Image> hotelImageList;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<LikeHotel> likeHotels;

    public Hotel(Integer hotelId) {
        this.hotelId = hotelId;
    }

    //회사와 연결
    //2025-01-22 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)  // 여기가 중요합니다.
    private Company company;

    //2024-02-10 양방향 삭제를 위해 추가
    @OneToMany(mappedBy = "hotelId", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude //무한루프방지
    private List<Room> rooms;

    //2024-02-11 양방향 삭제를 위해 추가
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude //무한루프방지
    private List<ServiceCate> serviceCate;

}
