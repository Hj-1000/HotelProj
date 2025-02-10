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
    private Integer hotelRating;
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
    private List<Image> hotelImageList;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE)
    private Set<LikeHotel> likeHotels;

    public Hotel(Integer hotelId) {
        this.hotelId = hotelId;
    }

    //회사와 연결
    //2025-01-22 추가
    @ManyToOne
    @JoinColumn(name = "companyId", nullable = false)
    private Company company;

    //2024-02-10 양방향 삭제를 위해 추가
    @OneToMany(mappedBy = "hotelId", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Room> rooms;

}
