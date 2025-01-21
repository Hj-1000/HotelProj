package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
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
    private List<ImageDTO> hotelImgDTOList = new ArrayList<>();

}
