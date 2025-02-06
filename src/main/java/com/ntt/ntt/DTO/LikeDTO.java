package com.ntt.ntt.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
@Builder
@ToString
@Getter
@Setter
public class LikeDTO {

    private Integer hotelId;

    private Integer likeHotelId;

    @JsonProperty("memberEmail")
    private String memberEmail;

    private HotelDTO hotelDTO;

    private List<LikeDTO> likeDTOList;

}
