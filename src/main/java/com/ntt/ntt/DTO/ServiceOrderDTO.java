package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import com.ntt.ntt.Entity.Room;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderDTO {

    private Integer serviceOrderId;

    private String serviceOrderStatus;

    private Member memberId;

    private Room roomId;

}
