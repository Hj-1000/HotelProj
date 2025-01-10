package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Room;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderDTO {

    private Integer serviceCartId;

    private Integer orderCount;

    private Integer totalPrice;

    private Room roomId;

    private Room serviceId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
