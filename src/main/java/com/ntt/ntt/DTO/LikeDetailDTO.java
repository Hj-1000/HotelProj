package com.ntt.ntt.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LikeDetailDTO {

    private Integer LikeHotelId;

    private Integer hotelId;

    private String hotelName;     // 상품명

    private String imagePath;      //이미지 경로

    // 체크인 시간
    private String hotelCheckIn;

    // 호텔 주소
    private String hotelLocation;

    public LikeDetailDTO(Integer likeHotelId, Integer hotelId, String hotelName,
                         String imagePath, String hotelCheckIn, String hotelLocation) {
        this.LikeHotelId = likeHotelId;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.imagePath = imagePath;
        this.hotelCheckIn = hotelCheckIn;
        this.hotelLocation = hotelLocation;
    }
}
