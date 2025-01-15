package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Hotel;
import com.ntt.ntt.Entity.ServiceMenu;
import com.ntt.ntt.Entity.ServiceOrder;
import com.ntt.ntt.Entity.User;
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

    private Integer cartCount;

    private ServiceOrder serviceOrderId;

    private User userId;

    private ServiceMenu serviceMenuId;

}
