package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.ServiceCart;
import com.ntt.ntt.Entity.ServiceMenu;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCartItemDTO {

    private Integer serviceCartItemId;

    private Integer serviceCartItemCount;

    private ServiceCart serviceCartId;

    private ServiceMenu serviceMenuId;

}
