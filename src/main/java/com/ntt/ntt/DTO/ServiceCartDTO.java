package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCartDTO {

    private Integer serviceCartId;

    private Integer count;

    private Room roomId;

    private Room serviceId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
