package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
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
public class ReservationDTO {

    private Integer reservationId;

    private String checkInDate;

    private String checkOutDate;

    private Integer totalPrice;

    private String reservationStatus;

    private User userId;

    private Room roomId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
