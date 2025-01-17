package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RoomReviewDTO {

    private Integer reviewId;

    private Integer rating;

    private String reviewText;

    private Room roomId;

    private Member memberId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
