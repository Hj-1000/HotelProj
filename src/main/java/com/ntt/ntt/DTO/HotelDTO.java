package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Company;
import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Member;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "호텔 이름은 필수 입력값입니다.")
    private String hotelName;

    @NotBlank(message = "호텔 지역은 필수 입력값입니다.")
    private String hotelLocation;

    @NotBlank(message = "호텔 주소는 필수 입력값입니다.")
    private String hotelAddress;

    @NotBlank(message = "호텔 설명은 필수 입력값입니다.")
    private String hotelInfo;

    private Integer hotelRating;

    @NotBlank(message = "호텔 전화는 필수 입력값입니다.")
    private String hotelPhone;

    private String hotelEmail;

    @NotBlank(message = "호텔 체크인 시간은 필수 입력값입니다.")
    private String hotelCheckIn;

    @NotBlank(message = "호텔 체크아웃 시간은 필수 입력값입니다.")
    private String hotelCheckOut;

    private Member memberId;

    private MemberDTO member;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    @Builder.Default
    private List<ImageDTO> hotelImgDTOList = new ArrayList<>(); //이미지 2025-01-21추가

    private Integer companyId; //회사 연결 (회사 안에 들어있는 호텔) 2025-01-22 추가
    private String companyName; //회사 연결 (회사 안에 들어있는 호텔) 2025-01-22 추가

    public HotelDTO(Integer hotelId, String hotelName) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
    }

    // Hotel 엔티티를 받는 생성자 추가
    public HotelDTO(Hotel hotel) {
        this.hotelId = hotel.getHotelId();
        this.hotelName = hotel.getHotelName();
        this.companyId = hotel.getCompany().getCompanyId(); // Company 엔티티를 DTO로 변환

        this.regDate = hotel.getRegDate();
        this.modDate = hotel.getModDate();

        // Member 정보 포함
        this.member = hotel.getMember() != null ? new MemberDTO(hotel.getMember()) : null;
    }
}
