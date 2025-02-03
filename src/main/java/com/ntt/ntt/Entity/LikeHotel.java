package com.ntt.ntt.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedBy;

@Entity //jpa에서 관리를 할수 있습니다. 엔티티매니저
@Table(name = "likes_hotel")   //jpa를 이용할 때 자동으로 데이터베이스 설정과 데이터베이스 내 테이블을 같이 확인하기때문에 에러 나올수 있음
// 데이터베이스상 어떤 테이블로 생성할 것인 정보를 담기 위한 어노테이션
@Getter
@Setter
@ToString
@NoArgsConstructor
public class LikeHotel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer likeHotelId; //상품코드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "likesId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Likes likes;              // 장바구니

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "hotelId")
    private Hotel hotel;              //아이템

    @CreatedBy
    @Column(updatable = false)
    private String createBy;


    // 카트에 담길 아이템을 참조하는 cartItem
    public static LikeHotel createLikeHotel (Likes likes, Hotel hotel) {
        LikeHotel LikeHotel = new LikeHotel();
        LikeHotel.setLikes(likes);
        LikeHotel.setHotel(hotel);
        return LikeHotel;

        // CartItem cartItem = CartItem.createCartItem(cart, item, count);
        //만들어진 객체로 저장을 할껄?
    }





}
