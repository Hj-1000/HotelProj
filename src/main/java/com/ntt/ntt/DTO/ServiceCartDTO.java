package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Entity.ServiceOrder;
import com.ntt.ntt.Entity.Users;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCartDTO {

    private Integer serviceCartId;

    private Integer cartCount;

    private ServiceOrder serviceOrderId;

    private Users usersId;

    private ServiceMenu serviceMenuId;

}
