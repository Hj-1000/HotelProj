package com.ntt.ntt.DTO;

import com.ntt.ntt.Entity.Member;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCartDTO {

    private Integer serviceCartId;

    private Member memberId;

}
