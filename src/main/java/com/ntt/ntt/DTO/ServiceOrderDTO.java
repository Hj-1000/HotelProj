package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Room;
import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Entity.Users;
import lombok.*;

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

    private Users usersId;

}
