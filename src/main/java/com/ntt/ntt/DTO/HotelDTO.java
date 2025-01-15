package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Chief;
import com.ntt.ntt.Entity.Manager;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDateTime;

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

    private String hotelImg;

    private String hotelCheckIn;

    private String hotelCheckOut;

    private Manager managerId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
