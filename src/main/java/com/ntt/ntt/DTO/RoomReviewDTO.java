package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RoomReviewDTO {

    private Integer reviewId;

    private String rating;

    private String reviewText;

    private Room roomId;

    private User userId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
