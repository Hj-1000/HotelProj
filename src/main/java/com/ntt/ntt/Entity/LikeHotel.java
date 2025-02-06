package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedBy;

@Entity
@Table(name = "likes_hotel")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeHotel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer likeHotelId; // 호텔 찜 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "likesId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Likes likes; // 장바구니

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotelId", nullable = false)
    private Hotel hotel; // 호텔

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    // LikeHotel 객체 생성 메서드
    public static LikeHotel createLikeHotel(Likes likes, Hotel hotel) {
        return LikeHotel.builder()
                .likes(likes)
                .hotel(hotel)
                .build();
    }

    @Override
    public String toString() {
        return "LikeHotel{" +
                "likeHotelId=" + likeHotelId +
                ", hotelName='" + hotel +
                ", createdBy='" + createdBy +'\'' +
                '}';
    }
}
