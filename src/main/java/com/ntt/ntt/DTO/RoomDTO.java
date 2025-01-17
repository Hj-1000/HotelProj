package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
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

}
