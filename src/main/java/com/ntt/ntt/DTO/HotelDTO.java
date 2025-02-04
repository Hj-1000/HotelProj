package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Company;
import com.ntt.ntt.Entity.Member;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder //2025-01-21 추가
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HotelDTO {

    private Integer hotelId;

    private String hotelName;

    private String hotelLocation;

    private String hotelAddress;

    private String hotelInfo;

    private Integer hotelRating;

    private String hotelPhone;

    private String hotelEmail;

    private String hotelCheckIn;

    private String hotelCheckOut;

    private Member memberId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    @Builder.Default
    private List<ImageDTO> hotelImgDTOList = new ArrayList<>(); //이미지 2025-01-21추가

    private Company companyId; //회사 연결 (회사 안에 들어있는 호텔) 2025-01-22 추가
    private String companyName; //회사 연결 (회사 안에 들어있는 호텔) 2025-01-22 추가

    public HotelDTO(Integer hotelId, String hotelName) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
    }
}
