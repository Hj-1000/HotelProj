package com.ntt.ntt.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ServiceOrderItemUpdateDTO {
    private Integer serviceOrderItemId;
    private Integer count;
}
