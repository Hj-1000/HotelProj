package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderDTO {

    private Integer serviceOrderId;

    private Integer orderCount;

    private Integer totalPrice;

    private Room roomId;

    private ServiceMenu serviceMenuId;

    private User userId;

}
