package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDTO {

    private Integer roomId;

    private String roomName;

    private String roomType;

    private Integer roomPrice;

    private Boolean roomStatus;

    private String roomInfo;

    private Hotel hotelId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

    @Builder.Default
    private List<ImageDTO> roomImageDTOList = new ArrayList<>();

    // 추가 필드 포맷된 가격
    private String formattedRoomPrice;

    // 예약 시작일
    private String reservationStart;
    // 예약 종료일
    private String reservationEnd;
    // 숙박 시작일
    private String stayStart;
    // 숙박 종료일
    private String stayEnd;

}
